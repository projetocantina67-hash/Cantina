package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;

public class Pedido {
    private Long id;
    private String pago;
    private String dataHoraPedido;
    private String horarioEstimadoRetirada;
    private String horarioRetiradaReal;
    private double valorTotal;
    private Usuario usuario;
    private StatusPedido statusPedido;



    public Pedido(Long id, String pago, String dataHoraPedido, String horarioEstimadoRetirada, String horarioRetiradaReal, double valorTotal, Usuario usuario, StatusPedido statusPedido) {
        this.id = id;
        this.pago = pago;
        this.dataHoraPedido = dataHoraPedido;
        this.horarioEstimadoRetirada = horarioEstimadoRetirada;
        this.horarioRetiradaReal = horarioRetiradaReal;
        this.valorTotal = valorTotal;
        this.usuario = usuario;
        this.statusPedido = statusPedido;
    }

    public Long getId() {
        return id;
    }

    public String getPago() {
        return pago;
    }

    public String getDataHoraPedido() {
        return dataHoraPedido;
    }

    public String getHorarioEstimadoRetirada() {
        return horarioEstimadoRetirada;
    }

    public String getHorarioRetiradaReal() {
        return horarioRetiradaReal;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public StatusPedido getStatusPedido() {
        return statusPedido;
    }
}
