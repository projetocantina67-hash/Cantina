package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class CadastroProdutoDTO {

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
    private String nome;

    @NotBlank(message = "A descrição do produto é obrigatória")
    @Size(min = 10, max = 500, message = "A descrição deve ter entre 10 e 500 caracteres")
    private String descricao;

    @NotNull(message = "O preço do produto é obrigatório")
    @Min(value = 0, message = "O preço não pode ser negativo")
    @DecimalMin(value = "0.01", message = "O preço mínimo é R$ 0,01")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter no máximo 2 casas decimais")
    private BigDecimal preco;

    @NotNull(message = "O tempo de preparo é obrigatório")
    @Min(value = 1, message = "O tempo de preparo deve ser de pelo menos 1 minuto")
    @Max(value = 480, message = "O tempo de preparo não pode exceder 8 horas")
    private Integer tempoPreparoMinutos;

    @NotNull(message = "A quantidade padrão diária é obrigatória")
    @Min(value = 0, message = "A quantidade não pode ser negativa")
    @Max(value = 10000, message = "A quantidade padrão não pode exceder 10.000")
    private Integer quantidadePadraoDiaria;

    @NotNull(message = "A categoria do produto é obrigatória")
    private CategoriaProduto categoriaProduto;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getTempoPreparoMinutos() {
        return tempoPreparoMinutos;
    }

    public void setTempoPreparoMinutos(Integer tempoPreparoMinutos) {
        this.tempoPreparoMinutos = tempoPreparoMinutos;
    }

    public Integer getQuantidadePadraoDiaria() {
        return quantidadePadraoDiaria;
    }

    public void setQuantidadePadraoDiaria(Integer quantidadePadraoDiaria) {
        this.quantidadePadraoDiaria = quantidadePadraoDiaria;
    }

    public CategoriaProduto getCategoriaProduto() {
        return categoriaProduto;
    }

    public void setCategoriaProduto(CategoriaProduto categoriaProduto) {
        this.categoriaProduto = categoriaProduto;
    }

}
