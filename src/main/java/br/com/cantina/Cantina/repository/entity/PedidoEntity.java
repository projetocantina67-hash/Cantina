package br.com.cantina.Cantina.repository.entity;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.database.model.ItemPedido;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;


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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
}
