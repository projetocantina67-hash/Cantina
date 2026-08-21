package br.com.cantina.Cantina.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.cantina.Cantina.database.model.ItemPedido;

import java.util.List;
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    @Query("SELECT i FROM ItemPedido i WHERE i.pedido.id = :pedidoId")
    List<ItemPedido> findByPedidoId(@Param("pedidoId") Long pedidoId);

    @Query("SELECT i FROM ItemPedido i WHERE i.nome = :nome")
    List<ItemPedido> findByNome(@Param("nome") String nome);

    @Query("SELECT i FROM ItemPedido i WHERE i.preco = :preco")
    List<ItemPedido> findByPreco(@Param("preco") Double preco);
}
