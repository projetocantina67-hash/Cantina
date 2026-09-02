package br.com.cantina.Cantina.controller;

import br.com.cantina.Cantina.config.security.JwtService;
import br.com.cantina.Cantina.config.security.LoginRateLimiter;
import br.com.cantina.Cantina.config.security.UsuarioDetails;
import br.com.cantina.Cantina.database.DTO.LoginDTO;
import br.com.cantina.Cantina.database.DTO.LoginResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                           LoginRateLimiter loginRateLimiter) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO dto) {
        String cpfSanitizado = dto.getCpf().replaceAll("[^0-9]", "");

        loginRateLimiter.verificarBloqueio(cpfSanitizado);

        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cpfSanitizado, dto.getSenha())
            );

            UsuarioDetails usuarioDetails = (UsuarioDetails) authentication.getPrincipal();
            String token = jwtService.gerarToken(usuarioDetails.getUsuario());

            loginRateLimiter.registrarSucesso(cpfSanitizado);
            return ResponseEntity.ok(new LoginResponseDTO(token, usuarioDetails.getUsuario()));
        } catch (AuthenticationException e) {
            loginRateLimiter.registrarFalha(cpfSanitizado);
            throw new BadCredentialsException("CPF ou senha inválidos");
        }
    }
}

