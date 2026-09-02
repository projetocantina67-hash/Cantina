package br.com.cantina.Cantina.database.repository;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Page<Produto> findByAtivo(boolean ativo, Pageable pageable);

    Page<Produto> findByAtivoAndCategoriaProduto(boolean ativo, CategoriaProduto categoria, Pageable pageable);

    Page<Produto> findByCategoriaProduto(CategoriaProduto categoria, Pageable pageable);

    boolean existsByNome(String nome);

    @Modifying
    @Query("UPDATE Produto p SET p.quantidadeDisponivelHoje = p.quantidadePadraoDiaria WHERE p.ativo = true")
    void resetarEstoqueDeProdutosAtivos();
}
