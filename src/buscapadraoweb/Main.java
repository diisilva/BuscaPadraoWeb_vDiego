package buscapadraoweb;

import buscaweb.CapturaRecursosWeb;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    // Busca char em vetor e retorna o índice
    public static int get_char_ref(char[] vet, char ref) {
        for (int i = 0; i < vet.length; i++) {
            if (vet[i] == ref) {
                return i;
            }
        }
        return -1;
    }

    // Busca string em vetor e retorna o índice
    public static int get_string_ref(String[] vet, String ref) {
        for (int i = 0; i < vet.length; i++) {
            if (vet[i].equals(ref)) {
                return i;
            }
        }
        return -1;
    }

    // Retorna o próximo estado, dado o estado atual e o símbolo lido
    public static int proximo_estado(char[] alfabeto, int[][] matriz, int estado_atual, char simbolo) {
        int simbol_indice = get_char_ref(alfabeto, simbolo);
        if (simbol_indice != -1) {
            return matriz[estado_atual][simbol_indice];
        } else {
            return -1;
        }
    }

    /*
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Instancia e usa objeto que captura código-fonte de páginas Web
        CapturaRecursosWeb crw = new CapturaRecursosWeb();

        // Adiciona as URLs a serem processadas
        String[] urls = {
            "https://pubmed.ncbi.nlm.nih.gov/?term=artificial%20intelligence",
            "https://api.crossref.org/works?query=artificial%20intelligence",
            "https://ieeexplore.ieee.org/document/9718926",
            "https://link.springer.com/search?query=artificial+intelligence",
            "https://dl.acm.org/doi/10.1145/3690639",
            "https://arxiv.org/abs/2408.16737"
        };

        String[] siteNames = {
            "PubMed",
            "CrossRef",
            "IEEE Xplore",
            /*"ScienceDirect",*/
            "SpringerLink",
            "ACM Digital Library",
            "Arxiv"
        };

        // Mapa do alfabeto: inclui números, letras e caracteres especiais permitidos em DOIs
        char[] alfabeto = new char[]{
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '.', '/', '-', '_', ';', ':', '(', ')'
        };

        // Mapa de estados
        String[] estados = new String[]{
            "q0",    // Estado inicial
            "q1",    // '1'
            "q2",    // '10'
            "q3",    // '10.'
            "q4",    // '10.x' (prefixo)
            "q5",    // '/'
            "q6",    // Sufixo
            "qError" // Estado de erro
        };

        String estado_inicial = "q0";

        // Estados finais
        String[] estados_finais = new String[]{
            "q6"
        };

        // Tabela de transição de AFD para reconhecimento de DOIs
        int[][] matriz = new int[estados.length][alfabeto.length];
        // Inicializa a matriz com -1 (transições inválidas)
        for (int[] row : matriz) {
            Arrays.fill(row, -1);
        }

        // Definindo as transições

        // Transições de q0
        matriz[get_string_ref(estados, "q0")][get_char_ref(alfabeto, '1')] = get_string_ref(estados, "q1");

        // Transições de q1
        matriz[get_string_ref(estados, "q1")][get_char_ref(alfabeto, '0')] = get_string_ref(estados, "q2");

        // Transições de q2
        matriz[get_string_ref(estados, "q2")][get_char_ref(alfabeto, '.')] = get_string_ref(estados, "q3");

        // Transições de q3 (espera pelos dígitos do prefixo antes do '/')
        for (char c : "0123456789".toCharArray()) {
            matriz[get_string_ref(estados, "q3")][get_char_ref(alfabeto, c)] = get_string_ref(estados, "q4");
        }

        // Transições de q4 (continua lendo prefixo ou encontra '/')
        for (char c : "0123456789".toCharArray()) {
            matriz[get_string_ref(estados, "q4")][get_char_ref(alfabeto, c)] = get_string_ref(estados, "q4");
        }
        matriz[get_string_ref(estados, "q4")][get_char_ref(alfabeto, '/')] = get_string_ref(estados, "q5");

        // Transições de q5 (início do sufixo)
        for (int i = 0; i < alfabeto.length; i++) {
            matriz[get_string_ref(estados, "q5")][i] = get_string_ref(estados, "q6");
        }

        // Transições de q6 (continua lendo o sufixo)
        for (int i = 0; i < alfabeto.length; i++) {
            matriz[get_string_ref(estados, "q6")][i] = get_string_ref(estados, "q6");
        }

        try {
            // Processa cada URL
            for (int siteIndex = 0; siteIndex < urls.length; siteIndex++) {
                String url = urls[siteIndex];
                String siteName = siteNames[siteIndex];
                crw.getListaRecursos().clear();
                crw.getListaRecursos().add(url);

                ArrayList<String> listaCodigos;
                try {
                    listaCodigos = crw.carregarRecursos();
                } catch (Exception e) {
                    System.out.println("Erro ao acessar o site: " + siteName + " - URL: " + url);
                    System.out.println("Motivo: " + e.getMessage());
                    System.out.println("Pulando para o próximo site...\n");
                    continue;
                }

                if (listaCodigos.isEmpty()) {
                    System.out.println("Nenhum conteúdo encontrado no site: " + siteName);
                    System.out.println("-------------------------------");
                    continue;
                }

                String codigoHTML = listaCodigos.get(0);

                int estado = get_string_ref(estados, estado_inicial);
                int estado_anterior = -1;
                ArrayList<String> doisEncontrados = new ArrayList<>();
                String palavra = "";

                // Varre o código-fonte procurando por DOIs
                for (int i = 0; i < codigoHTML.length(); i++) {
                    char atual = codigoHTML.charAt(i);
                    estado_anterior = estado;
                    estado = proximo_estado(alfabeto, matriz, estado, atual);

                    if (estado == -1) {
                        // Se não há transição válida, volta ao estado inicial
                        estado = get_string_ref(estados, estado_inicial);
                        // Se o estado anterior foi um estado final
                        if (Arrays.asList(estados_finais).contains(estados[estado_anterior])) {
                            if (!palavra.equals("")) {
                                doisEncontrados.add(palavra);
                            }
                            // Reprocessa o caractere atual no estado inicial
                            i--;
                        }
                        // Zera a palavra
                        palavra = "";
                    } else {
                        // Adiciona o caractere à palavra
                        palavra += atual;
                    }
                }

                // Verifica se a última palavra é um DOI válido
                if (Arrays.asList(estados_finais).contains(estados[estado])) {
                    if (!palavra.equals("")) {
                        doisEncontrados.add(palavra);
                    }
                }

                // Exibe todos os DOIs reconhecidos para cada site
                System.out.println("DOIs encontrados no site: " + siteName);
                if (doisEncontrados.isEmpty()) {
                    System.out.println("Nenhum DOI encontrado.");
                } else {
                    for (String doi : doisEncontrados) {
                        System.out.println(doi);
                    }
                }
                System.out.println("-------------------------------");
            }
        } catch (Exception e) {
            // Captura quaisquer outras exceções que possam ocorrer
            System.out.println("Ocorreu um erro inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
