package com.delivery.model;

public class Restaurante {

    private Integer id;
    private String nome;
    private String categoria;
    private String endereco;
    private String telefone;

    public Restaurante() {
    }

    public Restaurante(String nome, String categoria, String endereco, String telefone) {
        this.nome = nome;
        this.categoria = categoria;
        this.endereco = endereco;
        this.telefone = telefone;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    @Override
    public String toString() {
        return String.format("#%d - %s (%s) | %s | %s",
                id, nome, categoria, endereco, telefone);
    }
}
