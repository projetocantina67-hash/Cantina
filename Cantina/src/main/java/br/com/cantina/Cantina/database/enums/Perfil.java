package br.com.cantina.Cantina.database.enums;

public enum Perfil {
    ALUNO ("Aluno"),
    SECRETARIA ("Secretaria"),
    VISITANTE ("Visitante"),
    PROFESSOR ("professor"),
    FUNCIONARIO_CANTINA ("funcionario");

    private final String perfil;

    Perfil (String s){
        this.perfil = s;
    }

    public String getPerfil(){
        return perfil;
    }
}
