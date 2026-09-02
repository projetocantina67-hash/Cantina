package br.com.cantina.Cantina.database.enums;

public enum CategoriaProduto {
    SALGADO("Salgado"),
    DOCE("Doce"),
    BEBIDA("Bebida"),
    PRATO_FEITO("Prato Feito");

    private final String categoriaProduto;

    CategoriaProduto(String s){
        this.categoriaProduto = s;
    }


    public String getCategoriaProduto(){
        return categoriaProduto;
    }


}
