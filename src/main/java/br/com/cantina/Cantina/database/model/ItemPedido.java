package br.com.cantina.Cantina.database.model;

public class ItemPedido {
    private Long id;
    private String nome;
    private double preco;

    public ItemPedido(Long id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }
}
