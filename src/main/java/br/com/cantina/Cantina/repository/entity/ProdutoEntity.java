package br.com.cantina.Cantina.repository.entity;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import java.time.LocalDateTime;


@Entity
@Table(name = "produto")
public class ProdutoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String nome;

    @Column
    private String descricao;

    @Column
    private CategoriaProduto categoriaProduto;

    @Column
    private String tempoPreparoMinutos;

    @Column
    private int quantidadeDisponivelHoje;

    @Column
    private boolean ativo;
}