package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.database.DTO.CadastroProdutoDTO;
import br.com.cantina.Cantina.database.DTO.ProdutoResponseDTO;
import br.com.cantina.Cantina.database.enums.CategoriaProduto;
import br.com.cantina.Cantina.database.model.Produto;
import br.com.cantina.Cantina.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) CategoriaProduto categoria,
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        Page<Produto> produtos = produtoService.listar(categoria, pageable);
        return ResponseEntity.ok(produtos.map(ProdutoResponseDTO::new));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(new ProdutoResponseDTO(produtoService.buscarPorId(id)));
    }

    @GetMapping("/todos")
    public ResponseEntity<Page<ProdutoResponseDTO>> listarTodosParaAdmin(
            @RequestParam(required = false) CategoriaProduto categoria,
            @PageableDefault(size = 50, sort = "nome") Pageable pageable) {
        Page<Produto> produtos = produtoService.listarParaAdmin(categoria, pageable);
        return ResponseEntity.ok(produtos.map(ProdutoResponseDTO::new));
    }

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> cadastrar(@Valid @RequestBody CadastroProdutoDTO dto) {
        Produto produto = produtoService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ProdutoResponseDTO(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @Valid @RequestBody CadastroProdutoDTO dto) {
        return ResponseEntity.ok(new ProdutoResponseDTO(produtoService.atualizar(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<ProdutoResponseDTO> ativar(@PathVariable Long id) {
        return ResponseEntity.ok(new ProdutoResponseDTO(produtoService.ativar(id)));
    }

    @PostMapping("/resetar-estoque")
    public ResponseEntity<Void> resetarEstoque() {
        produtoService.resetarEstoqueDiario();
        return ResponseEntity.noContent().build();
    }
}
