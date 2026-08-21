package br.com.cantina.Cantina.repository;
import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import java.util.List;
import br.com.cantina.Cantina.database.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    @Query("SELECT p FROM Produto p WHERE p.categoriaProduto = :categoria")
    List<Produto> findByCategoria(@Param("categoria") CategoriaProduto categoria);

    @Query("SELECT p FROM Produto p WHERE p.nome = :nome")
    List<Produto> findByNome(@Param("nome") String nome);

    @Query("SELECT p FROM Produto p WHERE p.quantidadeDisponivelHoje = :quantidade")
    List<Produto> findByQuantidadeDisponivelHoje(@Param("quantidade") int quantidade);

    @Query("SELECT p FROM Produto p WHERE p.ativo = :ativo")
    List<Produto> findByAtivo(@Param("ativo") boolean ativo);

    @Query("SELECT p FROM Produto p WHERE p.descricao = :descricao")
    List<Produto> findByDescricao(@Param("descricao") String descricao);

    @Query("SELECT p FROM Produto p WHERE p.tempoPreparoMinutos = :tempoPreparoMinutos")
    List<Produto> findByTempoPreparoMinutos(@Param("tempoPreparoMinutos") String tempoPreparoMinutos);


}
