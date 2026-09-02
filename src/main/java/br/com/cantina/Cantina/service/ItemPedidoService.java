package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.DTO.ItemPedidoDTO;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.database.repository.ItemPedidoRepository;
import br.com.cantina.Cantina.database.repository.PedidoRepository;
import br.com.cantina.Cantina.database.repository.ProdutoRepository;
import br.com.cantina.Cantina.exception.RegistroNaoEncontradoException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ItemPedidoService {

    private final ItemPedidoRepository itemPedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository,
                              ProdutoRepository produtoRepository,
                              PedidoRepository pedidoRepository,
                              ProdutoService produtoService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.produtoRepository = produtoRepository;
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
    }

    @Transactional
    public ItemPedido adicionarItemAoPedido(Long pedidoId, ItemPedidoDTO dto) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado com o id: " + pedidoId));

        exigirPedidoPendente(pedido);

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() -> new RegistroNaoEncontradoException("Produto não encontrado com o id: " + dto.getProdutoId()));

        if (dto.getQuantidade() == null || dto.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade do item deve ser maior que zero");
        }

        if (!produto.isAtivo()) {
            throw new IllegalArgumentException(
                    "O produto '" + produto.getNome() + "' não está disponível no momento");
        }

        produtoService.baixarEstoque(produto, dto.getQuantidade());

        ItemPedido novoItem = new ItemPedido();
        novoItem.setQuantidade(dto.getQuantidade());
        novoItem.setPrecoUnitario(produto.getPreco());
        novoItem.setProduto(produto);

        pedido.adicionarItem(novoItem);
        recalcularValorTotal(pedido);
        pedidoRepository.save(pedido);

        return novoItem;
    }

    @Transactional
    public ItemPedido atualizarItemPedido(Long id, ItemPedidoDTO dto) {
        if (dto == null || dto.getQuantidade() == null || dto.getQuantidade() <= 0) {
            throw new IllegalArgumentException("A quantidade do item é obrigatória e deve ser maior que zero");
        }

        ItemPedido itemExistente = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Item do pedido não encontrado"));

        exigirPedidoPendente(itemExistente.getPedido());

        Produto produto = itemExistente.getProduto();
        int quantidadeAntiga = itemExistente.getQuantidade();
        int quantidadeNova = dto.getQuantidade();
        int diferenca = quantidadeNova - quantidadeAntiga;

        if (diferenca > 0) {
            produtoService.baixarEstoque(produto, diferenca);
        } else if (diferenca < 0) {
            produtoService.devolverEstoque(produto, -diferenca);
        }

        itemExistente.setQuantidade(quantidadeNova);
        itemExistente.setPrecoUnitario(produto.getPreco());
        itemPedidoRepository.save(itemExistente);

        recalcularValorTotal(itemExistente.getPedido());
        pedidoRepository.save(itemExistente.getPedido());

        return itemExistente;
    }

    @Transactional
    public void excluirItemPedido(Long id) {
        ItemPedido item = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Item do pedido não encontrado"));

        exigirPedidoPendente(item.getPedido());

        Pedido pedido = item.getPedido();
        produtoService.devolverEstoque(item.getProduto(), item.getQuantidade());

        pedido.getItens().remove(item);
        itemPedidoRepository.delete(item);

        recalcularValorTotal(pedido);
        pedidoRepository.save(pedido);
    }

    private void exigirPedidoPendente(Pedido pedido) {
        if (pedido.getStatus() != StatusPedido.PENDENTE) {
            throw new IllegalStateException(
                    "Não é possível alterar os itens de um pedido com status " + pedido.getStatus()
                            + ". Só é possível editar pedidos PENDENTES.");
        }
    }

    private void recalcularValorTotal(Pedido pedido) {
        BigDecimal total = pedido.getItens().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(total);
    }

    public Long buscarUsuarioIdDoPedidoDoItem(Long itemId) {
        ItemPedido item = itemPedidoRepository.findById(itemId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Item do pedido não encontrado"));
        return item.getPedido().getUsuario().getId();
    }

    public Long buscarUsuarioIdDoPedido(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado"));
        return pedido.getUsuario().getId();
    }
}
