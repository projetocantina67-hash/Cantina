package br.com.cantina.Cantina.database.model;

import br.com.cantina.Cantina.database.enums.CategoriaProduto;

public class Produto
{
    private Long id;
    private String nome;
    private String descricao;
    private CategoriaProduto categoriaProduto;
    private String tempoPreparoMinutos;
    private String quantidadeDisponivelHoje;
    private boolean ativo;

    public Produto() {

    }

    public Produto(Long id, String nome, String descricao,
                   CategoriaProduto categoriaProduto, String tempoPreparoMinutos,
                   String quantidadeDisponivelHoje, boolean ativo) {
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

    public String getQuantidadeDisponivelHoje() {
        return quantidadeDisponivelHoje;
    }

    public void setQuantidadeDisponivelHoje(String quantidadeDisponivelHoje) {
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
