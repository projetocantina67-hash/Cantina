package br.com.cantina.Cantina.config.security;

import br.com.cantina.Cantina.database.model.Usuario;
import br.com.cantina.Cantina.database.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String cpf) throws UsernameNotFoundException {
        String cpfSanitizado = cpf.replaceAll("[^0-9]", "");
        Usuario usuario = usuarioRepository.findByCpf(cpfSanitizado);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuário não encontrado para o CPF informado");
        }
        return new UsuarioDetails(usuario);
    }
}
