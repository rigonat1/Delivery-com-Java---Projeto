package com.delivery.service;

/**
 * Lançada quando se tenta atribuir a um pedido um entregador
 * que não está com status DISPONIVEL.
 * Uma exceção própria (em vez de uma genérica) deixa claro,
 * só pelo nome, qual regra de negócio foi violada.
 */
public class EntregadorIndisponivelException extends RuntimeException {
    public EntregadorIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
