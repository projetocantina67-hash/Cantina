package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.config.security.UsuarioDetails;
import br.com.cantina.Cantina.database.DTO.CadastroUsuarioDTO;
import br.com.cantina.Cantina.database.DTO.UsuarioResponseDTO;
import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.model.Usuario;
import br.com.cantina.Cantina.database.repository.UsuarioRepository;
import br.com.cantina.Cantina.exception.RegistroNaoEncontradoException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public UsuarioResponseDTO cadastrar(CadastroUsuarioDTO dto) {
        String cpfSanitizado = dto.getCpf().replaceAll("[^0-9]", "");

        if (dto.getPerfil() == Perfil.FUNCIONARIO_CANTINA && !requisitanteEhFuncionarioAutenticado()) {
            throw new AccessDeniedException(
                    "Somente um funcionário da cantina já autenticado pode cadastrar outro funcionário da cantina");
        }

        if (usuarioRepository.findByCpf(cpfSanitizado) != null) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com esse CPF");
        }
        if (usuarioRepository.findByEmail(dto.getEmail()) != null) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com esse email");
        }

        String telefoneSanitizado = dto.getTelefone().replaceAll("[^0-9]", "");
        if (usuarioRepository.findByTelefone(telefoneSanitizado) != null) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com esse telefone");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.getNome());
        usuario.setCpf(dto.getCpf());
        usuario.setEmail(dto.getEmail());
        usuario.setTelefone(dto.getTelefone());
        usuario.setPerfil(dto.getPerfil());
        usuario.setSenhaHash(passwordEncoder.encode(dto.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);
        emailService.enviarBoasVindas(salvo);
        return new UsuarioResponseDTO(salvo);
    }

    public UsuarioResponseDTO buscarPorId(Long id) {
        return new UsuarioResponseDTO(buscarEntidadePorId(id));
    }

    public Usuario buscarEntidadePorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Usuário não encontrado"));
    }

    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponseDTO::new);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = buscarEntidadePorId(id);
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    private boolean requisitanteEhFuncionarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof UsuarioDetails details)) {
            return false;
        }
        return details.getUsuario().getPerfil() == Perfil.FUNCIONARIO_CANTINA;
    }
}
