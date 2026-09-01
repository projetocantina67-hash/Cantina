CREATE TABLE produto
(
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    categoria_produto           VARCHAR(30)     NOT NULL,
    nome                        VARCHAR(100)    NOT NULL,
    descricao                   VARCHAR(500)    NOT NULL,
    preco                       DECIMAL(10,2)   NOT NULL,
    tempo_preparo_minutos       INT             NOT NULL,
    quantidade_disponivel_hoje  INT             NOT NULL,
    ativo                       BOOLEAN         NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
