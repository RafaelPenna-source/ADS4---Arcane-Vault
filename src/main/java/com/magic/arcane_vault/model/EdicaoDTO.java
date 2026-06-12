package com.magic.arcane_vault.model;

public class EdicaoDTO {

    private String codigo;
    private String nome;
    private String numero;

    public EdicaoDTO() {
        
    }

    public EdicaoDTO(String codigo, String nome, String numero) {
        this.codigo = codigo;
        this.nome = nome;
        this.numero = numero;
    }

    // getters e setters
    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public String getNumero() {
        return numero;
    }
}
