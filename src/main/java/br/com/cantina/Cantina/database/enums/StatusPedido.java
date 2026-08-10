package br.com.cantina.Cantina.database.enums;

public enum StatusPedido {
    CRIADO ("Pedido Criado"),
    EM_PREPARO ("Em Preparo"),
    PRONTO ("Pronto");
    private final String descricao;

    StatusPedido(String s) {
        this.descricao = s;
    }

    public String getDescricao() {
        return descricao;
    }
}
