package br.com.cantina.Cantina.database.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Perfil {
    PROFESSOR("Professor"),
    ALUNO("Aluno"),
    SECRETARIA("Secretaria"),
    FUNCIONARIO_CANTINA("Funcionario da cantina");

    private final String tipoUsuario;

    Perfil(String s){
        this.tipoUsuario = s;
    }

    @JsonValue
    public String getTipoUsuario(){
        return tipoUsuario;
    }

}
