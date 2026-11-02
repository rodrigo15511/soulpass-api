-- Sprint 4 - evolucao da TB_USUARIO para o login do Front-End
-- 1) SENHA: guarda o hash BCrypt (60 caracteres) - nunca a senha em texto puro.
--    Fica opcional no banco para nao quebrar cadastros antigos e o CRUD em Python;
--    a obrigatoriedade e validada pela API Java no cadastro.
-- 2) CPF deixa de ser obrigatorio (o login usa e-mail). Continua UNIQUE:
--    no Oracle, varios NULL nao violam a restricao UNIQUE.
ALTER TABLE TB_USUARIO ADD (SENHA VARCHAR2(100));
ALTER TABLE TB_USUARIO MODIFY (CPF NULL);
SELECT COLUMN_NAME, NULLABLE, DATA_TYPE, DATA_LENGTH FROM USER_TAB_COLUMNS WHERE TABLE_NAME='TB_USUARIO' AND COLUMN_NAME IN ('CPF','SENHA');
