CREATE TABLE usuario
(
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(255)    NOT NULL,
    senha_hash      VARCHAR(60)     NOT NULL,
    cpf             VARCHAR(255)    NOT NULL,
    email           VARCHAR(255)    NOT NULL,
    telefone        VARCHAR(255)    NOT NULL,
    perfil          VARCHAR(30)     NOT NULL,
    ativo           BOOLEAN         NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuario_cpf (cpf),
    UNIQUE KEY uk_usuario_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
