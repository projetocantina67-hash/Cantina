package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.Perfil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do usuário é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O CPF é obrigatório")
    @Size(min = 11, max = 11, message = "O CPF deve ter 11 dígitos")
    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Size(min = 6, max = 255, message = "A senha deve ter entre 6 e 255 caracteres")
    @Column(nullable = false)
    @JsonIgnore
    private String senha;

    @NotBlank(message = "O telefone é obrigatório")
    @Size(min = 10, max = 11, message = "O telefone deve ter entre 10 e 11 dígitos")
    @Column(nullable = false, length = 11)
    private String telefone;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ser válido")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O perfil é obrigatório")
    @Column(nullable = false)
    private Perfil perfil;

    public Usuario() {

    }

    public Usuario(Long id, String nome, String cpf, String senha,
                   Boolean ativo, String email, String telefone,
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @JsonProperty(value = "senha", access = JsonProperty.Access.WRITE_ONLY)
    public void setSenha(String senha) {
        this.senha = senha;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public void excluir() {
        this.ativo = false;
    }
}