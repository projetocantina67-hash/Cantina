package br.com.cantina.Cantina.seed;
import br.com.cantina.Cantina.database.enums.Perfil;
import br.com.cantina.Cantina.database.model.Usuario;
import br.com.cantina.Cantina.database.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@Profile("dev")
public class SeedFuncionario implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedFuncionario.class);

    private static final String CPF_SEED = "48916273870";
    private static final String SENHA_SEED = "Funcionario123";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedFuncionario(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCpf(CPF_SEED) != null) {
            log.info("Funcionário seed já existe, nenhuma ação necessária.");
            return;
        }

        Usuario funcionario = new Usuario();
        funcionario.setNome("Funcionario Cantina");
        funcionario.setCpf(CPF_SEED);
        funcionario.setEmail("funcionario@escola.com");
        funcionario.setTelefone("11999998888");
        funcionario.setPerfil(Perfil.FUNCIONARIO_CANTINA);
        funcionario.setSenhaHash(passwordEncoder.encode(SENHA_SEED));

        usuarioRepository.save(funcionario);

        log.info("Funcionário seed criado com sucesso. CPF: {} | Senha: {}", CPF_SEED, SENHA_SEED);
    }
}