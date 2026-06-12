package com.magic.arcane_vault.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;

@Repository
public class CardsDAO {

    @Autowired
    DataSource dataSource;

    JdbcTemplate jdbc;

    @PostConstruct
    private void initialize() {
        jdbc = new JdbcTemplate(dataSource);
    }


    public void inserirCarta(Cards card, int idColecao) {
        Integer idCartaBanco;

        // 1. Tenta encontrar a carta abstrata no banco
        String sqlBusca = "SELECT id_carta FROM cartas WHERE nome_normalizado = LOWER(?) AND id_raridade = ? AND id_tipo = ? AND custo_branco = ? AND custo_azul = ? AND custo_preto = ? AND custo_vermelho = ? AND custo_verde = ? AND custo_incolor = ?";
        
        try {
            idCartaBanco = jdbc.queryForObject(sqlBusca, Integer.class, 
                card.getNomeCarta(), card.getIdRaridade(), card.getIdTipo(),
                card.getCustoBranco(), card.getCustoAzul(), card.getCustoPreto(),
                card.getCustoVermelho(), card.getCustoVerde(), card.getCustoIncolor()
            );
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            // 2. Não encontrou? Insere a carta base (SEM url_imagem)
            String sqlCarta = "INSERT INTO cartas (nome, nome_normalizado, id_raridade, id_tipo, custo_branco, custo_azul, custo_preto, custo_vermelho, custo_verde, custo_incolor, texto_regra, supertipo, poder, resistencia) " +
                              "VALUES (?, LOWER(?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_carta";
            Object[] objCarta = new Object[]{
                card.getNomeCarta(), card.getNomeCarta(), card.getIdRaridade(), card.getIdTipo(),
                card.getCustoBranco(), card.getCustoAzul(), card.getCustoPreto(),
                card.getCustoVermelho(), card.getCustoVerde(), card.getCustoIncolor(),
                card.getTextoRegra(), card.getSupertipo(), card.getPoder(), card.getResistencia()
            };
            idCartaBanco = jdbc.queryForObject(sqlCarta, Integer.class, objCarta);
        }

        // 3. Insere na sua coleção (COM a url_imagem da variante correta)
        String sqlColecao = "INSERT INTO colecao_cartas (id_colecao, id_carta, quantidade, edicao, codigo_set, codigo_variante, url_imagem, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] objColecao = new Object[]{
            idColecao,
            idCartaBanco, 
            card.getQuantidade(),
            card.getEdicao(),
            card.getCodigoSet(),
            card.getCodigoVariante(),
            card.getUrlImagem(), // Imagem associada diretamente à sua cópia
            card.getObservacoes()
        };
        jdbc.update(sqlColecao, objColecao);
    }

    public List<Cards> obterColecaoCompleta(int idUsuario) {
        // Usa a View definida no seu SQL para facilitar o select
        String sql = "SELECT * FROM vw_colecao_completa WHERE id_usuario = ?";
        List<Map<String, Object>> listaRegistros = jdbc.queryForList(sql, idUsuario);
        ArrayList<Cards> aux = new ArrayList<>();
        
        for (Map<String, Object> registro : listaRegistros) {
            aux.add(Cards.converterRegistros(registro));
        }
        return aux;
    }
    
    // CORREÇÃO: O HTML envia o ID da carta, portanto o delete deve buscar por id_carta
    public void removerDaColecao(int idCarta) {
        String sql = "DELETE FROM colecao_cartas WHERE id_carta = ?";
        jdbc.update(sql, idCarta);
    }

    public Cards obterCartaPorId(int id) {
        String sql = "SELECT * FROM vw_colecao_completa WHERE id_carta = ?";
        Map<String, Object> registro = jdbc.queryForMap(sql, id);
        return Cards.converterRegistros(registro);
    }

    // Atualizado para refletir a nova separação dos dados
    public void atualizarCarta(Cards carta) {
        String sqlCartas = "UPDATE cartas SET nome = ?, id_raridade = ?, id_tipo = ?, "
                + "custo_branco = ?, custo_azul = ?, custo_preto = ?, custo_vermelho = ?, "
                + "custo_verde = ?, custo_incolor = ?, texto_regra = ?, supertipo = ?, poder = ?, resistencia = ? WHERE id_carta = ?";
        
        jdbc.update(sqlCartas, 
                carta.getNomeCarta(), carta.getIdRaridade(), carta.getIdTipo(),
                carta.getCustoBranco(), carta.getCustoAzul(), carta.getCustoPreto(), carta.getCustoVermelho(),
                carta.getCustoVerde(), carta.getCustoIncolor(), carta.getTextoRegra(), 
                carta.getSupertipo(), carta.getPoder(), carta.getResistencia(), carta.getIdCarta());

        // A url_imagem também deve ser atualizada aqui na coleção caso você troque de edição
        String sqlColecao = "UPDATE colecao_cartas SET quantidade = ?, edicao = ?, observacoes = ?, url_imagem = ? WHERE id_carta = ?";
        jdbc.update(sqlColecao, carta.getQuantidade(), carta.getEdicao(), carta.getObservacoes(), carta.getUrlImagem(), carta.getIdCarta());
    }
    
    // NOVO MÉTODO: Coleta estatísticas da view vw_resumo_colecao
    public Map<String, Object> obterResumoColecao(int idUsuario) {
        String sql = "SELECT * FROM vw_resumo_colecao WHERE id_usuario = ?";
        try {
            return jdbc.queryForMap(sql, idUsuario);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Cards> buscarNaColecao(int idUsuario, String busca) {
        String sql = """
            SELECT * 
            FROM vw_colecao_completa
            WHERE id_usuario = ?
            AND LOWER(nome_carta) LIKE LOWER(?)
        """;
    
        List<Map<String, Object>> lista =
            jdbc.queryForList(sql, idUsuario, "%" + busca + "%");
    
        List<Cards> resultado = new ArrayList<>();
    
        for (Map<String, Object> r : lista) {
            resultado.add(Cards.converterRegistros(r));
        }
    
        return resultado;
    }
}