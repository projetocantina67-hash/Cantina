package br.com.cantina.Cantina.repository.entity;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;


@Entity
@Table(name = "pedido")
public class PedidoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Enum<StatusPedido> Status;

    @Column
    private LocalDateTime dataHoraPedido;

    @Column
    private String horarioEstimadoRetirada;

    @Column
    private double valorTotal;

    @Column
    private Long usuarioId;
}
