package buscaweb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 * Classe para capturar recursos da web.
 * 
 * @author Santiago
 */
public class CapturaRecursosWeb {
    private ArrayList<String> listaRecursos = new ArrayList<>();
    
    /**
     * Carrega os recursos a partir das URLs na lista de recursos.
     * 
     * @return Uma lista com o conteúdo das páginas web.
     * @throws IOException se ocorrer um erro ao tentar acessar as URLs.
     */
    public ArrayList<String> carregarRecursos() throws IOException {
        ArrayList<String> resultado = new ArrayList<>();
        for (String stringURL : listaRecursos) {
            String resposta = "";

            try {
                URL url = new URL(stringURL);
                URLConnection connection = url.openConnection();

                // Adicionando o User-Agent para evitar bloqueios (erro 403)
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                        connection.getInputStream()));

                String inputLine;
                StringBuilder sb = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    sb.append(inputLine).append("\n");
                }
                resposta = sb.toString();
                resultado.add(resposta);
                in.close();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return resultado;
    }

    /**
     * @return a lista de recursos (URLs)
     */
    public ArrayList<String> getListaRecursos() {
        return listaRecursos;
    }
}
