package br.com.cantina.Cantina.database.DTO;

import br.com.cantina.Cantina.database.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public class CriarPedidoDTO {
        @NotNull(message = "O horário estimado de retirada é obrigatório")
        @Future(message = "O horário de retirada deve ser no futuro")
        private LocalDateTime horarioEstimadoRetirada;

        @NotNull(message = "A forma de pagamento é obrigatória")
        private FormaPagamento formaPagamento;

        @NotEmpty(message = "O pedido deve ter pelo menos um item")
        @Valid
        private List<ItemPedidoDTO> itens;

        public LocalDateTime getHorarioEstimadoRetirada() {
                return horarioEstimadoRetirada;
        }

        public void setHorarioEstimadoRetirada(LocalDateTime horarioEstimadoRetirada) {
                this.horarioEstimadoRetirada = horarioEstimadoRetirada;
        }

        public FormaPagamento getFormaPagamento() {
                return formaPagamento;
        }

        public void setFormaPagamento(FormaPagamento formaPagamento) {
                this.formaPagamento = formaPagamento;
        }

        public List<ItemPedidoDTO> getItens() {
                return itens;
        }

        public void setItens(List<ItemPedidoDTO> itens) {
                this.itens = itens;
        }
}