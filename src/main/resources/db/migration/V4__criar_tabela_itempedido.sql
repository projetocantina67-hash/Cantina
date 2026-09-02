CREATE TABLE item_pedido
(

    id              BIGINT          NOT NULL AUTO_INCREMENT,
    pedido_id       BIGINT          NOT NULL,
    produto_id      BIGINT          NOT NULL,
    quantidade      INT             NOT NULL,
    preco_unitario  DECIMAL(10,2)   NOT NULL,

    PRIMARY KEY (id),
    CONSTRAINT fk_item_pedido_pedido
    FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_item_pedido_produto
    FOREIGN KEY (produto_id) REFERENCES produto (id)

) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
