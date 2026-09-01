package br.com.cantina.Cantina.database.repository;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByDataHoraPedidoDesc(Long usuarioId);

    Page<Pedido> findByStatusPedido(StatusPedido status, Pageable pageable);
}