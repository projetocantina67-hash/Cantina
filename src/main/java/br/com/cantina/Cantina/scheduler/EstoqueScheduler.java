package br.com.cantina.Cantina.scheduler;

import br.com.cantina.Cantina.service.ProdutoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class EstoqueScheduler {

    private static final Logger log = LoggerFactory.getLogger(EstoqueScheduler.class);

    private final ProdutoService produtoService;

    public EstoqueScheduler(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void resetarEstoqueDiario() {
        log.info("Iniciando reset diário de estoque dos produtos...");
        produtoService.resetarEstoqueDiario();
        log.info("Reset diário de estoque concluído.");
    }
}
