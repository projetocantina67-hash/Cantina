package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.config.security.SecurityUtils;
import br.com.cantina.Cantina.database.DTO.CadastroUsuarioDTO;
import br.com.cantina.Cantina.database.DTO.UsuarioResponseDTO;
import br.com.cantina.Cantina.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }


    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody CadastroUsuarioDTO dto) {
        UsuarioResponseDTO usuario = usuarioService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        exigirDonoOuFuncionario(id);
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarTodos(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        exigirDonoOuFuncionario(id);
        usuarioService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    private void exigirDonoOuFuncionario(Long usuarioIdDoRecurso) {
        if (!SecurityUtils.isDonoOuFuncionario(usuarioIdDoRecurso)) {
            throw new AccessDeniedException("Você só pode acessar a própria conta");
        }
    }
}
