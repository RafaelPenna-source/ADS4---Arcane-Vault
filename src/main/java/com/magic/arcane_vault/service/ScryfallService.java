package com.magic.arcane_vault.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.magic.arcane_vault.model.Cards;
import com.magic.arcane_vault.model.EdicaoDTO;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class ScryfallService {

    public Cards buscarCartaNoScryfall(String nomeCarta, String acronimoSet, String numeroColecionador) {
    RestTemplate restTemplate = new RestTemplate();
    ObjectMapper mapper = new ObjectMapper();
    
    try {
        java.net.URI uriFinal;
        System.out.println("================================");
        System.out.println("nomeCarta = " + nomeCarta);
        System.out.println("acronimoSet = " + acronimoSet);
        System.out.println("numeroColecionador = " + numeroColecionador);
        System.out.println("================================");
        // Rota Inteligente: Se já temos o set E o número da variante, busca a impressão exata direto na API
        if (acronimoSet != null && !acronimoSet.trim().isEmpty() && numeroColecionador != null && !numeroColecionador.trim().isEmpty()) {
            uriFinal = UriComponentsBuilder.fromUriString("https://api.scryfall.com/cards/" + acronimoSet.trim().toLowerCase() + "/" + numeroColecionador.trim())
                    .build().toUri();
        } else {
            // Busca padrão por nome caso seja o primeiro carregamento da consulta
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://api.scryfall.com/cards/named")
                    .queryParam("exact", nomeCarta);
            
            if (acronimoSet != null && !acronimoSet.trim().isEmpty()) {
                uriBuilder.queryParam("set", acronimoSet.trim().toLowerCase());
            }
            uriFinal = uriBuilder.build().toUri();
        }
        
        String jsonResposta = restTemplate.getForObject(uriFinal, String.class);
        JsonNode root = mapper.readTree(jsonResposta);
        
        Cards carta = new Cards();

        // Identificadores de Layout para Duas Faces e Rooms
        JsonNode faceNode = root;
        boolean temDuasFaces = root.has("card_faces") && root.path("card_faces").isArray() && root.path("card_faces").size() > 1;
        String layout = root.path("layout").asText("");
        
        if (temDuasFaces) {
            faceNode = root.path("card_faces").get(0);
        }

        // Dados básicos da Raiz
        carta.setCodigoSet(root.path("set").asText("").toLowerCase());
        carta.setCodigoVariante(root.path("collector_number").asText(""));
        carta.setNomeCarta(root.path("name").asText(""));
        carta.setEdicao(root.path("set_name").asText("").toUpperCase());
        
        // --- PROCESSAMENTO DO ORACLE TEXT UNIFICADO (FRENTE E VERSO / PORTAS COMBINADOS) ---
        if (temDuasFaces) {
            JsonNode faceFrente = root.path("card_faces").get(0);
            JsonNode faceVerso = root.path("card_faces").get(1);
            
            String txtFrente = faceFrente.path("name").asText("") + " " + faceFrente.path("mana_cost").asText("") + "\n"
                             + faceFrente.path("type_line").asText("") + "\n"
                             + faceFrente.path("oracle_text").asText("")
                             + (faceFrente.has("power") ? "\n" + faceFrente.path("power").asText() + "/" + faceFrente.path("toughness").asText() : "");
                             
            String txtVerso = faceVerso.path("name").asText("") + " " + faceVerso.path("mana_cost").asText("") + "\n"
                             + (faceVerso.has("color_indicator") ? "Color Indicator: " + faceVerso.path("color_indicator").get(0).asText() + "\n" : "")
                             + faceVerso.path("type_line").asText("") + "\n"
                             + faceVerso.path("oracle_text").asText("")
                             + (faceVerso.has("power") ? "\n" + faceVerso.path("power").asText() + "/" + faceVerso.path("toughness").asText() : "");
                             
            // Customiza a divisória dependendo do layout para ficar elegante no painel
            if ("room".equals(layout)) {
                carta.setTextoRegra(txtFrente + "\n\n=======================================\n🚪 [PORTA] " + txtVerso);
            } else if ("split".equals(layout)) {
                carta.setTextoRegra(txtFrente + "\n\n=======================================\n💥 [OUTRO LADO] " + txtVerso);
            } else {
                carta.setTextoRegra(txtFrente + "\n\n---------------------------------------\n🌗 [VERSO] " + txtVerso);
            }
        } else {
            String oracleText = faceNode.has("oracle_text") ? faceNode.path("oracle_text").asText("") : root.path("oracle_text").asText("");
            carta.setTextoRegra(oracleText);
        }

        // Supertipo baseado na linha de tipo da face frontal
        String typeLine = faceNode.has("type_line") ? faceNode.path("type_line").asText("") : root.path("type_line").asText("");
        String supertipo = "";
        if (typeLine.contains("Legendary")) supertipo = "Lendária";
        else if (typeLine.contains("Basic")) supertipo = "Básica";
        else if (typeLine.contains("Snow")) supertipo = "Neve";
        else if (typeLine.contains("World")) supertipo = "Mundial";
        carta.setSupertipo(supertipo);

        // Poder e Resistência baseados na face frontal ou raiz
        if (faceNode.has("power")) carta.setPoder(faceNode.path("power").asText());
        else if (root.has("power")) carta.setPoder(root.path("power").asText());

        if (faceNode.has("toughness")) carta.setResistencia(faceNode.path("toughness").asText());
        else if (root.has("toughness")) carta.setResistencia(root.path("toughness").asText());

        // --- PROCESSAMENTO INTELIGENTE DA URL DA IMAGEM ---
        // ==========================================
        // URL da Imagem Inteligente (Bala de Prata)
        // ==========================================

        boolean ehRoom = faceNode.path("type_line").asText("").contains("Room");
        if (ehRoom) {

    String urlUnica =
        root.path("image_uris")
            .path("normal")
            .asText("");

    carta.setUrlImagem(urlUnica + "|room");

}
else if (
    root.has("card_faces")
    &&
    root.path("card_faces").get(0).has("image_uris")
) {

    String urlFrente =
        root.path("card_faces")
            .get(0)
            .path("image_uris")
            .path("normal")
            .asText("");

    String urlVerso =
        root.path("card_faces")
            .get(1)
            .path("image_uris")
            .path("normal")
            .asText("");

    carta.setUrlImagem(urlFrente + "|" + urlVerso);

}
else if (root.has("image_uris")) {

    carta.setUrlImagem(
        root.path("image_uris")
            .path("normal")
            .asText("")
    );
}

        // Mapeamento de Tipos robusto
        if (typeLine.contains("Creature")) carta.setIdTipo(1);
        else if (typeLine.contains("Sorcery")) carta.setIdTipo(2);
        else if (typeLine.contains("Instant")) carta.setIdTipo(3);
        else if (typeLine.contains("Artifact")) carta.setIdTipo(4);
        else if (typeLine.contains("Enchantment") || typeLine.contains("Room")) carta.setIdTipo(5); 
        else if (typeLine.contains("Land")) carta.setIdTipo(6);
        else if (typeLine.contains("Planeswalker")) carta.setIdTipo(7);
        else carta.setIdTipo(8);

        // Mapeamento de Raridades
        String rarity = root.path("rarity").asText("");
        if (rarity.equals("common")) carta.setIdRaridade(1);
        else if (rarity.equals("uncommon")) carta.setIdRaridade(2);
        else if (rarity.equals("rare")) carta.setIdRaridade(3);
        else if (rarity.equals("mythic")) carta.setIdRaridade(4);
        else carta.setIdRaridade(1);

        // Custo de Mana da face frontal
        String manaCost = faceNode.has("mana_cost") ? faceNode.path("mana_cost").asText("") : root.path("mana_cost").asText("");
        carta.setCustoBranco(contarSimbolos(manaCost, "{W}"));
        carta.setCustoAzul(contarSimbolos(manaCost, "{U}"));
        carta.setCustoPreto(contarSimbolos(manaCost, "{B}"));
        carta.setCustoVermelho(contarSimbolos(manaCost, "{R}"));
        carta.setCustoVerde(contarSimbolos(manaCost, "{G}"));
        carta.setCustoIncolor(extrairCustoIncolor(manaCost));
        carta.setQuantidade(1);

        // Busca de edições e mapeamento correto de variantes com número do colecionador
        String printsSearchUri = root.path("prints_search_uri").asText(null);
        java.util.List<EdicaoDTO> listaEdicoes = new java.util.ArrayList<>();
        System.out.println("================================");
        System.out.println("nomeCarta = " + nomeCarta);
        System.out.println("acronimoSet = " + acronimoSet);
        System.out.println("numeroColecionador = " + numeroColecionador);
        System.out.println("================================");
        
        if (printsSearchUri != null && !printsSearchUri.isEmpty()) {
            try {
                java.net.URI uriSegura = java.net.URI.create(printsSearchUri);
                String jsonPrints = restTemplate.getForObject(uriSegura, String.class);
                
                JsonNode printsRoot = mapper.readTree(jsonPrints);
                JsonNode dataArray = printsRoot.path("data");
                
                if (dataArray.isArray()) {
                    for (JsonNode printNode : dataArray) {
                        String sCode = printNode.path("set").asText("");
                        String sName = printNode.path("set_name").asText("");
                        String cNum = printNode.path("collector_number").asText("");
                        if (!sCode.isEmpty()) {
                            listaEdicoes.add(new EdicaoDTO(sCode,sName,cNum));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Falha na sub-busca de prints: " + e.getMessage());
                listaEdicoes.add(new EdicaoDTO(root.path("set").asText(""),root.path("set_name").asText(""),root.path("collector_number").asText("")));
            }
        }
        System.out.println("URI FINAL = " + uriFinal);
        System.out.println("SET = " + carta.getCodigoSet());
        System.out.println("VARIANTE = " + carta.getCodigoVariante());
        carta.setEdicoesDisponiveis(listaEdicoes); 
        System.out.println(root.toPrettyString());       
        return carta;

    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException(e);
    }
}

    private int contarSimbolos(String manaCost, String simbolo) {
        if (manaCost == null || manaCost.isEmpty()) return 0;
        int count = 0;
        int index = 0;
        while ((index = manaCost.indexOf(simbolo, index)) != -1) {
            count++;
            index += simbolo.length();
        }
        return count;
    }

    private int extrairCustoIncolor(String manaCost) {
        if (manaCost == null || manaCost.isEmpty()) return 0;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{(\\d+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(manaCost);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }
}