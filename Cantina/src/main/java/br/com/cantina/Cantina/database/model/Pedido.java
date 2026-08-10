package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.StatusPedido;

public class Pedido
{
    private Long id;
    private String pago;
    private String dataHoraPedido;
    private String horarioEstimadoRetirada;
    private String horarioRetiradaReal;
    private double valorTotal;
    private Usuario usuario;
    private StatusPedido statusPedido;


}
