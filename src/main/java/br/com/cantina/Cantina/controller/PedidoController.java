package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.config.security.SecurityUtils;
import br.com.cantina.Cantina.database.DTO.CriarPedidoDTO;
import br.com.cantina.Cantina.database.DTO.PedidoResponseDTO;
import br.com.cantina.Cantina.database.enums.StatusPedido;
import br.com.cantina.Cantina.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/usuarios/{usuarioId}/pedidos")
    public ResponseEntity<PedidoResponseDTO> criarPedido(@PathVariable Long usuarioId,
                                                           @Valid @RequestBody CriarPedidoDTO dto) {
        exigirDonoOuFuncionario(usuarioId);
        PedidoResponseDTO pedido = pedidoService.criarPedido(usuarioId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/pedidos/{id}")
    public ResponseEntity<PedidoResponseDTO> buscarPorId(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        exigirDonoOuFuncionario(pedido.getUsuarioId());
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/usuarios/{usuarioId}/pedidos")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorUsuario(@PathVariable Long usuarioId) {
        exigirDonoOuFuncionario(usuarioId);
        return ResponseEntity.ok(pedidoService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/pedidos")
    public ResponseEntity<Page<PedidoResponseDTO>> listar(
            @RequestParam(required = false) StatusPedido status,
            @PageableDefault(size = 20, sort = "dataHoraPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(pedidoService.listar(status, pageable));
    }

    @PatchMapping("/pedidos/{id}/status")
    public ResponseEntity<PedidoResponseDTO> atualizarStatus(@PathVariable Long id,
                                                               @RequestParam StatusPedido status) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, status));
    }

    @PatchMapping("/pedidos/{id}/pagamento")
    public ResponseEntity<PedidoResponseDTO> marcarPagamento(@PathVariable Long id,
                                                               @RequestParam boolean pago) {
        return ResponseEntity.ok(pedidoService.marcarPagamento(id, pago));
    }

    @PatchMapping("/pedidos/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id) {
        PedidoResponseDTO pedido = pedidoService.buscarPorId(id);
        exigirDonoOuFuncionario(pedido.getUsuarioId());
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, StatusPedido.CANCELADO));
    }

    private void exigirDonoOuFuncionario(Long usuarioIdDoRecurso) {
        if (!SecurityUtils.isDonoOuFuncionario(usuarioIdDoRecurso)) {
            throw new AccessDeniedException("Você só pode acessar os próprios pedidos");
        }
    }
}
