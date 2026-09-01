
ALTER TABLE produto
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;


ALTER TABLE produto
    ADD COLUMN quantidade_padrao_diaria INT NOT NULL DEFAULT 0;

UPDATE produto
SET quantidade_padrao_diaria = quantidade_disponivel_hoje;

ALTER TABLE produto
    ALTER COLUMN quantidade_padrao_diaria DROP DEFAULT;
