package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

@Entity(name = "Pedido")
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O status do pedido é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @NotBlank(message = "A data e hora do pedido é obrigatória")
    @Column(nullable = false, length = 50)
    private String dataHoraPedido;

    @NotBlank(message = "O horário estimado de retirada é obrigatório")
    @Column(nullable = false, length = 50)
    private String horarioEstimadoRetirada;

    @NotNull(message = "O valor total do pedido é obrigatório")
    @jakarta.validation.constraints.DecimalMin(value = "0.00", message = "O valor total não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    public Pedido() {
    }

    public Long getId() {
        return id;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getDataHoraPedido() {
        return dataHoraPedido;
    }

    public String getHorarioEstimadoRetirada() {
        return horarioEstimadoRetirada;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<ItemPedido> getItens() {return itens;}

    public void setItens(List<ItemPedido> itens) {this.itens = itens;}

    public void setValorTotal(BigDecimal valorTotal) {this.valorTotal = valorTotal;}
}