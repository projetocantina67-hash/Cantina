package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.FormaPagamento;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O status do pedido é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido statusPedido;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL)
    private List<ItemPedido> itens = new ArrayList<>();

    @NotNull(message = "A data e hora do pedido é obrigatória")
    @Column(nullable = false)
    private LocalDateTime dataHoraPedido;

    @Min(value = 0, message = "O valor total não pode ser negativo")
    @Column(nullable = false)
    private BigDecimal valorTotal;

    @NotNull(message = "O horário estimado de retirada é obrigatório")
    @Column(nullable = false)
    private LocalDateTime horarioEstimadoRetirada;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A forma de pagamento é obrigatória")
    @Column(nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @Column(nullable = false)
    private boolean pago = false;

    public boolean isPago() {
        return pago;
    }

    public void setPago(boolean pago) {
        this.pago = pago;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }

    public void setStatusPedido(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public Pedido() {

    }

    public Pedido(Long id, LocalDateTime horarioEstimadoRetirada, LocalDateTime dataHoraPedido,
                  BigDecimal valorTotal, List<ItemPedido> itens, Usuario usuario, StatusPedido statusPedido) {
        this.id = id;
        this.horarioEstimadoRetirada = horarioEstimadoRetirada;
        this.dataHoraPedido = dataHoraPedido;
        this.valorTotal = valorTotal;
        this.itens = itens;
        this.usuario = usuario;
        this.statusPedido = statusPedido;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getHorarioEstimadoRetirada() {
        return horarioEstimadoRetirada;
    }

    public void setHorarioEstimadoRetirada(LocalDateTime horarioEstimadoRetirada) {
        this.horarioEstimadoRetirada = horarioEstimadoRetirada;
    }

    public LocalDateTime getDataHoraPedido() {
        return dataHoraPedido;
    }

    public void setDataHoraPedido(LocalDateTime dataHoraPedido) {
        this.dataHoraPedido = dataHoraPedido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public StatusPedido getStatus() {
        return statusPedido;
    }

    public void setStatus(StatusPedido statusPedido) {
        this.statusPedido = statusPedido;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public void adicionarItem(ItemPedido item) {
        item.setPedido(this);
        this.itens.add(item);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Pedido that = (Pedido) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}