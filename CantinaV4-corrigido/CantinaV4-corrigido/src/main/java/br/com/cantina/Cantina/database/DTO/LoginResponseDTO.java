package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.model.Usuario;


public class LoginResponseDTO {

    private final String token;
    private final Long usuarioId;
    private final String nome;
    private final Perfil perfil;

    public LoginResponseDTO(String token, Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuário não pode ser nulo");
        }
        this.token = token;
        this.usuarioId = usuario.getId();
        this.nome = usuario.getNome();
        this.perfil = usuario.getPerfil();
    }

    public String getToken() {
        return token;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getNome() {
        return nome;
    }

    public Perfil getPerfil() {
        return perfil;
    }
}
