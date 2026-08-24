package br.com.cantina.Cantina.database.enums;

public enum CategoriaProduto {
    BEBIDAS("Bebidas"),
    SALGADOS("Salgados"),
    DOCES("Doces"),
    PRATOS_PRONTOS("Pratos prontos"),
    DESCARTAVEIS("Descartaveis");

    private final String categoria;

    CategoriaProduto(String s){
        this.categoria = s;
    }

    public String getCategoria(){
        return categoria;
    }
}
