package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.database.model.ItemPedido;
import br.com.cantina.Cantina.service.ItemPedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/item-pedido")
public class ItemPedidoController {
    private final ItemPedidoService itemPedidoService;

    @Autowired
    public ItemPedidoController(ItemPedidoService itemPedidoService) {
        this.itemPedidoService = itemPedidoService;
    }

    @GetMapping("/listar")
    public ResponseEntity<List<ItemPedido>> listarItemPedido() {
        return ResponseEntity.ok(itemPedidoService.listAll());
    }

    @PostMapping("/criar")
    public ResponseEntity<ItemPedido> criarItemPedido(@RequestBody ItemPedido itemPedido) {
        ItemPedido novoItem = itemPedidoService.criarItemPedido(itemPedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoItem);
    }

    @GetMapping("/editar/{id}")
    public ResponseEntity<ItemPedido> buscarItemPedido(@PathVariable Long id) {
        return ResponseEntity.ok(itemPedidoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemPedido> atualizarItemPedido(@PathVariable Long id, @RequestBody ItemPedido itemPedido) {
        ItemPedido itemAtualizado = itemPedidoService.atualizarItemPedido(id, itemPedido);
        return ResponseEntity.ok(itemAtualizado);
    }

    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Void> deletarItemPedido(@PathVariable Long id) {
        itemPedidoService.excluirItemPedido(id);
        return ResponseEntity.noContent().build();
    }
}