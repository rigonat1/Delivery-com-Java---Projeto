package com.delivery.model;

/**
 * Representa os possíveis estados de um entregador.
 * Usar enum (em vez de String solta) evita erros de digitação
 * e deixa o compilador nos avisar se usarmos um valor inválido.
 */
public enum StatusEntregador {
    DISPONIVEL,
    EM_ENTREGA,
    OFFLINE
}
