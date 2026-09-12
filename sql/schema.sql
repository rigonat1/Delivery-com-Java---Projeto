-- =====================================================================
-- Sistema de Delivery de Comida - Script de criação do banco (PostgreSQL)
-- =====================================================================

DROP TABLE IF EXISTS itens_pedido CASCADE;
DROP TABLE IF EXISTS pedidos CASCADE;
DROP TABLE IF EXISTS produtos CASCADE;
DROP TABLE IF EXISTS entregadores CASCADE;
DROP TABLE IF EXISTS clientes CASCADE;
DROP TABLE IF EXISTS restaurantes CASCADE;

-- ---------------------------------------------------------------------
-- Restaurantes
-- ---------------------------------------------------------------------
CREATE TABLE restaurantes (
    id         SERIAL PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL,
    categoria  VARCHAR(60),
    endereco   VARCHAR(200),
    telefone   VARCHAR(20)
);

-- ---------------------------------------------------------------------
-- Produtos (cardápio) - cada produto pertence a UM restaurante (1:N)
-- ---------------------------------------------------------------------
CREATE TABLE produtos (
    id              SERIAL PRIMARY KEY,
    restaurante_id  INTEGER NOT NULL REFERENCES restaurantes(id) ON DELETE CASCADE,
    nome            VARCHAR(120) NOT NULL,
    descricao       VARCHAR(300),
    preco           NUMERIC(10,2) NOT NULL CHECK (preco >= 0)
);

-- ---------------------------------------------------------------------
-- Clientes
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    endereco  VARCHAR(200),
    telefone  VARCHAR(20),
    email     VARCHAR(120)
);

-- ---------------------------------------------------------------------
-- Entregadores - o "status" é o que a regra de negócio vai consultar
-- ---------------------------------------------------------------------
CREATE TABLE entregadores (
    id        SERIAL PRIMARY KEY,
    nome      VARCHAR(120) NOT NULL,
    telefone  VARCHAR(20),
    veiculo   VARCHAR(60),
    status    VARCHAR(20) NOT NULL DEFAULT 'DISPONIVEL'
              CHECK (status IN ('DISPONIVEL', 'EM_ENTREGA', 'OFFLINE'))
);

-- ---------------------------------------------------------------------
-- Pedidos - já guarda o "retrato" financeiro do pedido
-- (subtotal, % de desconto, valor de desconto, taxa e total),
-- assim o relatório não precisa recalcular tudo depois.
-- ---------------------------------------------------------------------
CREATE TABLE pedidos (
    id                    SERIAL PRIMARY KEY,
    cliente_id            INTEGER NOT NULL REFERENCES clientes(id),
    restaurante_id        INTEGER NOT NULL REFERENCES restaurantes(id),
    entregador_id         INTEGER REFERENCES entregadores(id),
    data_hora             TIMESTAMP NOT NULL DEFAULT NOW(),
    status                VARCHAR(25) NOT NULL DEFAULT 'PENDENTE'
        CHECK (status IN ('PENDENTE','CONFIRMADO','EM_PREPARO',
                           'SAIU_PARA_ENTREGA','ENTREGUE','CANCELADO')),
    subtotal              NUMERIC(10,2) NOT NULL,
    percentual_desconto   NUMERIC(5,2)  NOT NULL DEFAULT 0,
    valor_desconto        NUMERIC(10,2) NOT NULL DEFAULT 0,
    taxa_entrega          NUMERIC(10,2) NOT NULL DEFAULT 8.00,
    valor_total           NUMERIC(10,2) NOT NULL
);

-- ---------------------------------------------------------------------
-- Itens do pedido - tabela associativa N:N entre pedidos e produtos
-- Guarda preco_unitario "congelado" no momento da compra.
-- ---------------------------------------------------------------------
CREATE TABLE itens_pedido (
    id              SERIAL PRIMARY KEY,
    pedido_id       INTEGER NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    produto_id      INTEGER NOT NULL REFERENCES produtos(id),
    quantidade      INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario  NUMERIC(10,2) NOT NULL
);

-- ---------------------------------------------------------------------
-- Índices úteis para os relatórios e buscas mais comuns
-- ---------------------------------------------------------------------
CREATE INDEX idx_produtos_restaurante ON produtos(restaurante_id);
CREATE INDEX idx_pedidos_restaurante  ON pedidos(restaurante_id);
CREATE INDEX idx_pedidos_cliente      ON pedidos(cliente_id);
CREATE INDEX idx_itens_pedido         ON itens_pedido(pedido_id);
