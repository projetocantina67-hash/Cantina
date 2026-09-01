package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "A categoria do produto é obrigatória")
    @Column(nullable = false, length = 30)
    private CategoriaProduto categoriaProduto;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descricao;

    @NotNull(message = "O preço do produto é obrigatório")
    @Column(nullable = false)
    private BigDecimal preco;

    @NotNull(message = "O tempo de preparo é obrigatório")
    @Min(value = 1, message = "O tempo de preparo deve ser de pelo menos 1 minuto")
    private Integer tempoPreparoMinutos;

    @Min(value = 0, message = "A quantidade não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidadeDisponivelHoje;


    @NotNull(message = "A quantidade padrão diária é obrigatória")
    @Min(value = 0, message = "A quantidade padrão não pode ser negativa")
    @Column(nullable = false)
    private Integer quantidadePadraoDiaria;

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    private Long version;

    public Produto() {

    }

    public Produto(Long id, boolean ativo, Integer quantidadeDisponivelHoje, Integer quantidadePadraoDiaria,
                   Integer tempoPreparoMinutos, BigDecimal preco, String descricao, String nome,
                   CategoriaProduto categoriaProduto) {
        this.id = id;
        this.ativo = ativo;
        this.quantidadeDisponivelHoje = quantidadeDisponivelHoje;
        this.quantidadePadraoDiaria = quantidadePadraoDiaria;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.preco = preco;
        this.descricao = descricao;
        this.nome = nome;
        this.categoriaProduto = categoriaProduto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setQuantidadeDisponivelHoje(Integer quantidadeDisponivelHoje) {
        this.quantidadeDisponivelHoje = quantidadeDisponivelHoje;
    }

    public Integer getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public void setTempoPreparoMinutos(Integer tempoPreparoMinutos) {
        this.tempoPreparoMinutos = tempoPreparoMinutos;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaProduto getCategoriaProduto() {
        return categoriaProduto;
    }

    public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
        this.categoriaProduto = categoriaProduto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Integer getQuantidadeDisponivelHoje() {
        return quantidadeDisponivelHoje;
    }

    public Integer getQuantidadePadraoDiaria() {
        return quantidadePadraoDiaria;
    }

    public void setQuantidadePadraoDiaria(Integer quantidadePadraoDiaria) {
        this.quantidadePadraoDiaria = quantidadePadraoDiaria;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Produto that = (Produto) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
