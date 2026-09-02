package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.model.Usuario;

public class UsuarioResponseDTO {
    private final Long id;
    private final String nome;
    private final String email;
    private final String telefone;
    private final Perfil perfil;

    public UsuarioResponseDTO(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.perfil = usuario.getPerfil();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefone() {
        return telefone;
    }

    public Perfil getPerfil() {
        return perfil;
    }



}
