package br.com.cantina.Cantina.database.repository;

import br.com.cantina.Cantina.database.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByCpf(String cpf);
    Usuario findByEmail(String email);
    Usuario findByTelefone(String telefone);
}
