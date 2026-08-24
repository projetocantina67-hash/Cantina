package br.com.cantina.Cantina.dto;

import br.com.cantina.Cantina.database.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public class StatusPedidoRequest {
    
    @NotNull(message = "O status do pedido é obrigatório")
    private StatusPedido status;

    public StatusPedidoRequest() {}

    public StatusPedidoRequest(StatusPedido status) {
        this.status = status;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}
