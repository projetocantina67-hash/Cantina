package br.com.cantina.Cantina.service;

import br.com.cantina.Cantina.database.model.Usuario;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final String TEMPLATE_BOAS_VINDAS = "email/boas-vindas.html";

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String templateBoasVindas;

    public EmailService(JavaMailSender mailSender, @Value("${cantina.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.templateBoasVindas = carregarTemplate(TEMPLATE_BOAS_VINDAS);
    }

    @Async("emailTaskExecutor")
    public void enviarBoasVindas(Usuario usuario) {
        String corpoHtml = templateBoasVindas.replace("{{NOME}}", usuario.getNome());
        enviarHtml(usuario.getEmail(), "Bem-vindo(a) à Cantina!", corpoHtml);
    }

    private void enviarHtml(String destinatario, String assunto, String corpoHtml) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(remetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(corpoHtml, true);
            mailSender.send(mimeMessage);
            log.info("Email '{}' enviado para {}", assunto, destinatario);
        } catch (Exception e) {
            log.warn("Não foi possível enviar o email '{}' para {}: {}", assunto, destinatario, e.getMessage());
        }
    }

    private String carregarTemplate(String caminho) {
        try (InputStream inputStream = new ClassPathResource(caminho).getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Template de email não encontrado: " + caminho, e);
        }
    }
}