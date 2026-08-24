package br.com.cantina.Cantina.service;


import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.repository.ItemPedidoRepository;
import br.com.cantina.Cantina.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.cantina.Cantina.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemPedidoService {
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProdutoService produtoService;

    @Autowired
    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository, PedidoRepository pedidoRepository, ProdutoService produtoService) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.produtoService = produtoService;
    }

    public void completarItemComProdutoOficial(ItemPedido itemPedido) {
        if (itemPedido.getProduto() == null || itemPedido.getProduto().getId() == null) {
            throw new IllegalStateException("Produto é obrigatório e deve conter um ID válido");
        }
        br.com.cantina.Cantina.database.model.Produto produto = produtoService.buscarPorId(itemPedido.getProduto().getId());
        itemPedido.setProduto(produto);
        itemPedido.setNome(produto.getNome());
        itemPedido.setPreco(produto.getPreco());
    }

    @Transactional
    public ItemPedido criarItemPedido(ItemPedido itemPedido) {
        if (itemPedido == null) {
            throw new IllegalStateException("Item do pedido é obrigatório");
        }
        if (itemPedido.getPedido() == null || itemPedido.getPedido().getId() == null) {
            throw new IllegalStateException("Pedido é obrigatório");
        }
        completarItemComProdutoOficial(itemPedido);
        Pedido pedido = pedidoRepository.findById(itemPedido.getPedido().getId())
                .orElseThrow(() -> new IllegalStateException("Pedido não encontrado"));
        itemPedido.setPedido(pedido);
        ItemPedido salvo = itemPedidoRepository.save(itemPedido);
        itemPedidoRepository.flush();
        recalcularValorTotalPedido(pedido);
        return salvo;
    }

    @Transactional
    public ItemPedido atualizarItemPedido(Long id, ItemPedido itemPedido) {
        if (itemPedido == null) {
            throw new IllegalStateException("Item do pedido é obrigatório");
        }
        ItemPedido itemPedidoExistente = itemPedidoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException
                ("Item do pedido não encontrado"));
        itemPedidoExistente.setProduto(itemPedido.getProduto());
        completarItemComProdutoOficial(itemPedidoExistente);
        ItemPedido salvo = itemPedidoRepository.save(itemPedidoExistente);
        itemPedidoRepository.flush();
        recalcularValorTotalPedido(itemPedidoExistente.getPedido());
        return salvo;
    }

    @Transactional
    public void excluirItemPedido(Long id) {
        ItemPedido item = itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item do pedido não encontrado"));
        Pedido pedido = item.getPedido();
        itemPedidoRepository.delete(item);
        itemPedidoRepository.flush();
        recalcularValorTotalPedido(pedido);
    }

    private void recalcularValorTotalPedido(Pedido pedido) {
        List<ItemPedido> itens = itemPedidoRepository.findByPedidoId(pedido.getId());
        BigDecimal total = itens.stream()
                .map(ItemPedido::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(total);
        pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> listAll() {
        return itemPedidoRepository.findAll();
    }

    public ItemPedido buscarPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item do pedido não encontrado"));
    }
}
