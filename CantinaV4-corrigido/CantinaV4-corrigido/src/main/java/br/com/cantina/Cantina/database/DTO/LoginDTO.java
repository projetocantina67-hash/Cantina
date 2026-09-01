package br.com.cantina.Cantina.database.DTO;

import jakarta.validation.constraints.NotBlank;

public class LoginDTO {

    @NotBlank(message = "O CPF é obrigatório")
    private String cpf;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
