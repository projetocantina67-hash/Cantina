package br.com.cantina.Cantina.repository;

import br.com.cantina.Cantina.database.model.Pedido;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.math.BigDecimal;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    @Query("SELECT p FROM Pedido p WHERE p.status = :status")
    List<Pedido> findByStatus(@Param("status") StatusPedido status);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId")
    List<Pedido> findByUsuarioId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT p FROM Pedido p WHERE p.dataHoraPedido = :dataHoraPedido")
    List<Pedido> findByDataHoraPedido(@Param("dataHoraPedido") String dataHoraPedido);

    @Query("SELECT p FROM Pedido p WHERE p.horarioEstimadoRetirada = :horarioEstimadoRetirada")
    List<Pedido> findByHorarioEstimadoRetirada(@Param("horarioEstimadoRetirada") String horarioEstimadoRetirada);

    @Query("SELECT p FROM Pedido p WHERE p.valorTotal = :valorTotal")
    List<Pedido> findByValorTotal(@Param("valorTotal") BigDecimal valorTotal);

    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId AND p.status = :status")
    List<Pedido> findByUsuarioIdAndStatus(@Param("usuarioId") Long usuarioId, @Param("status") StatusPedido status);


}
