package com.magic.arcane_vault.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.magic.arcane_vault.model.Cards;
import com.magic.arcane_vault.model.CardsService;

@Controller
public class MainController {

    // CORREÇÃO: Injeção padronizada para funcionar em todos os métodos
    @Autowired
    private CardsService cardsService;

    @Autowired
    private com.magic.arcane_vault.service.ScryfallService scryfallService;

    @GetMapping("/")
    public String index(Model model) {
        int idUsuarioLogado = 1; 
        java.util.Map<String, Object> resumo = cardsService.obterResumoColecao(idUsuarioLogado);
        
        if (resumo != null) {
            model.addAttribute("totalCartas", resumo.get("total_cartas"));
            model.addAttribute("cartasDiferentes", resumo.get("cartas_diferentes"));
            model.addAttribute("rarasMiticas", resumo.get("raras_miticas"));
            
            // Calcula quantas das 5 cores básicas o jogador possui na coleção
            int cores = 0;
            if (((Number) resumo.get("usa_branco")).intValue() > 0) cores++;
            if (((Number) resumo.get("usa_azul")).intValue() > 0) cores++;
            if (((Number) resumo.get("usa_preto")).intValue() > 0) cores++;
            if (((Number) resumo.get("usa_vermelho")).intValue() > 0) cores++;
            if (((Number) resumo.get("usa_verde")).intValue() > 0) cores++;
            model.addAttribute("coresUsadas", cores);
        } else {
            model.addAttribute("totalCartas", 0);
            model.addAttribute("cartasDiferentes", 0);
            model.addAttribute("rarasMiticas", 0);
            model.addAttribute("coresUsadas", 0);
        }
        return "home"; 
    }

    @GetMapping("/colecao")
    public String gerenciarColecao(Model model) {
        int idUsuarioLogado = 1; 

        // Uso direto do atributo injetado
        List<Cards> minhaColecao = cardsService.obterColecaoCompleta(idUsuarioLogado);
        
        model.addAttribute("cartasSalvas", minhaColecao);
        model.addAttribute("novaCarta", new Cards());
        
        return "colecao"; 
    }

    @PostMapping("/colecao/salvar")
    public String salvarCarta(@ModelAttribute Cards novaCarta) {
        int idUsuarioLogado = 1; 
        cardsService.inserirCarta(novaCarta, idUsuarioLogado);
        return "redirect:/colecao#lista-cartas"; 
    }
    
    @PostMapping("/colecao/{id}/excluir")
    public String excluirCarta(@PathVariable int id) {
        cardsService.removerCarta(id); // O 'id' aqui é o idCarta vindo do HTML
        return "redirect:/colecao#lista-cartas";
    }

    @GetMapping("/colecao/editar/{id}")
    public String exibirFormularioEditar(@PathVariable("id") int id, Model model) {
        // CORREÇÃO: Agora 'cardsService' existe no escopo da classe
        Cards cartaExistente = cardsService.obterCartaPorId(id);
        model.addAttribute("cards", cartaExistente); 
        return "editar-carta"; 
    }

    @PostMapping("/colecao/editar/{id}")
    public String processarEdicao(@PathVariable("id") int id, @ModelAttribute("cards") Cards carta) {
        carta.setIdCarta(id); 
        cardsService.atualizarCarta(carta);
        return "redirect:/colecao"; 
    }

    @GetMapping("/colecao/scryfall/buscar")
    @ResponseBody
    public Cards buscarScryfall(@RequestParam("nome") String nome, 
                                @RequestParam(value = "set", required = false) String set,
                                @RequestParam(value = "number", required = false) String number) { // <--- Novo parâmetro
        Cards carta =
            scryfallService.buscarCartaNoScryfall(nome, set, number);
        
        System.out.println("NOME = " + nome);
        System.out.println("SET = " + set);
        System.out.println("NUMBER = " + number);      
        return carta;
    }

    @GetMapping("/colecao/buscar")
    public String buscarColecao(@RequestParam("busca") String busca, Model model) {
    
        int idUsuarioLogado = 1;
    
        List<Cards> resultado;
    
        if (busca == null || busca.isBlank()) {
            resultado = cardsService.obterColecaoCompleta(idUsuarioLogado);
        } else {
            resultado = cardsService.buscarNaColecao(idUsuarioLogado, busca);
        }
    
        model.addAttribute("cartasSalvas", resultado);
        model.addAttribute("novaCarta", new Cards());
        model.addAttribute("busca", busca);
    
        return "colecao";
    }
}