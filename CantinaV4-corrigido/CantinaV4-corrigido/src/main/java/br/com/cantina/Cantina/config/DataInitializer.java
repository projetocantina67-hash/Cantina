package br.com.cantina.Cantina.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final int TEMPO_PADRAO_MINUTOS = 20;
    private static final int QTDE_PADRAO = 60;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        popularProdutosPadrao();
    }

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // =========================================================================
    // Produtos padrão (IDs 101–108)
    // =========================================================================

    private void popularProdutosPadrao() {
        log.info("--- Verificando produtos padrão (IDs 101–108) ---");

        inserirProduto(101, "PRATO_FEITO", "Tradicional",
                "File de frango grelhado com calabresa ao molho especial da cantina",
                new BigDecimal("25.00"), 15, 80, 80);

        inserirProduto(102, "PRATO_FEITO", "Do Dia",
                "File de frango ou labareda temperados com os condimentos da casa",
                new BigDecimal("28.00"), 15, 80, 80);

        inserirProduto(103, "PRATO_FEITO", "Picadinho de Carne com Legumes",
                "Picadinho de carne bovina com legumes frescos da estacao",
                new BigDecimal("22.00"), TEMPO_PADRAO_MINUTOS, QTDE_PADRAO, QTDE_PADRAO);

        inserirProduto(104, "PRATO_FEITO", "Strogonoff com Batata Palha",
                "Classico strogonoff de frango com batata palha crocante",
                new BigDecimal("24.00"), TEMPO_PADRAO_MINUTOS, QTDE_PADRAO, QTDE_PADRAO);

        inserirProduto(105, "PRATO_FEITO", "Feijoada",
                "Feijoada completa com arroz, couve, farofa e laranja",
                new BigDecimal("26.00"), TEMPO_PADRAO_MINUTOS, QTDE_PADRAO, QTDE_PADRAO);

        inserirProduto(106, "PRATO_FEITO", "Massa com Almondegas",
                "Massa ao molho de tomate caseiro com almondegas grelhadas",
                new BigDecimal("23.00"), TEMPO_PADRAO_MINUTOS, QTDE_PADRAO, QTDE_PADRAO);

        inserirProduto(107, "PRATO_FEITO", "Peixe com Pure",
                "File de peixe grelhado servido com pure de batatas cremoso",
                new BigDecimal("27.00"), TEMPO_PADRAO_MINUTOS, QTDE_PADRAO, QTDE_PADRAO);

        inserirProduto(108, "PRATO_FEITO", "Feijoada Especial de Sabado",
                "Feijoada especial de sabado com todos os acompanhamentos tradicionais",
                new BigDecimal("30.00"), 25, 70, 70);

        jdbcTemplate.execute("ALTER TABLE produto AUTO_INCREMENT = 200");
        log.info("AUTO_INCREMENT da tabela 'produto' ajustado para 200.");
    }

    /**
     * Insere um produto com ID explícito somente se ainda não existir.
     * O uso de {@link JdbcTemplate} (JDBC puro) permite definir o {@code id}
     * sem conflito com a estratégia {@code GenerationType.IDENTITY} do JPA.
     */
    private void inserirProduto(long id, String categoria, String nome, String descricao,
                                BigDecimal preco, int tempoPreparo,
                                int qtdeHoje, int qtdePadrao) {
        Integer existente = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM produto WHERE id = ?", Integer.class, id);

        if (existente == null || existente == 0) {
            jdbcTemplate.update(
                    "INSERT INTO produto " +
                            "  (id, categoria_produto, nome, descricao, preco, " +
                            "   tempo_preparo_minutos, quantidade_disponivel_hoje, " +
                            "   quantidade_padrao_diaria, ativo, version) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, 0)",
                    id, categoria, nome, descricao, preco,
                    tempoPreparo, qtdeHoje, qtdePadrao);
            log.info("  [INSERIDO] Produto #{}: {}", id, nome);
        } else {
            log.info("  [JA EXISTE] Produto #{}: {}", id, nome);
        }
    }
}