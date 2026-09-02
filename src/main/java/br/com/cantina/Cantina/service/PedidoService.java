package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.DTO.CriarPedidoDTO;
import br.com.cantina.Cantina.database.DTO.ItemPedidoDTO;
import br.com.cantina.Cantina.database.DTO.PedidoResponseDTO;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.database.model.Usuario;
import br.com.cantina.Cantina.database.repository.PedidoRepository;
import br.com.cantina.Cantina.exception.RegistroNaoEncontradoException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {


    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioService usuarioService, ProdutoService produtoService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
    }

    @Transactional
    public PedidoResponseDTO criarPedido(Long usuarioId, CriarPedidoDTO dto) {
        Usuario usuario = usuarioService.buscarEntidadePorId(usuarioId);

        LocalDateTime horario = dto.getHorarioEstimadoRetirada();
        int hora = horario.getHour();
        if (hora < 8 || hora >= 21) {
            throw new IllegalArgumentException(
                    "O horário de retirada deve estar entre 08:00 e 21:00");
        }

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setHorarioEstimadoRetirada(dto.getHorarioEstimadoRetirada());
        pedido.setFormaPagamento(dto.getFormaPagamento());
        pedido.setPago(false);

        BigDecimal total = BigDecimal.ZERO;

        for (ItemPedidoDTO itemDto : dto.getItens()) {
            Produto produto = produtoService.buscarPorId(itemDto.getProdutoId());

            produtoService.baixarEstoque(produto, itemDto.getQuantidade());

            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidade(itemDto.getQuantidade());
            item.setPrecoUnitario(produto.getPreco());

            pedido.adicionarItem(item);
            total = total.add(item.getSubtotal());
        }

        pedido.setValorTotal(total);
        Pedido salvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(salvo);
    }

    public PedidoResponseDTO buscarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado"));
        return new PedidoResponseDTO(pedido);
    }

    public List<PedidoResponseDTO> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdOrderByDataHoraPedidoDesc(usuarioId).stream()
                .map(PedidoResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Page<PedidoResponseDTO> listar(StatusPedido status, Pageable pageable) {
        if (status != null) {
            return pedidoRepository.findByStatusPedido(status, pageable).map(PedidoResponseDTO::new);
        }
        return pedidoRepository.findAll(pageable).map(PedidoResponseDTO::new);
    }

    @Transactional
    public PedidoResponseDTO atualizarStatus(Long id, StatusPedido novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado"));

        StatusPedido statusAtual = pedido.getStatus();

        if (!transicaoPermitida(statusAtual, novoStatus)) {
            throw new IllegalStateException(
                    "Transição de status inválida: de " + statusAtual + " para " + novoStatus);
        }

        boolean estavaCancelado = pedido.getStatus() == StatusPedido.CANCELADO;
        boolean vaiCancelar = novoStatus == StatusPedido.CANCELADO;

        if (vaiCancelar && !estavaCancelado) {
            for (ItemPedido item : pedido.getItens()) {
                if (item.getId() != null) {
                    produtoService.devolverEstoque(item.getProduto(), item.getQuantidade());
                }
            }
        }

        pedido.setStatus(novoStatus);
        Pedido salvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(salvo);
    }
    @Transactional
    public PedidoResponseDTO marcarPagamento(Long id, boolean pago) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado"));
        pedido.setPago(pago);
        Pedido salvo = pedidoRepository.save(pedido);
        return new PedidoResponseDTO(salvo);
    }

    public Pedido buscarEntidadePorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pedido não encontrado"));
    }

    private boolean transicaoPermitida(StatusPedido atual, StatusPedido novo) {
        return switch (atual) {
            case PENDENTE -> novo == StatusPedido.EM_PREPARO || novo == StatusPedido.CANCELADO;
            case EM_PREPARO -> novo == StatusPedido.PRONTO || novo == StatusPedido.CANCELADO;
            case PRONTO -> novo == StatusPedido.ENTREGUE || novo == StatusPedido.CANCELADO;
            case ENTREGUE, CANCELADO -> false;
        };
    }

}