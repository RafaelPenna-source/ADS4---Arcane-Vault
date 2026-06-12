package com.magic.arcane_vault.model;

import java.util.List;
import java.util.Map;

public class Cards {

    private Integer idCarta;
    private String nomeCarta;

    private Integer idRaridade;
    private Integer idTipo;

    private int quantidade;
    private String edicao;

    private int custoBranco;
    private int custoAzul;
    private int custoPreto;
    private int custoVermelho;
    private int custoVerde;
    private int custoIncolor;

    private String textoRegra;
    private String observacoes;

    private String urlImagem;
    private String supertipo;
    private String poder;
    private String resistencia;

    private String codigoSet;
    private List<EdicaoDTO> edicoesDisponiveis;
    private String codigoVariante;

    private String raridade;
    private String tipo;

    public Cards() {
    }

    public Integer getIdCarta() {
        return idCarta;
    }

    public void setIdCarta(Integer idCarta) {
        this.idCarta = idCarta;
    }

    public String getNomeCarta() {
        return nomeCarta;
    }

    public void setNomeCarta(String nomeCarta) {
        this.nomeCarta = nomeCarta;
    }

    public Integer getIdRaridade() {
        return idRaridade;
    }

    public void setIdRaridade(Integer idRaridade) {
        this.idRaridade = idRaridade;
    }

    public Integer getIdTipo() {
        return idTipo;
    }

    public void setIdTipo(Integer idTipo) {
        this.idTipo = idTipo;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getEdicao() {
        return edicao;
    }

    public void setEdicao(String edicao) {
        this.edicao = edicao;
    }

    public int getCustoBranco() {
        return custoBranco;
    }

    public void setCustoBranco(int custoBranco) {
        this.custoBranco = custoBranco;
    }

    public int getCustoAzul() {
        return custoAzul;
    }

    public void setCustoAzul(int custoAzul) {
        this.custoAzul = custoAzul;
    }

    public int getCustoPreto() {
        return custoPreto;
    }

    public void setCustoPreto(int custoPreto) {
        this.custoPreto = custoPreto;
    }

    public int getCustoVermelho() {
        return custoVermelho;
    }

    public void setCustoVermelho(int custoVermelho) {
        this.custoVermelho = custoVermelho;
    }

    public int getCustoVerde() {
        return custoVerde;
    }

    public void setCustoVerde(int custoVerde) {
        this.custoVerde = custoVerde;
    }

    public int getCustoIncolor() {
        return custoIncolor;
    }

    public void setCustoIncolor(int custoIncolor) {
        this.custoIncolor = custoIncolor;
    }

    public String getTextoRegra() {
        return textoRegra;
    }

    public void setTextoRegra(String textoRegra) {
        this.textoRegra = textoRegra;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public String getSupertipo() {
        return supertipo;
    }

    public void setSupertipo(String supertipo) {
        this.supertipo = supertipo;
    }

    public String getPoder() {
        return poder;
    }

    public void setPoder(String poder) {
        this.poder = poder;
    }

    public String getResistencia() {
        return resistencia;
    }

    public void setResistencia(String resistencia) {
        this.resistencia = resistencia;
    }

    public String getCodigoSet() {
        return codigoSet;
    }

    public void setCodigoSet(String codigoSet) {
        this.codigoSet = codigoSet;
    }

    public List<EdicaoDTO> getEdicoesDisponiveis() {
        return edicoesDisponiveis;
    }

    public void setEdicoesDisponiveis(List<EdicaoDTO> edicoesDisponiveis) {
        this.edicoesDisponiveis = edicoesDisponiveis;
    }

    public String getCodigoVariante() {
        return codigoVariante;
    }

    public void setCodigoVariante(String codigoVariante) {
        this.codigoVariante = codigoVariante;
    }

    public String getRaridade() {
        return raridade;
    }

    public void setRaridade(String raridade) {
        this.raridade = raridade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    // ==========================================
    // GETTERS INTELIGENTES PARA O FRONTEND
    // ==========================================

    public String getUrlFrente() {
        if (this.urlImagem != null && this.urlImagem.contains("|")) {
            return this.urlImagem.split("\\|")[0];
        }
        return this.urlImagem;
    }

    public String getUrlVerso() {
        if (this.urlImagem != null && this.urlImagem.contains("|")) {
            String verso = this.urlImagem.split("\\|")[1];
            // Se o "verso" for a tag de room, significa que não há imagem no verso
            return verso.equals("room") ? null : verso;
        }
        return null;
    }

    public boolean isCartaDupla() {
        return this.urlImagem != null && this.urlImagem.contains("|") && !this.urlImagem.endsWith("|room");
    }

    public boolean isRoom() {
        return this.urlImagem != null && this.urlImagem.endsWith("|room");
    }

    public static Cards converterRegistros(Map<String, Object> registros) {

        Cards carta = new Cards();

        carta.setIdCarta(((Number) registros.get("id_carta")).intValue());
        carta.setNomeCarta((String) registros.get("nome_carta"));

        carta.setIdRaridade(((Number) registros.get("id_raridade")).intValue());
        carta.setIdTipo(((Number) registros.get("id_tipo")).intValue());

        carta.setCustoBranco(((Number) registros.get("custo_branco")).intValue());
        carta.setCustoAzul(((Number) registros.get("custo_azul")).intValue());
        carta.setCustoPreto(((Number) registros.get("custo_preto")).intValue());
        carta.setCustoVermelho(((Number) registros.get("custo_vermelho")).intValue());
        carta.setCustoVerde(((Number) registros.get("custo_verde")).intValue());
        carta.setCustoIncolor(((Number) registros.get("custo_incolor")).intValue());

        carta.setQuantidade(((Number) registros.get("quantidade")).intValue());

        carta.setEdicao((String) registros.get("edicao"));
        carta.setTextoRegra((String) registros.get("texto_regra"));
        carta.setObservacoes((String) registros.get("observacoes"));

        carta.setUrlImagem((String) registros.get("url_imagem"));
        carta.setSupertipo((String) registros.get("supertipo"));
        carta.setPoder((String) registros.get("poder"));
        carta.setResistencia((String) registros.get("resistencia"));

        carta.setCodigoSet((String) registros.get("codigo_set"));
        carta.setCodigoVariante((String) registros.get("codigo_variante"));

        carta.setRaridade((String) registros.get("raridade"));
        carta.setTipo((String) registros.get("tipo"));

        return carta;
    }
}