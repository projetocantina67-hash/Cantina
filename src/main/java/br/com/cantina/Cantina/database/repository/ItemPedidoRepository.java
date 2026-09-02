package br.com.cantina.Cantina.database.repository;

import br.com.cantina.Cantina.database.model.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
}
