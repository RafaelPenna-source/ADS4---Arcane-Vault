// Garante que o script rode apenas após o DOM estar 100% carregado
document.addEventListener("DOMContentLoaded", function() {
    const selectTipo = document.getElementById('idTipo');
    if (selectTipo) {
        selectTipo.addEventListener('change', function() {
            const divAtributos = document.getElementById('atributos-criatura');
            if (this.value === '1') { // ID 1 = Criatura
                divAtributos.style.display = 'grid';
                divAtributos.style.gridTemplateColumns = '1fr 1fr';
                divAtributos.style.gap = '15px';
            } else {
                divAtributos.style.display = 'none';
                document.getElementById('poder').value = '';
                document.getElementById('resistencia').value = '';
            }
        });
    }
});

// Mantém as suas funções de busca do Scryfall intactas...
function importarDoScryfall() {
    const nome = document.getElementById('scryfall-search').value;
    executarBuscaScryfall(nome, '', '');
}

function alterarEdicaoScryfall() {
    console.log("SCRIPT NOVO CARREGADO");
    const nome = document.getElementById('scryfall-search').value;

    const select =
        document.getElementById('scryfall-select-edicao');

    const codigoSetSelecionado =
        select.value;

    const optionSelecionada =
        select.selectedOptions[0];

    if (!optionSelecionada) {
        return;
    }
    const numeroSelecionado =
        optionSelecionada.dataset.number;

    console.log("OPTION:", optionSelecionada);

    console.log(
        "value:",
        optionSelecionada.value
    );

    console.log(
        "dataset.number:",
        optionSelecionada.dataset.number
    );
    executarBuscaScryfall(
        nome,
        codigoSetSelecionado,
        numeroSelecionado
    );
}

function executarBuscaScryfall(nome, setCodigo, numeroColecionador) {
    const status = document.getElementById('scryfall-status');
    if(!nome) {
        status.innerText = "Por favor, digite o nome de uma carta.";
        return;
    }
    status.innerText = "Consultando grimório do Scryfall...";
    let url = `/colecao/scryfall/buscar?nome=${encodeURIComponent(nome)}`;

    if (setCodigo) {
        url += `&set=${encodeURIComponent(setCodigo)}`;
    }
    
    if (numeroColecionador) {
        url += `&number=${encodeURIComponent(numeroColecionador)}`;
    }
    
    console.log("URL Scryfall:", url);

    fetch(url)
        .then(response => { if (!response.ok) throw new Error(); return response.json(); })
        .then(data => {
            if(data) {
                document.getElementById('nomeCarta').value = data.nomeCarta || '';
                document.getElementById('idRaridade').value = data.idRaridade || '';
                document.getElementById('idTipo').value = data.idTipo || '';
                document.getElementById('edicao').value = data.edicao || '';
                document.getElementById('textoRegra').value = data.textoRegra || '';
                document.getElementById('supertipo').value = data.supertipo || '';
                document.getElementById('poder').value = data.poder || '';
                document.getElementById('resistencia').value = data.resistencia || '';
                document.getElementById('urlImagem').value = data.urlImagem || '';
                document.getElementById('codigoSet').value = data.codigoSet || '';
                document.getElementById('codigoVariante').value = data.codigoVariante || '';
                document.getElementById('custoBranco').value = data.custoBranco || 0;
                document.getElementById('custoAzul').value = data.custoAzul || 0;
                document.getElementById('custoPreto').value = data.custoPreto || 0;
                document.getElementById('custoVermelho').value = data.custoVermelho || 0;
                document.getElementById('custoVerde').value = data.custoVerde || 0;
                document.getElementById('custoIncolor').value = data.custoIncolor || 0;

                if(document.getElementById('idTipo')) {
                    document.getElementById('idTipo').dispatchEvent(new Event('change'));
                }

                const containerEdicoes = document.getElementById('container-edicoes');
                const selectEdicao = document.getElementById('scryfall-select-edicao');

                // REGRA 3: Uso de JSON puro para criar os options
                if (!setCodigo && data.edicoesDisponiveis && data.edicoesDisponiveis.length > 0) {
                    selectEdicao.innerHTML = '';
                    data.edicoesDisponiveis.forEach(edicao => {            
                        const option = document.createElement('option');
                        option.value = edicao.codigo;
                        option.dataset.number = edicao.numero;
                        option.innerText = `[${edicao.codigo.toUpperCase()}] — ${edicao.nome} (#${edicao.numero})`;
                        selectEdicao.appendChild(option);
                    });

                    if (containerEdicoes) {
                        containerEdicoes.style.display = 'block';
                    }
                }

                // REGRA 4: Validação com casting rigoroso (String)
                for (const option of selectEdicao.options) {
                    if (
                        option.value === String(data.codigoSet) &&
                        option.dataset.number === String(data.codigoVariante)
                    ) {
                        option.selected = true;
                        break;
                    }
                }
                status.innerText = `Exibindo versão de: ${data.edicao}`;
            } else {
                status.innerText = "Carta não encontrada no Scryfall.";
            }
        }).catch(error => {
            console.error(error);
            status.innerText = "Erro ao conectar com o serviço de busca.";
        });
}

// CORREÇÃO DA FUNÇÃO VIRAR CARTA (Evita bugs com URLs completas/relativas do navegador)
function virarCarta(btn, id) {
    const img = document.getElementById('img-' + id);
    if (img) {
        const frente = btn.getAttribute('data-frente');
        const verso = btn.getAttribute('data-verso');
        if (img.src.includes(frente)) {
            img.src = verso;
        } else {
            img.src = frente;
        }
    }
}

// --- CONTROLE DE ROTAÇÃO E ZOOM DAS ROOMS ---
const estadosRotacao = {};
const estadosZoom = {};

function rotacionarCarta(idCarta) {
    const img = document.getElementById('img-' + idCarta);
    if (!img) return;

    // Se estiver em pé (ou sem estado), gira 90º e aplica zoom 1.4
    if (!estadosRotacao[idCarta] || estadosRotacao[idCarta] === 0) {
        estadosRotacao[idCarta] = 90;
        estadosZoom[idCarta] = 1.4; 
    } else {
        // Se já estiver deitada, volta para o padrão (0º e sem zoom)
        estadosRotacao[idCarta] = 0;
        estadosZoom[idCarta] = 1.0;
    }
    
    aplicarTransformacoes(idCarta, img);
}

function aplicarTransformacoes(idCarta, img) {
    const graus = estadosRotacao[idCarta] || 0;
    const escala = estadosZoom[idCarta] || 1.0;
    
    img.style.transform = `rotate(${graus}deg) scale(${escala})`;
    
    // Zarante que o z-index aumente para o zoom ficar por cima dos outros elementos do grid
    if (graus === 90 || escala > 1) {
        img.style.margin = "35px 0";
        img.style.zIndex = "10";
    } else {
        img.style.margin = "0";
        img.style.zIndex = "1";
    }
}
