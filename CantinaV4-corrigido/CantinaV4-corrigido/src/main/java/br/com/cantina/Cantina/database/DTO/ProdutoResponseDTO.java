package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.model.Produto;

import java.math.BigDecimal;

public class ProdutoResponseDTO {
        private final Long id;
        private final String nome;
        private final String descricao;
        private final BigDecimal preco;
        private final Integer tempoPreparoMinutos;
        private final Integer quantidadeDisponivelHoje;
        private final CategoriaProduto categoriaProduto;
        private final boolean ativo;
        private final Integer quantidadePadraoDiaria;

        public ProdutoResponseDTO(Produto produto) {
            if (produto == null) {
                throw new IllegalArgumentException("Produto não pode ser nulo");
            }
            this.id = produto.getId();
            this.nome = produto.getNome();
            this.descricao = produto.getDescricao();
            this.preco = produto.getPreco();
            this.tempoPreparoMinutos = produto.getTempoPreparoMinutos();
            this.quantidadeDisponivelHoje = produto.getQuantidadeDisponivelHoje();
            this.categoriaProduto = produto.getCategoriaProduto();
            this.ativo = produto.isAtivo();
            this.quantidadePadraoDiaria = produto.getQuantidadePadraoDiaria();
        }

    public Integer getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public CategoriaProduto getCategoriaProduto() {
        return categoriaProduto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public Integer getQuantidadeDisponivelHoje() {
        return quantidadeDisponivelHoje;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNome() {
        return nome;
    }

    public Long getId() {
        return id;
    }

    public Integer getQuantidadePadraoDiaria() { return quantidadePadraoDiaria; }
}