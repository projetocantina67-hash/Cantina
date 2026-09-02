package br.com.cantina.Cantina.database.enums;

public enum FormaPagamento {

    DEBITO("Débito"),
    CREDITO("Crédito"),
    DINHEIRO("Dinheiro"),
    PIX("PIX");

    private final String formaPagamento;

    FormaPagamento(String s){
        this.formaPagamento = s;
    }

    public String getFormaPagamento(){
        return formaPagamento;
    }

}