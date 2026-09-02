package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.FormaPagamento;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.database.model.Pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoResponseDTO {
    public static class ItemDTO {
        private final Long produtoId;
        private final String nomeProduto;
        private final Integer quantidade;
        private final BigDecimal precoUnitario;
        private final BigDecimal subtotal;

        public ItemDTO(ItemPedido item) {
            this.produtoId = item.getProduto().getId();
            this.nomeProduto = item.getProduto().getNome();
            this.quantidade = item.getQuantidade();
            this.precoUnitario = item.getPrecoUnitario();
            this.subtotal = item.getSubtotal();
        }

        public Long getProdutoId() {
            return produtoId;
        }

        public String getNomeProduto() {
            return nomeProduto;
        }

        public Integer getQuantidade() {
            return quantidade;
        }

        public BigDecimal getPrecoUnitario() {
            return precoUnitario;
        }

        public BigDecimal getSubtotal() {
            return subtotal;
        }
    }

    private final Long id;
    private final Long usuarioId;
    private final String nomeUsuario;
    private final StatusPedido status;
    private final LocalDateTime dataHoraPedido;
    private final LocalDateTime horarioEstimadoRetirada;
    private final FormaPagamento formaPagamento;
    private final boolean pago;
    private final BigDecimal valorTotal;
    private final List<ItemDTO> itens;

    public PedidoResponseDTO(Pedido pedido) {
        if (pedido == null) {
            throw new IllegalArgumentException("Pedido não pode ser nulo");
        }

        var usuario = pedido.getUsuario();

        this.id = pedido.getId();
        this.usuarioId = usuario != null ? usuario.getId() : null;
        this.nomeUsuario = usuario != null ? usuario.getNome() : null;
        this.status = pedido.getStatus();
        this.dataHoraPedido = pedido.getDataHoraPedido();
        this.horarioEstimadoRetirada = pedido.getHorarioEstimadoRetirada();
        this.formaPagamento = pedido.getFormaPagamento();
        this.pago = pedido.isPago();
        this.valorTotal = pedido.getValorTotal();
        this.itens = pedido.getItens() != null
                ? pedido.getItens().stream().map(ItemDTO::new).collect(Collectors.toList())
                : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public LocalDateTime getDataHoraPedido() {
        return dataHoraPedido;
    }

    public LocalDateTime getHorarioEstimadoRetirada() {
        return horarioEstimadoRetirada;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public boolean isPago() {
        return pago;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemDTO> getItens() {
        return itens;
    }
}