package com.magic.arcane_vault.model;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardsService {

    @Autowired
    CardsDAO cdao;

    public void inserirCarta(Cards card, int idUsuario) {
        // Para simplificar o projeto acadêmico, estamos assumindo que o idColecao é = idUsuario 
        // (com base na inserção de dados de teste do arquivo SQL).
        cdao.inserirCarta(card, idUsuario);
    }

    public Cards obterCartaPorId(int id) {
        return cdao.obterCartaPorId(id);
    }

    public void atualizarCarta(Cards carta) {
        cdao.atualizarCarta(carta);
    }

    public List<Cards> obterColecaoCompleta(int idUsuario) {
        return cdao.obterColecaoCompleta(idUsuario);
    }
    
    public void removerCarta(int idColecaoCarta) {
        cdao.removerDaColecao(idColecaoCarta);
    }

    public Map<String, Object> obterResumoColecao(int idUsuario) {
        return cdao.obterResumoColecao(idUsuario);
    }

    public List<Cards> buscarNaColecao(int idUsuario, String busca) {
        return cdao.buscarNaColecao(idUsuario, busca);
    }
}