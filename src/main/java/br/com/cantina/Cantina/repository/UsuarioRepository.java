package br.com.cantina.Cantina.repository;

import br.com.cantina.Cantina.database.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u WHERE u.nome = :nome")
    Usuario findByNome(@Param("nome") String nome);

    @Query("SELECT u FROM Usuario u WHERE u.cpf = :cpf")
    Usuario findByCpf(@Param("cpf") String cpf);

    @Query("SELECT u FROM Usuario u WHERE u.ativo = :ativo")
    Usuario findByAtivo(@Param("ativo") boolean ativo);

    @Query("SELECT u FROM Usuario u WHERE u.senha = :senha")
    Usuario findBySenha(@Param("senha") String senha);

    @Query("SELECT u FROM Usuario u WHERE u.telefone = :telefone")
    Usuario findByTelefone(@Param("telefone") String telefone);

    @Query("SELECT u FROM Usuario u WHERE u.email = :email")
    Usuario findByEmail(@Param("email") String email);
}
