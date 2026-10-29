package com.example.paoesuco;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Métodos simples, sem nada do Android, para poderem ser testados no RuUtilsTest.
public class RuUtils {

    //1 valores das refeições no RU (em reais)
    public static final int PRECO_CAFE = 2;
    public static final int PRECO_ALMOCO = 3;

    //2 calcula quanto depositar e devolve o texto que vai para o TextView da calculadora
    public static String calcular(int nCafe, int nAlmoco) {
        float cafe = nCafe * PRECO_CAFE;
        float almoco = nAlmoco * PRECO_ALMOCO;
        float total = cafe + almoco;

        Locale br = Locale.forLanguageTag("pt-BR");   // vírgula como separador decimal
        return String.format(br, "Café da manhã: R$ %.2f", cafe) + "\n"
                + String.format(br, "Almoço/Jantar: R$ %.2f", almoco) + "\n"
                + String.format(br, "Total a depositar: R$ %.2f", total);
    }

    //3 procura no HTML do site as datas disponíveis: <input type="hidden" name="data" value="2026-10-29"/>
    public static ArrayList<String> extrairDatas(String html) {
        ArrayList<String> datas = new ArrayList<>();
        if (html == null) {
            return datas;
        }
        Matcher m = Pattern.compile("name=\"data\" value=\"(\\d{4}-\\d{2}-\\d{2})\"").matcher(html);
        while (m.find()) {
            datas.add(m.group(1));
        }
        return datas;
    }

    //4 "2026-10-29" -> "29/10/2026"
    public static String formatarData(String iso) {
        String[] partes = iso.split("-");
        return partes[2] + "/" + partes[1] + "/" + partes[0];
    }

    //5 recorta só a parte do cardápio do HTML do site e deixa pronta para o Html.fromHtml()
    public static String extrairCardapio(String html) {
        if (html == null) {
            return null;
        }
        int inicio = html.indexOf("<div id=\"dia\">");
        if (inicio < 0) {
            return null;
        }
        int fim = html.indexOf("sujeito a altera", inicio);
        if (fim < 0) {
            fim = html.length();
        } else {
            fim = html.lastIndexOf("<", fim);   // corta antes da tag "<th> Cardápio sujeito..."
        }
        String trecho = html.substring(inicio, fim);

        //5.1 o site separa padrão e vegano só por comentários; trocamos por títulos visíveis
        trecho = trecho.replaceAll("<!--\\s*Cardapio Padr[^>]*-->", "<br><b>&gt; Padrão</b><br>");
        trecho = trecho.replaceAll("<!--\\s*Cardapio Vegano\\s*-->", "<br><b>&gt; Vegano</b><br>");
        //5.2 remove os outros comentários e o cabeçalho "Cardápio Padrão | Cardápio Vegano" do topo
        trecho = trecho.replaceAll("<!--[\\s\\S]*?-->", "");
        trecho = trecho.replaceAll("<div class=\"col-6 h4[^>]*>[^<]*</div>", "");
        //5.3 cada linha da tabela vira uma quebra de linha
        trecho = trecho.replace("</tr>", "<br>");
        //5.4 títulos das refeições em negrito
        trecho = trecho.replace(">ALMOÇO<", "><br><b>ALMOÇO</b><")
                .replace(">JANTAR<", "><br><b>JANTAR</b><")
                .replace(">CAFÉ DA MANHÃ<", "><br><b>CAFÉ DA MANHÃ</b><");
        return trecho;
    }
}
