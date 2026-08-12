package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.Usuario;
import jakarta.persistence.*;


@Entity(name = "Pedido")
@Table(name = "pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    private String dataHoraPedido;
    private String horarioEstimadoRetirada;
    private double valorTotal;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
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