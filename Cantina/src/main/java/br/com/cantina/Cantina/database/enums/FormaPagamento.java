package br.com.cantina.Cantina.database.enums;

public enum FormaPagamento {
    PIX ("Pix"),
    DEBITO ("Debito"),
    CREDITO ("Credito"),
    DINHEIRO ("Dinheiro");

    private final String descricao;

    FormaPagamento (String s){
        this.descricao = s;
    }

    public String getDescricao (){
        return descricao;
    }
}
