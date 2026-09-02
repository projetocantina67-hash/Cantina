package br.com.cantina.Cantina.config.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_BLOQUEIO_MINUTOS = 15;

    private record Tentativas(AtomicInteger contador, Instant bloqueadoAte) {
    }

    private final ConcurrentHashMap<String, AtomicInteger> contadores = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Instant> bloqueios = new ConcurrentHashMap<>();


    public void verificarBloqueio(String cpf) {
        Instant bloqueadoAte = bloqueios.get(cpf);
        if (bloqueadoAte != null) {
            if (Instant.now().isBefore(bloqueadoAte)) {
                throw new LoginBloqueadoException(
                        "Muitas tentativas de login. Tente novamente em alguns minutos.");
            }
            bloqueios.remove(cpf);
            contadores.remove(cpf);
        }
    }

    public void registrarFalha(String cpf) {
        int tentativas = contadores.computeIfAbsent(cpf, k -> new AtomicInteger(0)).incrementAndGet();
        if (tentativas >= MAX_TENTATIVAS) {
            bloqueios.put(cpf, Instant.now().plusSeconds(JANELA_BLOQUEIO_MINUTOS * 60));
        }
    }


    public void registrarSucesso(String cpf) {
        contadores.remove(cpf);
        bloqueios.remove(cpf);
    }
}
