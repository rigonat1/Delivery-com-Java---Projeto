package com.delivery.model;

import java.math.BigDecimal;

public class Produto {

    private Integer id;
    private Integer restauranteId;
    private String nome;
    private String descricao;
    private BigDecimal preco;

    public Produto() {
    }

    public Produto(Integer restauranteId, String nome, String descricao, BigDecimal preco) {
        this.restauranteId = restauranteId;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(Integer restauranteId) {
        this.restauranteId = restauranteId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s | R$ %.2f | %s", id, nome, preco, descricao);
    }
}
