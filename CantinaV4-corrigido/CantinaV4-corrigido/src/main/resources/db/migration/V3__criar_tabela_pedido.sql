CREATE TABLE pedido
(
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    status_pedido               VARCHAR(20)     NOT NULL,
    usuario_id                  BIGINT          NOT NULL,
    data_hora_pedido            DATETIME        NOT NULL,
    valor_total                 DECIMAL(10,2)   NOT NULL,
    horario_estimado_retirada   DATETIME        NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
