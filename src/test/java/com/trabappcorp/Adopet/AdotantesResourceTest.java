package com.trabappcorp.Adopet;

import java.util.Date;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import DAO.UsuarioDAO;
import modelo.Adotante;
import modelo.Doador;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AdotantesResourceTest {

    @Mock
    HttpServletRequest servletRequest;

    @Mock
    UsuarioDAO usuarioDAO;

    @InjectMocks
    AdotantesResource resource;

    MockedStatic<UsuarioDAO> usuarioStatic;
    Adotante adotante;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        usuarioStatic = mockStatic(UsuarioDAO.class);
        usuarioStatic.when(UsuarioDAO::getInstance).thenReturn(usuarioDAO);

        adotante = new Adotante("ana", "123", null, "Ana", "adotante",
                "12345678900", new Date());
        when(servletRequest.getAttribute("usuario")).thenReturn(adotante);
    }

    @AfterEach
    @SuppressWarnings("unused")
    void tearDown() {
        usuarioStatic.close();
    }

    private void preencherFiltro() {
        adotante.getFiltro().setAlturaMenor(50.0);
        adotante.getFiltro().setGastoMensalMenor(300.0);
        adotante.getFiltro().setPesoMenor(20.0);
        adotante.getFiltro().setRaca("Vira-lata");
        adotante.getFiltro().setPorte("medio");
        adotante.getFiltro().setEspecie("cachorro");
    }

    // Testes unitários para o método updateFiltro

    @Test
    @DisplayName("CT-AR-01: Usuário não adotante")
    void updateFiltroRejeitaNaoAdotante() {
        Doador doador = new Doador("bia", "123", null, "Bia", "doador",
                "98765432100", new Date());
        when(servletRequest.getAttribute("usuario")).thenReturn(doador);

        Response response = resource.updateFiltro(null, null, null, "Poodle", null, null);

        assertEquals(401, response.getStatus());
        JsonObject body = (JsonObject) response.getEntity();
        assertEquals("Usuário autenticado não é um adotante", body.getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-02: Texto válido")
    void updateFiltroAtualizaCampoDeTextoValido() {
        Response response = resource.updateFiltro(null, null, null, "Poodle", null, null);

        assertEquals(200, response.getStatus());
        assertEquals("Poodle", adotante.getFiltro().getRaca());
        verify(usuarioDAO).persist(adotante);
    }

    @Test
    @DisplayName("CT-AR-03: Sentinela de limpeza (texto)")
    void updateFiltroLimpaCampoDeTexto() {
        adotante.getFiltro().setRaca("Poodle");

        Response response = resource.updateFiltro(null, null, null, "null", null, null);

        assertEquals(200, response.getStatus());
        assertNull(adotante.getFiltro().getRaca());
        verify(usuarioDAO).persist(adotante);
    }

    @Test
    @DisplayName("CT-AR-04: Campo ausente")
    void updateFiltroComCampoAusente() {
        adotante.getFiltro().setRaca("Poodle");

        Response response = resource.updateFiltro(null, null, null, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals("Poodle", adotante.getFiltro().getRaca());
        verify(usuarioDAO).persist(adotante);
    }

    @Test
    @DisplayName("CT-AR-05: Campo vazio")
    void updateFiltroComCampoVazio() {
        adotante.getFiltro().setRaca("Poodle");

        Response response = resource.updateFiltro(null, null, null, "", null, null);

        assertEquals(200, response.getStatus());
        assertEquals("Poodle", adotante.getFiltro().getRaca());
        verify(usuarioDAO).persist(adotante);
    }

    @Test
    @DisplayName("CT-AR-06: Número válido")
    void updateFiltroAtualizaNumeroValido() {
        Response response = resource.updateFiltro("12.5", null, null, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals(12.5, adotante.getFiltro().getAlturaMenor(), 0.0001);
    }

    @Test
    @DisplayName("CT-AR-07: Sentinela de limpeza (número)")
    void updateFiltroLimpaCampoNumerico() {
        adotante.getFiltro().setPesoMenor(10.0);

        Response response = resource.updateFiltro(null, null, "null", null, null, null);

        assertEquals(200, response.getStatus());
        assertNull(adotante.getFiltro().getPesoMenor());
    }

    @Test
    @DisplayName("CT-AR-08: Número inválido")
    void updateFiltroRejeitaNumeroInvalido() {
        Response response = resource.updateFiltro(null, "abc", null, null, null, null);

        assertEquals(400, response.getStatus());
        JsonObject body = (JsonObject) response.getEntity();
        assertEquals("O campo gastoMensalMenor não é um número válido", body.getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-09: Formato decimal brasileiro")
    void updateFiltroRejeitaVirgulaDecimal() {
        Response response = resource.updateFiltro("12,5", null, null, null, null, null);

        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-10: Zero")
    void updateFiltroAceitaZero() {
        Response response = resource.updateFiltro(null, null, "0", null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals(0.0, adotante.getFiltro().getPesoMenor(), 0.0001);
    }

    @Test
    @DisplayName("CT-AR-11: Negativo")
    @Disabled("issue #1")
    void updateFiltroRejeitaNegativo() {
        Response response = resource.updateFiltro(null, null, "-5", null, null, null);

        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-12: NaN")
    @Disabled("issue #2")
    void updateFiltroRejeitaNaN() {
        Response response = resource.updateFiltro(null, null, "NaN", null, null, null);

        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-13: Variação do sentinela")
    void updateFiltroTrataSentinelaMaiusculoComoTexto() {
        Response response = resource.updateFiltro(null, null, null, "NULL", null, null);

        assertEquals(200, response.getStatus());
        assertEquals("NULL", adotante.getFiltro().getRaca());
    }

    @Test
    @DisplayName("CT-AR-14: Combinação de campos")
    void updateFiltroAtualizaTodosOsCampos() {
        Response response = resource.updateFiltro("12.5", "200.0", "10.0",
                "Poodle", "pequeno", "cachorro");

        assertEquals(200, response.getStatus());
        assertEquals(12.5, adotante.getFiltro().getAlturaMenor(), 0.0001);
        assertEquals(200.0, adotante.getFiltro().getGastoMensalMenor(), 0.0001);
        assertEquals(10.0, adotante.getFiltro().getPesoMenor(), 0.0001);
        assertEquals("Poodle", adotante.getFiltro().getRaca());
        assertEquals("pequeno", adotante.getFiltro().getPorte());
        assertEquals("cachorro", adotante.getFiltro().getEspecie());
        verify(usuarioDAO, times(1)).persist(adotante);
    }

    @Test
    @DisplayName("CT-AR-15: Atomicidade da atualização")
    @Disabled("issue #3")
    void updateFiltroNaoAlteraNadaQuandoUmCampoEInvalido() {
        adotante.getFiltro().setRaca("Vira-lata");

        Response response = resource.updateFiltro(null, null, "abc", "Poodle", null, null);

        assertEquals(400, response.getStatus());
        assertEquals("Vira-lata", adotante.getFiltro().getRaca());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AR-16: Falha de dependência")
    void updateFiltroRetornaErroInternoQuandoPersistFalha() {
        doThrow(new RuntimeException("database unavailable"))
                .when(usuarioDAO).persist(any());

        Response response = resource.updateFiltro(null, null, null, "Poodle", null, null);

        assertEquals(500, response.getStatus());
        JsonObject body = (JsonObject) response.getEntity();
        assertEquals("Erro interno", body.getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AR-22: Sentinela de limpeza em todos os campos")
    void updateFiltroLimpaTodosOsCampos() {
        preencherFiltro();

        Response response = resource.updateFiltro("null", "null", "null",
                "null", "null", "null");

        assertEquals(200, response.getStatus());
        assertNull(adotante.getFiltro().getAlturaMenor());
        assertNull(adotante.getFiltro().getGastoMensalMenor());
        assertNull(adotante.getFiltro().getPesoMenor());
        assertNull(adotante.getFiltro().getRaca());
        assertNull(adotante.getFiltro().getPorte());
        assertNull(adotante.getFiltro().getEspecie());
    }

    @Test
    @DisplayName("CT-AR-23: Requisição vazia")
    void updateFiltroSemCamposMantemTudo() {
        preencherFiltro();

        Response response = resource.updateFiltro(null, null, null, null, null, null);

        assertEquals(200, response.getStatus());
        assertEquals(50.0, adotante.getFiltro().getAlturaMenor(), 0.0001);
        assertEquals(300.0, adotante.getFiltro().getGastoMensalMenor(), 0.0001);
        assertEquals(20.0, adotante.getFiltro().getPesoMenor(), 0.0001);
        assertEquals("Vira-lata", adotante.getFiltro().getRaca());
        assertEquals("medio", adotante.getFiltro().getPorte());
        assertEquals("cachorro", adotante.getFiltro().getEspecie());
    }

    // Testes unitários para o método getFiltro

    @Test
    @DisplayName("CT-AR-17: Usuário não adotante")
    void getFiltroRejeitaNaoAdotante() {
        Doador doador = new Doador("bia", "123", null, "Bia", "doador",
                "98765432100", new Date());
        when(servletRequest.getAttribute("usuario")).thenReturn(doador);

        Response response = resource.getFiltro();

        assertEquals(401, response.getStatus());
        JsonObject body = (JsonObject) response.getEntity();
        assertEquals("Usuário autenticado não é um adotante", body.getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AR-18: Filtro preenchido")
    void getFiltroRetornaValoresDoFiltro() {
        adotante.getFiltro().setRaca("Poodle");
        adotante.getFiltro().setAlturaMenor(12.5);

        Response response = resource.getFiltro();

        assertEquals(200, response.getStatus());
        JsonObject filtro = ((JsonObject) response.getEntity()).getJsonObject("filtroAdotante");
        assertEquals("Poodle", filtro.getString("raca"));
        assertEquals(12.5, filtro.getJsonNumber("alturaMenor").doubleValue(), 0.0001);
        assertTrue(filtro.isNull("pesoMenor"));
    }

    @Test
    @DisplayName("CT-AR-19: Filtro nulo")
    void getFiltroComFiltroNuloRetornaCamposVazios() {
        adotante.setFiltro(null);

        Response response = resource.getFiltro();

        assertEquals(200, response.getStatus());
        JsonObject filtro = ((JsonObject) response.getEntity()).getJsonObject("filtroAdotante");
        assertTrue(filtro.isNull("raca"));
        assertTrue(filtro.isNull("alturaMenor"));
    }

    @Test
    @DisplayName("CT-AR-20: Encadeamento com CT-AR-12")
    @Disabled("issue #2")
    void getFiltroComPesoNaN() {
        adotante.getFiltro().setPesoMenor(Double.NaN);

        Response response = resource.getFiltro();

        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("CT-AR-21: Falha inesperada")
    void getFiltroSemUsuarioRetornaErroInterno() {
        when(servletRequest.getAttribute("usuario")).thenReturn(null);

        Response response = resource.getFiltro();

        assertEquals(500, response.getStatus());
        JsonObject body = (JsonObject) response.getEntity();
        assertEquals("Erro interno", body.getString("mensagem"));
    }
}