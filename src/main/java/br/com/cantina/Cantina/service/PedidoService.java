package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.database.model.Usuario;
import br.com.cantina.Cantina.repository.PedidoRepository;
import br.com.cantina.Cantina.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import br.com.cantina.Cantina.exception.ResourceNotFoundException;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ItemPedidoService itemPedidoService;

    @Autowired
    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository, ItemPedidoService itemPedidoService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.itemPedidoService = itemPedidoService;
    }

    public void verificarPedido(List<ItemPedido> itemPedido) {
        if (itemPedido == null || itemPedido.isEmpty()) {
            throw new IllegalArgumentException("Pedido precisa conter ao menos um item");
        }
    }

    @Transactional
    public Pedido criarPedido(Pedido pedido) {
        if (pedido.getUsuario() != null && pedido.getUsuario().getId() != null) {
            Usuario usuario = usuarioRepository.findById(pedido.getUsuario().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
            pedido.setUsuario(usuario);
        } else {
            throw new IllegalArgumentException("Usuário é obrigatório e deve conter um ID válido");
        }

        if (pedido.getItens() != null) {
            pedido.getItens().forEach(item -> {
                item.setPedido(pedido);
                itemPedidoService.completarItemComProdutoOficial(item);
            });
        }
        verificarPedido(pedido.getItens());

        BigDecimal total = pedido.getItens().stream()
                .map(ItemPedido::getPreco)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(total);

        return pedidoRepository.save(pedido);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado"));
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAll();
    }

    @Transactional
    public void excluirPedido(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pedido não encontrado");
        }
        pedidoRepository.deleteById(id);
    }

    @Transactional
    public Pedido atualizarStatus(Long id, StatusPedido status) {
        if (status == null) {
            throw new IllegalArgumentException("O status do pedido é obrigatório");
        }
        Pedido pedido = buscarPorId(id);
        pedido.setStatus(status);
        return pedidoRepository.save(pedido);
    }
}