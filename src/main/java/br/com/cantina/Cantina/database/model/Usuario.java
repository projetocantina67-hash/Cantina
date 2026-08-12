package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.Perfil;
import jakarta.persistence.*;


@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cpf;
    private boolean ativo = true;
    private String senha;
    private String telefone;
    private String email;

    @Enumerated(EnumType.STRING)
    private Perfil perfil;

    public Usuario() {

    }

    public Usuario(Long id, String nome, String cpf, String senha,
                   boolean ativo, String email, String telefone,
                   Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.senha = senha;
        this.ativo = ativo;
        this.email = email;
        this.telefone = telefone;
        this.perfil = perfil;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSenha() {
        return senha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void excluir() {
        this.ativo = false;
    }
}