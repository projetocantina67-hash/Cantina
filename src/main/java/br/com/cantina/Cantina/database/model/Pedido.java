package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

    @Min(value = 0, message = "O valor total não pode ser negativo")
    @Column(nullable = false)
    private double valorTotal;

    @NotNull(message = "O usuário é obrigatório")
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Pedido() {
    }

    public Pedido(Long id, StatusPedido status, String dataHoraPedido, String horarioEstimadoRetirada, double valorTotal, Usuario usuario) {
        this.id = id;
        this.status = status;
        this.dataHoraPedido = dataHoraPedido;
        this.horarioEstimadoRetirada = horarioEstimadoRetirada;
        this.valorTotal = valorTotal;
        this.usuario = usuario;
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

    public double getValorTotal() {
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

}