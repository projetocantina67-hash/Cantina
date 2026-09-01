package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.config.security.SecurityUtils;
import br.com.cantina.Cantina.database.DTO.ItemPedidoDTO;
import br.com.cantina.Cantina.database.DTO.PedidoResponseDTO;
import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.service.ItemPedidoService;
import br.com.cantina.Cantina.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
public class ItemPedidoController {

    private final ItemPedidoService itemPedidoService;
    private final PedidoService pedidoService;

    public ItemPedidoController(ItemPedidoService itemPedidoService, PedidoService pedidoService) {
        this.itemPedidoService = itemPedidoService;
        this.pedidoService = pedidoService;
    }

    @PostMapping("/pedidos/{pedidoId}/itens")
    public ResponseEntity<PedidoResponseDTO> adicionar(@PathVariable Long pedidoId,
                                                       @Valid @RequestBody ItemPedidoDTO dto) {
        exigirDonoOuFuncionario(itemPedidoService.buscarUsuarioIdDoPedido(pedidoId));
        itemPedidoService.adicionarItemAoPedido(pedidoId, dto);
        PedidoResponseDTO pedidoAtualizado = pedidoService.buscarPorId(pedidoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoAtualizado);
    }

    @PutMapping("/itens/{id}")
    public ResponseEntity<PedidoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody ItemPedidoDTO dto) {
        exigirDonoOuFuncionario(itemPedidoService.buscarUsuarioIdDoPedidoDoItem(id));
        ItemPedido item = itemPedidoService.atualizarItemPedido(id, dto);
        PedidoResponseDTO pedidoAtualizado = pedidoService.buscarPorId(item.getPedido().getId());
        return ResponseEntity.ok(pedidoAtualizado);
    }

    @DeleteMapping("/itens/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        exigirDonoOuFuncionario(itemPedidoService.buscarUsuarioIdDoPedidoDoItem(id));
        itemPedidoService.excluirItemPedido(id);
        return ResponseEntity.noContent().build();
    }

    private void exigirDonoOuFuncionario(Long usuarioIdDoRecurso) {
        if (!SecurityUtils.isDonoOuFuncionario(usuarioIdDoRecurso)) {
            throw new AccessDeniedException("Você só pode acessar os itens dos próprios pedidos");
        }
    }
}
