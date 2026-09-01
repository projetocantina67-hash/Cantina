package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.Perfil;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome completo é obrigatório")
    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, length = 60)
    private String senhaHash;

    @CPF(message = "CPF inválido")
    @NotBlank
    @Column(nullable = false, unique = true)
    private String cpf;

    @NotBlank(message = "O email é obrigatório")
    @Email(message = "O email deve ter a estrutura minhaconta@email.com")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "O telefone é obrigatório")
    @Column(nullable = false)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "O perfil é obrigatório")
    @Column(nullable = false)
    private Perfil perfil;

    @OneToMany(mappedBy = "usuario")
    private List<Pedido> pedidos = new ArrayList<>();

    @Column(nullable = false)
    private boolean ativo = true;

    public Usuario() {

    }

    public Usuario(Long id, Perfil perfil, String telefone, String email,
                   String cpf, String nome, String senhaHash) {
        this.id = id;
        this.perfil = perfil;
        this.telefone = telefone;
        this.email = email;
        this.cpf = cpf;
        this.nome = nome;
        this.senhaHash = senhaHash;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone != null ? telefone.replaceAll("[^0-9]", "") : null;
    }
    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<Pedido> pedidos) {
        this.pedidos = pedidos;
    }
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf != null ? cpf.replaceAll("[^0-9]", "") : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Usuario that = (Usuario) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}