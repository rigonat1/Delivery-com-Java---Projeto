package com.delivery.model;

/** Representa o ciclo de vida de um pedido, do início ao fim. */
public enum StatusPedido {
    PENDENTE,
    CONFIRMADO,
    EM_PREPARO,
    SAIU_PARA_ENTREGA,
    ENTREGUE,
    CANCELADO
}
