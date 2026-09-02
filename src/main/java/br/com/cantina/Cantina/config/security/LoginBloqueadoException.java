package br.com.cantina.Cantina.config.security;


public class LoginBloqueadoException extends RuntimeException {
    public LoginBloqueadoException(String message) {
        super(message);
    }
}
