package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;

public class Pedido {
    private Long id;
    private Enum<StatusPedido> Status;
    private String dataHoraPedido;
    private String horarioEstimadoRetirada;
    private double valorTotal;
    private Usuario usuario;




    public Pedido(Long id, Enum<StatusPedido> Status, String dataHoraPedido, String horarioEstimadoRetirada, String horarioRetiradaReal, double valorTotal, Usuario usuario, StatusPedido statusPedido) {
        this.id = id;
        this.Status = Status;
        this.dataHoraPedido = dataHoraPedido;
        this.horarioEstimadoRetirada = horarioEstimadoRetirada;
        this.valorTotal = valorTotal;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public Enum<StatusPedido> getStatus() {
        return Status;
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

}
