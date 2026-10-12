package com.trabappcorp.Adopet;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import DAO.AdotanteDAO;
import DAO.DoadorDAO;
import DAO.UsuarioDAO;
import modelo.Adotante;
import modelo.Doador;
import modelo.Endereco;
import modelo.Instituicao;
import modelo.Usuario;

/**
 * Casos de teste unitários da classe AuthenticationResource (CT-AU-01 a CT-AU-48).
 *
 * Complexidade ciclomática da classe sob teste: 28 (register = 20, authenticate = 7),
 * medida com a ferramenta lizard. Escopo e divisão estão no Plano de Teste, seção 1.1.
 *
 * Técnica de isolamento desta classe: mockStatic nos três DAOs alcançados por
 * register (UsuarioDAO, DoadorDAO e AdotanteDAO). Os dois últimos são
 * instanciados pelo código de produção mas nunca usados; sem o mockStatic, o
 * construtor de JpaDAO tentaria abrir um EntityManager real e os testes
 * dependeriam do banco.
 *
 * Técnicas de projeto dos casos: partição de equivalência e análise de valor limite.
 * O oráculo de cada caso está declarado na planilha CT-AU e, quando é o domínio e o
 * sistema discorda, o teste fica com @Disabled apontando a issue, conforme a regra
 * de triagem do Plano de Teste, seção 2.3.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuthenticationResourceTest {

    private static final String DATA_ISO_COMPLETA = "1990-05-20T00:00:00Z";

    @Mock
    HttpServletRequest servletRequest;

    @Mock
    UsuarioDAO usuarioDAO;

    @Mock
    DoadorDAO doadorDAO;

    @Mock
    AdotanteDAO adotanteDAO;

    @InjectMocks
    AuthenticationResource resource;

    MockedStatic<UsuarioDAO> usuarioStatic;
    MockedStatic<DoadorDAO> doadorStatic;
    MockedStatic<AdotanteDAO> adotanteStatic;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        usuarioStatic = mockStatic(UsuarioDAO.class);
        usuarioStatic.when(UsuarioDAO::getInstance).thenReturn(usuarioDAO);

        doadorStatic = mockStatic(DoadorDAO.class);
        doadorStatic.when(DoadorDAO::getInstance).thenReturn(doadorDAO);

        adotanteStatic = mockStatic(AdotanteDAO.class);
        adotanteStatic.when(AdotanteDAO::getInstance).thenReturn(adotanteDAO);

        // Por padrão o login pesquisado não existe, ou seja, está livre para cadastro.
        when(usuarioDAO.findByLogin(any())).thenReturn(null);
    }

    @AfterEach
    @SuppressWarnings("unused")
    void tearDown() {
        usuarioStatic.close();
        doadorStatic.close();
        adotanteStatic.close();
    }

    // ---------------------------------------------------------------- helpers

    /** Cadastro de pessoa física (doador ou adotante), sem dados de endereço. */
    private Response registrarPessoa(String login, String senha, String nome, String perfil,
            String cpf, String dataNascimento) {
        return resource.register(login, senha, nome, perfil,
                null, null, null, null, null, null,
                cpf, dataNascimento,
                null, null, null, null, null, null);
    }

    /** Cadastro de instituição, sem dados de endereço. */
    private Response registrarInstituicao(String numAnimais, String capacidade,
            String numRegistro, String certificacoes, String razaoSocial, String dataFundacao) {
        return resource.register("abrigo", "senha123", "Abrigo Bons Amigos", "instituicao",
                null, null, null, null, null, null,
                null, null,
                numAnimais, capacidade, numRegistro, certificacoes, razaoSocial, dataFundacao);
    }

    /** Doador válido com um endereço completo. */
    private Response registrarComEndereco(String cidade, String bairro, String estado,
            String rua, String numero, String complemento) {
        return resource.register("joao", "senha123", "João Silva", "doador",
                cidade, bairro, estado, rua, numero, complemento,
                "12345678900", DATA_ISO_COMPLETA,
                null, null, null, null, null, null);
    }

    private Usuario capturarUsuarioPersistido() {
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDAO).persist(captor.capture());
        return captor.getValue();
    }

    private JsonObject corpo(Response response) {
        return (JsonObject) response.getEntity();
    }

    /**
     * Devolve o payload de um JWT já decodificado. Não valida a assinatura de
     * propósito: o objetivo é inspecionar as claims emitidas sem replicar no
     * teste a chave secreta que está hard-coded no código de produção.
     */
    private JsonObject payloadDoToken(String jwt) {
        String[] partes = jwt.split("\\.");
        assertEquals(3, partes.length, "um JWT deve ter header, payload e assinatura");
        String json = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        return javax.json.Json.createReader(new java.io.StringReader(json)).readObject();
    }

    private Usuario usuarioComSenha(String login, String senha) {
        Doador doador = new Doador(login, senha, null, "Nome Qualquer", "doador",
                "12345678900", new Date());
        doador.setId(7L);
        return doador;
    }

    // =====================================================================
    // authenticate - POST /auth/login (complexidade ciclomática 7)
    // =====================================================================

    @Test
    @DisplayName("CT-AU-01: Login ausente")
    void authenticateRejeitaLoginAusente() {
        Response response = resource.authenticate(null, "senha123");

        assertEquals(400, response.getStatus());
        assertEquals("Falta parâmetros", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    @DisplayName("CT-AU-02: Login vazio")
    void authenticateRejeitaLoginVazio() {
        Response response = resource.authenticate("", "senha123");

        assertEquals(400, response.getStatus());
        assertEquals("Falta parâmetros", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    @DisplayName("CT-AU-03: Senha ausente")
    void authenticateRejeitaSenhaAusente() {
        Response response = resource.authenticate("joao", null);

        assertEquals(400, response.getStatus());
        assertEquals("Falta parâmetros", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    @DisplayName("CT-AU-04: Senha vazia")
    void authenticateRejeitaSenhaVazia() {
        Response response = resource.authenticate("joao", "");

        assertEquals(400, response.getStatus());
        assertEquals("Falta parâmetros", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    @DisplayName("CT-AU-05: Senha só com espaço em branco (valor limite de isSet)")
    void authenticateTrataSenhaEmBrancoComoPreenchida() {
        // ValidationWrapper.isSet só olha o comprimento da string, então " " conta
        // como parâmetro preenchido e a requisição passa da validação inicial.
        Response response = resource.authenticate("joao", " ");

        assertEquals(401, response.getStatus());
        assertEquals("Usuário e/ou senha inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO).findByLogin("joao");
    }

    @Test
    @DisplayName("CT-AU-06: Credenciais válidas")
    void authenticateAceitaCredenciaisValidas() {
        when(usuarioDAO.findByLogin("joao")).thenReturn(usuarioComSenha("joao", "senha123"));

        Response response = resource.authenticate("joao", "senha123");

        assertEquals(200, response.getStatus());
        String token = corpo(response).getString("token");
        JsonObject payload = payloadDoToken(token);
        assertEquals("joao", payload.getString("sub"));
        assertEquals("my_app", payload.getString("iss"));
    }

    @Test
    @DisplayName("CT-AU-07: Usuário inexistente")
    void authenticateRejeitaUsuarioInexistente() {
        when(usuarioDAO.findByLogin("fantasma")).thenReturn(null);

        Response response = resource.authenticate("fantasma", "senha123");

        assertEquals(401, response.getStatus());
        assertEquals("Usuário e/ou senha inválidos", corpo(response).getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AU-08: Senha incorreta")
    void authenticateRejeitaSenhaIncorreta() {
        when(usuarioDAO.findByLogin("joao")).thenReturn(usuarioComSenha("joao", "senha123"));

        Response response = resource.authenticate("joao", "outraSenha");

        assertEquals(401, response.getStatus());
        assertEquals("Usuário e/ou senha inválidos", corpo(response).getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AU-09: Diferença de caixa na senha")
    void authenticateDiferenciaMaiusculasNaSenha() {
        when(usuarioDAO.findByLogin("joao")).thenReturn(usuarioComSenha("joao", "senha123"));

        Response response = resource.authenticate("joao", "SENHA123");

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("CT-AU-10: Falha de dependência não deve vazar a mensagem interna")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#13 (BUG-AD-12)")
    void authenticateNaoVazaMensagemInternaDaExcecao() {
        when(usuarioDAO.findByLogin("joao"))
                .thenThrow(new RuntimeException("Communications link failure to mysql://10.0.0.7:3306"));

        Response response = resource.authenticate("joao", "senha123");

        assertEquals(500, response.getStatus());
        // register devolve um JSON com "Erro interno"; authenticate devolve a
        // mensagem crua da exceção, expondo detalhes da infraestrutura.
        Object entidade = response.getEntity();
        assertTrue(entidade instanceof JsonObject,
                "a resposta deveria ser um JSON como nos outros fluxos da classe, mas veio: " + entidade);
        assertEquals("Erro interno", ((JsonObject) entidade).getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AU-11: Token emitido deve ter expiração")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#14 (BUG-AD-13)")
    void authenticateEmiteTokenComExpiracao() {
        when(usuarioDAO.findByLogin("joao")).thenReturn(usuarioComSenha("joao", "senha123"));

        Response response = resource.authenticate("joao", "senha123");

        assertEquals(200, response.getStatus());
        JsonObject payload = payloadDoToken(corpo(response).getString("token"));
        assertTrue(payload.containsKey("exp"),
                "o token deveria trazer a claim exp; emitido sem expiração, vale para sempre");
    }

    // =====================================================================
    // register - POST /auth/register (complexidade ciclomática 20)
    // =====================================================================

    @Test
    @DisplayName("CT-AU-12: Login ausente")
    void registerRejeitaLoginAusente() {
        Response response = registrarPessoa(null, "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-13: Senha vazia")
    void registerRejeitaSenhaVazia() {
        Response response = registrarPessoa("joao", "", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-14: Nome ausente")
    void registerRejeitaNomeAusente() {
        Response response = registrarPessoa("joao", "senha123", null, "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-15: Perfil ausente")
    void registerRejeitaPerfilAusente() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", null,
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-16: Login já cadastrado")
    void registerRejeitaLoginDuplicado() {
        when(usuarioDAO.findByLogin("joao")).thenReturn(usuarioComSenha("joao", "outra"));

        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Usuário já existe", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-17: Ordem da validação em relação à consulta ao banco")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#12 (BUG-AD-11)")
    void registerNaoConsultaOBancoAntesDeValidarOsParametros() {
        Response response = registrarPessoa(null, null, null, null, null, null);

        assertEquals(400, response.getStatus());
        // findByLogin é chamado na primeira linha do try, antes do bloco de
        // validação, então uma requisição sem nenhum parâmetro ainda custa uma
        // consulta ao banco de dados.
        verify(usuarioDAO, never()).findByLogin(any());
    }

    @Test
    @DisplayName("CT-AU-18: Doador válido")
    void registerCadastraDoadorValido() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(200, response.getStatus());
        assertEquals("Cadastro feito com sucesso", corpo(response).getString("mensagem"));
        assertEquals("joao", corpo(response).getString("login"));

        Usuario persistido = capturarUsuarioPersistido();
        assertTrue(persistido instanceof Doador);
        assertEquals("joao", persistido.getLogin());
        assertEquals("João Silva", persistido.getNome());
        assertEquals("doador", persistido.getPerfil());
        assertEquals("12345678900", ((Doador) persistido).getCPF());
    }

    @Test
    @DisplayName("CT-AU-19: Adotante válido deve ser gravado com o nome informado")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#6 (BUG-AD-05)")
    void registerCadastraAdotanteComONomeInformado() {
        Response response = registrarPessoa("ana", "senha123", "Ana Souza", "adotante",
                "98765432100", DATA_ISO_COMPLETA);

        assertEquals(200, response.getStatus());

        Usuario persistido = capturarUsuarioPersistido();
        assertTrue(persistido instanceof Adotante);
        // O ramo do adotante passa `login` na posição do parâmetro `nome` do
        // construtor, diferente do ramo do doador (CT-AU-18), que passa `nome`.
        assertEquals("Ana Souza", persistido.getNome());
    }

    @Test
    @DisplayName("CT-AU-20: Instituição válida")
    void registerCadastraInstituicaoValida() {
        Response response = registrarInstituicao("10", "50", "REG-001",
                "ONG certificada", "Abrigo Bons Amigos LTDA", "2010-03-15T00:00:00Z");

        assertEquals(200, response.getStatus());
        assertEquals("Cadastro feito com sucesso", corpo(response).getString("mensagem"));

        Usuario persistido = capturarUsuarioPersistido();
        assertTrue(persistido instanceof Instituicao);
        Instituicao instituicao = (Instituicao) persistido;
        assertEquals(10, instituicao.getNumeroAnimais());
        assertEquals(50, instituicao.getCapacidade());
        assertEquals("REG-001", instituicao.getNumeroRegistro());
        assertEquals("ONG certificada", instituicao.getCertificacoes());
        assertEquals("Abrigo Bons Amigos LTDA", instituicao.getRazaoSocial());
        assertEquals("Abrigo Bons Amigos", instituicao.getNome());
    }

    @Test
    @DisplayName("CT-AU-21: Perfil com caixa diferente")
    void registerRejeitaPerfilComCaixaDiferente() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "Doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Perfil inválido", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-22: Perfil desconhecido")
    void registerRejeitaPerfilDesconhecido() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "veterinario",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Perfil inválido", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-23: Doador sem CPF")
    void registerRejeitaDoadorSemCpf() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                null, DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-24: Adotante com CPF vazio")
    void registerRejeitaAdotanteComCpfVazio() {
        Response response = registrarPessoa("ana", "senha123", "Ana Souza", "adotante",
                "", DATA_ISO_COMPLETA);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-25: Data de nascimento ausente")
    void registerRejeitaDataDeNascimentoAusenteComErroDeRequisicao() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", null);

        // ValidationWrapper.parseDate chama value_str.length() sem checar null,
        // mas o catch(Exception) do próprio parseDate captura o NullPointerException
        // e o converte em BadRequest. A hipótese inicial era de que o campo ausente
        // viraria 500; o teste refutou, e nenhuma issue foi aberta.
        assertEquals(400, response.getStatus());
        assertEquals("O campo dataNascimento não é uma data válida",
                corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-26: Data de nascimento vazia")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#7 (BUG-AD-06)")
    void registerRejeitaDataDeNascimentoVazia() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", "");

        // parseDate devolve null quando a string é vazia, então o cadastro é
        // concluído com um doador sem data de nascimento.
        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-27: Data de nascimento em formato brasileiro")
    void registerRejeitaDataEmFormatoBrasileiro() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", "20/05/1990");

        assertEquals(400, response.getStatus());
        assertEquals("O campo dataNascimento não é uma data válida",
                corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-28: Data de nascimento no formato ISO de data (yyyy-MM-dd)")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#8 (BUG-AD-07)")
    void registerAceitaDataNoFormatoIsoDeData() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", "1990-05-20");

        // parseDate usa Instant.parse, que só aceita o instante completo com
        // fuso. Um campo <input type="date"> manda exatamente "1990-05-20".
        assertEquals(200, response.getStatus());
        assertEquals("1990-05-20", dataIsoDe(capturarUsuarioPersistido()));
    }

    @Test
    @DisplayName("CT-AU-29: Data de nascimento no formato ISO completo")
    void registerAceitaDataNoFormatoIsoCompleto() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(200, response.getStatus());
        assertEquals("1990-05-20", dataIsoDe(capturarUsuarioPersistido()));
    }

    private String dataIsoDe(Usuario usuario) {
        Date data = ((Doador) usuario).getDataNascimento();
        Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendario.setTime(data);
        return String.format("%04d-%02d-%02d",
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH) + 1,
                calendario.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    @DisplayName("CT-AU-30: Instituição sem número de animais")
    void registerRejeitaInstituicaoSemNumeroDeAnimais() {
        Response response = registrarInstituicao(null, "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-31: Instituição sem razão social")
    void registerRejeitaInstituicaoSemRazaoSocial() {
        Response response = registrarInstituicao("10", "50", "REG-001",
                "ONG certificada", null, "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-32: Instituição sem número de registro")
    void registerRejeitaInstituicaoSemNumeroDeRegistro() {
        Response response = registrarInstituicao("10", "50", "",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-33: Instituição sem data de fundação")
    void registerRejeitaInstituicaoSemDataDeFundacao() {
        Response response = registrarInstituicao("10", "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", null);

        assertEquals(400, response.getStatus());
        assertEquals("Parâmetros faltantes ou inválidos", corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-34: Número de animais não numérico")
    void registerRejeitaNumeroDeAnimaisNaoNumerico() {
        Response response = registrarInstituicao("muitos", "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        assertEquals("O campo numAnimais não é um inteiro válido",
                corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-35: Instituição recém-criada, com zero animais (valor limite)")
    void registerAceitaInstituicaoComZeroAnimais() {
        Response response = registrarInstituicao("0", "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(200, response.getStatus());
        assertEquals(0, ((Instituicao) capturarUsuarioPersistido()).getNumeroAnimais());
    }

    @Test
    @DisplayName("CT-AU-36: Número de animais negativo (valor limite)")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#9 (BUG-AD-08)")
    void registerRejeitaNumeroDeAnimaisNegativo() {
        Response response = registrarInstituicao("-5", "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-37: Mais animais do que a capacidade")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#10 (BUG-AD-09)")
    void registerRejeitaMaisAnimaisQueACapacidade() {
        Response response = registrarInstituicao("80", "50", "REG-001",
                "ONG certificada", "Abrigo LTDA", "2010-03-15T00:00:00Z");

        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-38: Instituição sem certificações")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#11 (BUG-AD-10)")
    void registerRejeitaInstituicaoSemCertificacoes() {
        Response response = registrarInstituicao("10", "50", "REG-001",
                null, "Abrigo LTDA", "2010-03-15T00:00:00Z");

        // certificacoes é o único campo da instituição que não entra na
        // validação, mas a coluna correspondente é declarada nullable = false.
        assertEquals(400, response.getStatus());
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-39: Número do endereço não numérico")
    void registerRejeitaNumeroDeEnderecoNaoNumerico() {
        Response response = registrarComEndereco("Rio de Janeiro", "Centro", "RJ",
                "Rua A", "sem número", "ap 101");

        assertEquals(400, response.getStatus());
        assertEquals("O campo numero não é um inteiro válido",
                corpo(response).getString("mensagem"));
        verify(usuarioDAO, never()).persist(any());
    }

    @Test
    @DisplayName("CT-AU-40: Número do endereço vazio (valor limite de campo opcional)")
    void registerRejeitaNumeroDeEnderecoVazio() {
        // O código testa numero_str != null em vez de isSet(numero_str), então
        // uma string vazia chega ao parseInt e vira 400. Um formulário que envia
        // todos os campos de endereço em branco não consegue cadastrar.
        Response response = registrarComEndereco("Rio de Janeiro", "Centro", "RJ",
                "Rua A", "", "ap 101");

        assertEquals(400, response.getStatus());
        assertEquals("O campo numero não é um inteiro válido",
                corpo(response).getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AU-41: Endereço completo é repassado ao usuário")
    void registerRepassaOEnderecoCompleto() {
        Response response = registrarComEndereco("Rio de Janeiro", "Centro", "RJ",
                "Rua A", "100", "ap 101");

        assertEquals(200, response.getStatus());

        Endereco endereco = capturarUsuarioPersistido().getEndereco();
        assertEquals("Rio de Janeiro", endereco.getCidade());
        assertEquals("Centro", endereco.getBairro());
        assertEquals("RJ", endereco.getEstado());
        assertEquals("Rua A", endereco.getRua());
        assertEquals(Integer.valueOf(100), endereco.getNumero());
        assertEquals("ap 101", endereco.getComplemento());
    }

    @Test
    @DisplayName("CT-AU-42: Endereço ausente é aceito")
    void registerAceitaCadastroSemEndereco() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(200, response.getStatus());

        Endereco endereco = capturarUsuarioPersistido().getEndereco();
        assertNull(endereco.getCidade());
        assertNull(endereco.getNumero());
    }

    @Test
    @DisplayName("CT-AU-43: Senha não deve ser persistida em texto puro")
    @Disabled("Defeito conhecido: RTV-QS/Adopet#15 (BUG-AD-14)")
    void registerNaoPersisteASenhaEmTextoPuro() {
        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(200, response.getStatus());
        // A senha vai para o banco exatamente como chegou, e authenticate a
        // compara com equals, também em texto puro.
        assertNotEquals("senha123", capturarUsuarioPersistido().getSenha(),
                "a senha deveria ser gravada com uma função de hash, não em texto puro");
    }

    @Test
    @DisplayName("CT-AU-44: Falha ao persistir")
    void registerRetornaErroInternoQuandoPersistFalha() {
        doThrow(new RuntimeException("Communications link failure to mysql://10.0.0.7:3306"))
                .when(usuarioDAO).persist(any());

        Response response = registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        assertEquals(500, response.getStatus());
        // Aqui register acerta: devolve uma mensagem genérica, sem expor a
        // infraestrutura. Contraste com CT-AU-10, em authenticate.
        assertEquals("Erro interno", corpo(response).getString("mensagem"));
    }

    @Test
    @DisplayName("CT-AU-45: DAOs de doador e de adotante são instanciados e nunca usados")
    void registerNaoUsaOsDaosEspecificosDePerfil() {
        registrarPessoa("joao", "senha123", "João Silva", "doador",
                "12345678900", DATA_ISO_COMPLETA);

        // O código chama DoadorDAO.getInstance() dentro do ramo do doador, mas
        // persiste pelo UsuarioDAO. Documentado como código morto, não como
        // defeito: o comportamento observável pela API está correto.
        doadorStatic.verify(DoadorDAO::getInstance);
        verifyNoInteractions(doadorDAO);
        verify(usuarioDAO).persist(any());
    }

    // =====================================================================
    // check - GET /auth/check
    // =====================================================================

    @Test
    @DisplayName("CT-AU-46: Usuário autenticado")
    void checkRetornaOUsuarioAutenticado() {
        Usuario usuario = usuarioComSenha("joao", "senha123");
        when(servletRequest.getAttribute("usuario")).thenReturn(usuario);

        Response response = resource.check();

        assertEquals(200, response.getStatus());
        JsonObject corpo = corpo(response);
        assertEquals("sucesso", corpo.getString("mensagem"));
        assertEquals("joao", corpo.getString("login"));
        assertEquals(7, corpo.getJsonNumber("id").intValue());
    }

    @Test
    @DisplayName("CT-AU-47: Usuário autenticado ainda sem id (valor limite)")
    void checkComUsuarioSemIdRetornaIdNulo() {
        Doador semId = new Doador("joao", "senha123", null, "João Silva", "doador",
                "12345678900", new Date());
        when(servletRequest.getAttribute("usuario")).thenReturn(semId);

        Response response = resource.check();

        assertEquals(200, response.getStatus());
        assertTrue(corpo(response).isNull("id"));
    }

    @Test
    @DisplayName("CT-AU-48: Requisição sem o atributo de usuário")
    void checkSemAtributoDeUsuarioPropagaNullPointer() {
        when(servletRequest.getAttribute("usuario")).thenReturn(null);

        // check é o único método da classe sem try/catch. O cenário não é
        // alcançável pela API, porque AuthorizeFilter sempre popula o atributo
        // antes de liberar a requisição; por isso é registrado como oportunidade
        // de programação defensiva e não como defeito.
        assertThrows(NullPointerException.class, () -> resource.check());
    }
}
