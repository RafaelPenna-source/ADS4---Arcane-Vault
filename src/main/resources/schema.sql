-- =========================================================
-- Banco PostgreSQL para coleção de cartas de Magic
-- Projeto: Arcane Vault
-- =========================================================

-- =========================================================
-- FUNÇÕES AUXILIARES
-- =========================================================



-- =========================================================
-- 1. USUÁRIOS
-- =========================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha_hash TEXT NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 2. COLEÇÕES
-- =========================================================

CREATE TABLE IF NOT EXISTS colecoes (
    id_colecao SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_colecoes_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON DELETE CASCADE,

    CONSTRAINT uk_colecao_usuario_nome
        UNIQUE (id_usuario, nome)
);

CREATE INDEX IF NOT EXISTS idx_colecoes_usuario
ON colecoes(id_usuario);

-- =========================================================
-- 3. RARIDADES
-- =========================================================

CREATE TABLE IF NOT EXISTS raridades (
    id_raridade SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

INSERT INTO raridades (id_raridade, nome)
VALUES
(1, 'Comum'),
(2, 'Incomum'),
(3, 'Rara'),
(4, 'Mítica'),
(5, 'Especial'),
(6, 'Token'),
(7, 'Promo')
ON CONFLICT (id_raridade) DO NOTHING;

-- =========================================================
-- 4. TIPOS DE CARTAS
-- =========================================================

CREATE TABLE IF NOT EXISTS tipos_cartas (
    id_tipo SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

INSERT INTO tipos_cartas (id_tipo, nome)
VALUES
(1, 'Criatura'),
(2, 'Feitiço'),
(3, 'Mágica Instantânea'),
(4, 'Artefato'),
(5, 'Encantamento'),
(6, 'Terreno'),
(7, 'Planeswalker'),
(8, 'Batalha')
ON CONFLICT (id_tipo) DO NOTHING;

-- =========================================================
-- 5. CARTAS (Removida a url_imagem)
-- =========================================================
CREATE TABLE IF NOT EXISTS cartas (
    id_carta SERIAL PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    nome_normalizado VARCHAR(255) NOT NULL,
    id_raridade INTEGER NOT NULL,
    id_tipo INTEGER NOT NULL,
    custo_branco INTEGER NOT NULL DEFAULT 0 CHECK (custo_branco >= 0),
    custo_azul INTEGER NOT NULL DEFAULT 0 CHECK (custo_azul >= 0),
    custo_preto INTEGER NOT NULL DEFAULT 0 CHECK (custo_preto >= 0),
    custo_vermelho INTEGER NOT NULL DEFAULT 0 CHECK (custo_vermelho >= 0),
    custo_verde INTEGER NOT NULL DEFAULT 0 CHECK (custo_verde >= 0),
    custo_incolor INTEGER NOT NULL DEFAULT 0 CHECK (custo_incolor >= 0),
    texto_regra TEXT,
    supertipo VARCHAR(100),
    poder VARCHAR(10),
    resistencia VARCHAR(10),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    busca TSVECTOR,

    CONSTRAINT fk_cartas_raridade FOREIGN KEY (id_raridade) REFERENCES raridades(id_raridade) ON DELETE RESTRICT,
    CONSTRAINT fk_cartas_tipo FOREIGN KEY (id_tipo) REFERENCES tipos_cartas(id_tipo) ON DELETE RESTRICT,
    CONSTRAINT uk_cartas_unica UNIQUE (
        nome_normalizado, id_raridade, id_tipo, custo_branco, custo_azul, custo_preto, custo_vermelho, custo_verde, custo_incolor
    )
);

CREATE INDEX IF NOT EXISTS idx_cartas_nome
ON cartas(nome);

CREATE INDEX IF NOT EXISTS idx_cartas_nome_normalizado
ON cartas(nome_normalizado);

CREATE INDEX IF NOT EXISTS idx_cartas_raridade
ON cartas(id_raridade);

CREATE INDEX IF NOT EXISTS idx_cartas_tipo
ON cartas(id_tipo);

CREATE INDEX IF NOT EXISTS idx_cartas_busca
ON cartas USING GIN(busca);

-- =========================================================
-- 6. COLEÇÃO CARTAS (Adicionada url_imagem e ajustada a UK)
-- =========================================================
CREATE TABLE IF NOT EXISTS colecao_cartas (
    id_colecao_carta SERIAL PRIMARY KEY,
    id_colecao INTEGER NOT NULL,
    id_carta INTEGER NOT NULL,
    quantidade INTEGER NOT NULL DEFAULT 1 CHECK (quantidade > 0),
    edicao VARCHAR(255),
    codigo_set VARCHAR(50),
    codigo_variante VARCHAR(50),
    url_imagem TEXT, -- Imagem agora pertence à cópia física que você possui
    observacoes TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_colecao_cartas_colecao FOREIGN KEY (id_colecao) REFERENCES colecoes(id_colecao) ON DELETE CASCADE,
    CONSTRAINT fk_colecao_cartas_carta FOREIGN KEY (id_carta) REFERENCES cartas(id_carta) ON DELETE CASCADE,
    
    -- Ajuste fino: Adicionado codigo_variante para permitir artes diferentes do mesmo set
    CONSTRAINT uk_colecao_carta UNIQUE (id_colecao, id_carta, edicao, codigo_variante)
);

CREATE INDEX IF NOT EXISTS idx_colecao_cartas_colecao
ON colecao_cartas(id_colecao);

CREATE INDEX IF NOT EXISTS idx_colecao_cartas_carta
ON colecao_cartas(id_carta);

-- =========================================================
-- 7. TRIGGERS
-- =========================================================
INSERT INTO usuarios (id_usuario, nome, email, senha_hash)
VALUES (1, 'admin', 'admin@local', 'x')
ON CONFLICT (id_usuario) DO NOTHING;

INSERT INTO colecoes (id_colecao, id_usuario, nome)
VALUES (1, 1, 'Coleção Padrão')
ON CONFLICT (id_colecao) DO NOTHING;
-- =========================================================
-- 8. VIEW vw_colecao_completa (Ajustado o alias da url_imagem)
-- =========================================================
CREATE OR REPLACE VIEW vw_colecao_completa AS
SELECT
    cc.id_colecao_carta,
    co.id_colecao,
    u.id_usuario,
    u.nome AS usuario,
    c.id_carta,
    c.nome AS nome_carta,
    c.custo_branco,
    c.custo_azul,
    c.custo_preto,
    c.custo_vermelho,
    c.custo_verde,
    c.custo_incolor,
    TRIM(
        CASE WHEN c.custo_branco > 0 THEN 'Branco: ' || c.custo_branco || ' ' ELSE '' END ||
        CASE WHEN c.custo_azul > 0 THEN 'Azul: ' || c.custo_azul || ' ' ELSE '' END ||
        CASE WHEN c.custo_preto > 0 THEN 'Preto: ' || c.custo_preto || ' ' ELSE '' END ||
        CASE WHEN c.custo_vermelho > 0 THEN 'Vermelho: ' || c.custo_vermelho || ' ' ELSE '' END ||
        CASE WHEN c.custo_verde > 0 THEN 'Verde: ' || c.custo_verde || ' ' ELSE '' END ||
        CASE WHEN c.custo_incolor > 0 THEN 'Incolor: ' || c.custo_incolor || ' ' ELSE '' END
    ) AS custo_mana_texto,
    cc.quantidade,
    cc.edicao,
    c.texto_regra,
    cc.url_imagem, -- Agora a imagem vem de colecao_cartas (cc)
    c.supertipo,
    c.poder,
    c.resistencia,
    c.id_raridade,
    r.nome AS raridade,
    t.nome AS tipo,
    c.id_tipo,
    cc.codigo_set,
    cc.codigo_variante,
    cc.observacoes,
    cc.atualizado_em

FROM colecao_cartas cc
JOIN colecoes co ON co.id_colecao = cc.id_colecao
JOIN usuarios u ON u.id_usuario = co.id_usuario
JOIN cartas c ON c.id_carta = cc.id_carta
JOIN raridades r ON r.id_raridade = c.id_raridade
JOIN tipos_cartas t ON t.id_tipo = c.id_tipo;

CREATE OR REPLACE VIEW vw_resumo_colecao AS
SELECT
    u.id_usuario,
    u.nome AS usuario,

    COUNT(cc.id_colecao_carta) AS cartas_diferentes,

    COALESCE(SUM(cc.quantidade), 0) AS total_cartas,

    SUM(
        CASE
            WHEN r.nome IN ('Rara', 'Mítica')
            THEN cc.quantidade
            ELSE 0
        END
    ) AS raras_miticas,

    SUM(CASE WHEN c.custo_branco > 0 THEN 1 ELSE 0 END) AS usa_branco,
    SUM(CASE WHEN c.custo_azul > 0 THEN 1 ELSE 0 END) AS usa_azul,
    SUM(CASE WHEN c.custo_preto > 0 THEN 1 ELSE 0 END) AS usa_preto,
    SUM(CASE WHEN c.custo_vermelho > 0 THEN 1 ELSE 0 END) AS usa_vermelho,
    SUM(CASE WHEN c.custo_verde > 0 THEN 1 ELSE 0 END) AS usa_verde

FROM usuarios u
LEFT JOIN colecoes co
    ON co.id_usuario = u.id_usuario
LEFT JOIN colecao_cartas cc
    ON cc.id_colecao = co.id_colecao
LEFT JOIN cartas c
    ON c.id_carta = cc.id_carta
LEFT JOIN raridades r
    ON r.id_raridade = c.id_raridade

GROUP BY u.id_usuario, u.nome;