package br.com.cantina.Cantina.database.enums;

public enum StatusPedido {
    PENDENTE("Pendente"),
    EM_PREPARO("Em preparo"),
    PRONTO("Pronto"),
    ENTREGUE("Entregue"),
    CANCELADO("Cancelado");

    private final String statusPedido;

    StatusPedido(String s){
        this.statusPedido = s;
    }

    public String getStatusPedido(){
        return statusPedido;
    }


}
