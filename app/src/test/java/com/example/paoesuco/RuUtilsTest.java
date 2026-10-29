package com.example.paoesuco;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Testes da lógica do app (rodam no computador, sem emulador).
 * O arquivo src/test/resources/cardapio_exemplo.html é uma cópia real do site do RU (29/10/2026).
 */
public class RuUtilsTest {

    private String lerHtmlExemplo() throws Exception {
        InputStream in = getClass().getClassLoader().getResourceAsStream("cardapio_exemplo.html");
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Test
    public void calcular_valoresCorretos() {
        String r = RuUtils.calcular(5, 10);
        assertTrue(r.contains("Café da manhã: R$ 10,00"));
        assertTrue(r.contains("Almoço/Jantar: R$ 30,00"));
        assertTrue(r.contains("Total a depositar: R$ 40,00"));
    }

    @Test
    public void calcular_zeros() {
        String r = RuUtils.calcular(0, 0);
        assertTrue(r.contains("Café da manhã: R$ 0,00"));
        assertTrue(r.contains("Almoço/Jantar: R$ 0,00"));
        assertTrue(r.contains("Total a depositar: R$ 0,00"));
    }

    @Test
    public void extrairDatas_doSite() throws Exception {
        ArrayList<String> datas = RuUtils.extrairDatas(lerHtmlExemplo());
        assertEquals(4, datas.size());
        assertEquals("2026-10-29", datas.get(0));
        assertEquals("2026-10-30", datas.get(1));
        assertEquals("2026-10-31", datas.get(2));
        assertEquals("2026-11-01", datas.get(3));
    }

    @Test
    public void extrairDatas_nullOuSemDatas() {
        assertTrue(RuUtils.extrairDatas(null).isEmpty());
        assertTrue(RuUtils.extrairDatas("<html></html>").isEmpty());
    }

    @Test
    public void formatarData() {
        assertEquals("31/10/2026", RuUtils.formatarData("2026-10-31"));
    }

    @Test
    public void extrairCardapio_doSite() throws Exception {
        String c = RuUtils.extrairCardapio(lerHtmlExemplo());
        assertNotNull(c);
        assertTrue(c.contains("Strogonoff de frango"));
        assertTrue(c.contains("Strogonoff vegano"));
        assertTrue(c.contains("Vegano"));
        assertTrue(c.contains("JANTAR"));
        assertTrue(c.contains("CAFÉ DA MANHÃ"));
    }

    @Test
    public void extrairCardapio_nullOuInvalido() {
        assertNull(RuUtils.extrairCardapio(null));
        assertNull(RuUtils.extrairCardapio("<html>sem cardapio</html>"));
    }
}
