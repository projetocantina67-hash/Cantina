package br.com.cantina.Cantina.config.security;

import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.model.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || !(auth.getPrincipal() instanceof UsuarioDetails details)) {
            throw new AccessDeniedException("Usuário não autenticado");
        }
        return details.getUsuario();
    }

    public static boolean isFuncionarioCantina() {
        try {
            return usuarioAutenticado().getPerfil() == Perfil.FUNCIONARIO_CANTINA;
        } catch (AccessDeniedException e) {
            return false;
        }
    }

    public static boolean isDonoOuFuncionario(Long usuarioIdDoRecurso) {
        Usuario logado = usuarioAutenticado();
        return isFuncionarioCantina() || logado.getId().equals(usuarioIdDoRecurso);
    }
}
