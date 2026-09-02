package br.com.cantina.Cantina.database.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;


public class ItemPedidoDTO {
        @NotNull(message = "O produto é obrigatório")
        private Long produtoId;

        @NotNull(message = "A quantidade é obrigatória")
        @Min(value = 1, message = "A quantidade deve ser de pelo menos 1")
        private Integer quantidade;

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }
}