package br.com.cantina.Cantina.service;


import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.repository.ItemPedidoRepository;
import br.com.cantina.Cantina.repository.PedidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ItemPedidoService {
    private final ItemPedidoRepository itemPedidoRepository;
    private final PedidoRepository pedidoRepository;

    @Autowired
    public ItemPedidoService(ItemPedidoRepository itemPedidoRepository, PedidoRepository pedidoRepository) {
        this.itemPedidoRepository = itemPedidoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public ItemPedido criarItemPedido(ItemPedido itemPedido) {
        if (itemPedido == null) {
            throw new IllegalStateException("Item do pedido é obrigatório");
        }
        if (itemPedido.getPedido() == null || itemPedido.getPedido().getId() == null) {
            throw new IllegalStateException("Pedido é obrigatório");
        }
        if (itemPedido.getPreco() == null || itemPedido.getPreco().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("O preço do item do pedido é obrigatório e não pode ser negativo");
        }
        Pedido pedido = pedidoRepository.findById(itemPedido.getPedido().getId())
                .orElseThrow(() -> new IllegalStateException("Pedido não encontrado"));
        itemPedido.setPedido(pedido);
        return itemPedidoRepository.save(itemPedido);
    }

    @Transactional
    public ItemPedido atualizarItemPedido(Long id, ItemPedido itemPedido) {
        if (itemPedido == null) {
            throw new IllegalStateException("Item do pedido é obrigatório");
        }
        ItemPedido itemPedidoExistente = itemPedidoRepository.findById(id).orElseThrow(() -> new IllegalStateException
                ("Item do pedido não encontrado"));
        itemPedidoExistente.setNome(itemPedido.getNome());
        itemPedidoExistente.setPreco(itemPedido.getPreco());
        return itemPedidoRepository.save(itemPedidoExistente);
    }

    @Transactional
    public void excluirItemPedido(Long id) {
        itemPedidoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ItemPedido> listAll() {
        return itemPedidoRepository.findAll();
    }

    public ItemPedido buscarPorId(Long id) {
        return itemPedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item do pedido não encontrado"));
    }
}
