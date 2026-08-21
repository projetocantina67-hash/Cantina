package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "produto")
public class Produto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descricao;
    
    @NotNull(message = "A categoria é obrigatória")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoriaProduto;
    
    @NotBlank(message = "O tempo de preparo é obrigatório")
    @Size(max = 10, message = "O tempo de preparo deve ter no máximo 10 caracteres")
    @Column(nullable = false, length = 10)
    private String tempoPreparoMinutos;

    @Min(value = 0, message = "A quantidade não pode ser negativa")
    @Column(nullable = false)
    private int quantidadeDisponivelHoje;

    @Column(nullable = false)
    private boolean ativo;

    public Produto() {

    }

    public Produto(Long id, String nome, String descricao,
                   CategoriaProduto categoriaProduto, String tempoPreparoMinutos,
                   int quantidadeDisponivelHoje, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.categoriaProduto = categoriaProduto;
        this.tempoPreparoMinutos = tempoPreparoMinutos;
        this.quantidadeDisponivelHoje = quantidadeDisponivelHoje;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getQuantidadeDisponivelHoje() {
        return quantidadeDisponivelHoje;
    }

    public void setQuantidadeDisponivelHoje(int quantidadeDisponivelHoje) {
        this.quantidadeDisponivelHoje = quantidadeDisponivelHoje;
    }

    public String getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public void setTempoPreparoMinutos(String tempoPreparoMinutos) {
        this.tempoPreparoMinutos = tempoPreparoMinutos;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }
}
