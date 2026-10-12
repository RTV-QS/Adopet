# Trabalho de Qualidade de Software 
## Grupo RTV: Rafaella Lenzi, Thiago Moutinho e Leonardo Vaz
Este repositório é o ponto central da entrega: todos os artefatos estão linkados abaixo.

## Sistemas sob teste
 
Usamos dois sistemas, que se complementam: o Adopet tem as classes de maior complexidade ciclomática (testes unitários) e a Lanchonete tem interface web (testes manuais e, na Entrega 2, testes de sistema).
 
| Sistema | Repositório do grupo | Repositório de origem |
| --- | --- | --- |
| Adopet (API REST de adoção de animais) | Este repositório | [repo-software-testing-courses/Adopet](https://github.com/repo-software-testing-courses/Adopet) |
| Lanchonete Online (sistema de pedidos) | [RTV-QS/Lanchonete](https://github.com/RTV-QS/Lanchonete) | [repo-software-testing-courses/APS-04-Lanchonete-Online-em-Java](https://github.com/repo-software-testing-courses/APS-04-Lanchonete-Online-em-Java) |

## Entrega 1
 
| Item | Artefato | Link |
| --- | --- | --- |
| 1. Escopo dos sistemas | Plano de Teste, seção 1.1 | [Plano de Teste](https://docs.google.com/document/d/140v4290iqe8Y4YQBjYshYHFi0e81EmsRfP9LbHvgTYg/edit) |
| 2. Testes unitários | Classes de teste e planilhas de casos (ver tabela abaixo) | [`src/test/java`](src/test/java/com/trabappcorp/Adopet) · [`docs/entrega1/testes-unitarios`](docs/entrega1/testes-unitarios) |
| 3. Plano de Teste (IEEE 829) | Documento no Google Docs | [Plano de Teste](https://docs.google.com/document/d/140v4290iqe8Y4YQBjYshYHFi0e81EmsRfP9LbHvgTYg/edit) |
| 4. Testes manuais | Casos e execução, por funcionalidade (ver tabela abaixo) | [`docs/entrega1/testes-manuais`](https://github.com/RTV-QS/Lanchonete/tree/trabalho-qs/docs/entrega1/testes-manuais) |
| 5. Issues | GitHub Issues dos dois forks | [Adopet](https://github.com/RTV-QS/Adopet/issues) · [Lanchonete](https://github.com/RTV-QS/Lanchonete/issues) |
 
### Testes unitários (Adopet)

Toda a suíte roda sem banco de dados, com um único comando:

```bash
mvn test
```

Verificada com JDK 21 e JDK 27. O Mockito 4 não suporta oficialmente JDKs acima do
21; neles a suíte depende da flag `-Dnet.bytebuddy.experimental=true`, já configurada
no surefire do [`pom.xml`](pom.xml) — ver Plano de Teste, seção 4.2. Os testes que apontam defeitos confirmados estão marcados com
`@Disabled` referenciando a issue correspondente, conforme a regra de triagem da
seção 2.3 do Plano de Teste, de forma que a suíte fica verde e os defeitos
seguem rastreáveis.

| Integrante | Classe sob teste | Classe de teste | Casos de teste |
| --- | --- | --- | --- |
| Leonardo Vaz | `AuthenticationResource` | [`AuthenticationResourceTest`](src/test/java/com/trabappcorp/Adopet/AuthenticationResourceTest.java) | [Planilha CT-AU](https://docs.google.com/spreadsheets/d/1GhxLh4pKMCooLMxSzE9t9OwpoMXqXKp_mi8opR8Rtg4/edit?usp=sharing) · [CSV](docs/entrega1/testes-unitarios/casos-teste-CT-AU.csv) |
| Rafaella Lenzi | `AdotantesResource` | [`AdotantesResourceTest`](src/test/java/com/trabappcorp/Adopet/AdotantesResourceTest.java) | [Planilha CT-AR](https://docs.google.com/spreadsheets/d/1APhWzYmzxmuvqNNjlrEM5k-AwzITs-DdwDaY5x-xIso/edit?usp=sharing) · [CSV](docs/entrega1/testes-unitarios/casos-teste-CT-AR.csv) |
| Thiago Moutinho | `PetDAO` | [`PetDAOTest`](src/test/java/com/trabappcorp/Adopet/PetDAOTest.java) | [Planilha CT-PD](docs/entrega1/testes-unitarios/CT-PD_PetDAO_Thiago.xlsx) · [CSV](docs/entrega1/testes-unitarios/casos-teste-CT-PD.csv) · [execução](docs/entrega1/testes-unitarios/DAO.PetDAOTest.txt) |
 
### Testes manuais (Lanchonete)
 
| Integrante | Funcionalidade | Registro |
| --- | --- | --- |
| Rafaella Lenzi | Carrinho e finalização do pedido | TestLink: [relatório de execução](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/Carrinho/TestPlanExecutionReportCarrinho.pdf) · [métricas](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/Carrinho/GeneralTestPlanReportCarrinho.pdf) · [plano](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/Carrinho/TestPlanReportCarrinho.pdf) · [casos (XML)](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/Carrinho/testlink-carrinho.xml) · Casos de Teste: [Planilha CT-CR](https://docs.google.com/spreadsheets/d/1g0kw0XVC-zGDFqql9p14jj0e2WOVgpvdOolvmPowzfQ/edit?usp=sharing) · [CSV](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/Carrinho/casos-teste-CT-CR.csv) |
| Thiago Moutinho | Consulta do cardápio | TestLink: [relatório de execução](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/TestPlanExecutionReportCardapio.pdf) · [métricas](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/GeneralTestPlanReportCardapio.pdf) · [plano](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/TestPlanReportCardapio.pdf) · [casos (XML)](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/testlink-cardapio.xml) · [planilha CT-CM](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CT-CM_Cardapio_Thiago.xlsx) · [CSV](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/casos-teste-CT-CM.csv) |
| Leonardo Vaz | Cadastro e login do cliente | TestLink: [relatório de execução](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/TestPlanExecutionReportCadastroLogin.pdf) · [métricas](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/GeneralTestPlanReportCadastroLogin.pdf) · [plano](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/TestPlanReportCadastroLogin.pdf) · [casos (XML)](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/testlink-cadastro-login.xml) · Casos de Teste: [Planilha CT-AL](https://docs.google.com/spreadsheets/d/1x4TJoBn1zPQOh_yKTHckskp6BNqWBVY8SZEQvnCfwCg/edit?usp=sharing) · [CSV](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/casos-teste-CT-AL.csv) · [Verificação de backend](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/verificacao-backend-cadastro-login.md) |
 
### Defeitos encontrados

**Adopet (testes unitários)**

| Issue | Resumo | Caso de teste |
| --- | --- | --- |
| [Adopet#2](https://github.com/RTV-QS/Adopet/issues/2) | `updateFiltro` aceita valor negativo como peso máximo | CT-AR-11 |
| [Adopet#3](https://github.com/RTV-QS/Adopet/issues/3) | `updateFiltro` aceita NaN como valor numérico | CT-AR-12 |
| [Adopet#4](https://github.com/RTV-QS/Adopet/issues/4) | `updateFiltro` altera o filtro mesmo quando a requisição é rejeitada | CT-AR-15 |
| [Adopet#5](https://github.com/RTV-QS/Adopet/issues/5) | `getFiltro` retorna 500 quando o filtro contém NaN | CT-AR-20 |
| [Adopet#6](https://github.com/RTV-QS/Adopet/issues/6) | `register` grava o adotante com o login no lugar do nome | CT-AU-19 |
| [Adopet#7](https://github.com/RTV-QS/Adopet/issues/7) | `register` conclui o cadastro sem data de nascimento quando o campo vem vazio | CT-AU-26 |
| [Adopet#8](https://github.com/RTV-QS/Adopet/issues/8) | `register` rejeita data no formato ISO de data (`yyyy-MM-dd`) | CT-AU-28 |
| [Adopet#9](https://github.com/RTV-QS/Adopet/issues/9) | `register` aceita número negativo de animais no cadastro de instituição | CT-AU-36 |
| [Adopet#10](https://github.com/RTV-QS/Adopet/issues/10) | `register` aceita instituição com mais animais do que a capacidade | CT-AU-37 |
| [Adopet#11](https://github.com/RTV-QS/Adopet/issues/11) | `register` aceita instituição sem certificações, embora a coluna seja NOT NULL | CT-AU-38 |
| [Adopet#12](https://github.com/RTV-QS/Adopet/issues/12) | `register` consulta o banco antes de validar os parâmetros da requisição | CT-AU-17 |
| [Adopet#13](https://github.com/RTV-QS/Adopet/issues/13) | `authenticate` devolve a mensagem interna da exceção no corpo da resposta | CT-AU-10 |
| [Adopet#14](https://github.com/RTV-QS/Adopet/issues/14) | Token JWT é emitido sem expiração | CT-AU-11 |
| [Adopet#15](https://github.com/RTV-QS/Adopet/issues/15) | Senha é gravada e comparada em texto puro | CT-AU-43 |

**Lanchonete (testes manuais)**

| Issue | Resumo | Caso de teste |
| --- | --- | --- |
| [Lanchonete#1](https://github.com/RTV-QS/Lanchonete/issues/1) | Valor total do pedido ignora a quantidade dos itens | CT-CR-08 |
| [Lanchonete#2](https://github.com/RTV-QS/Lanchonete/issues/2) | Pedido é criado com o carrinho vazio | CT-CR-09 |
| [Lanchonete#3](https://github.com/RTV-QS/Lanchonete/issues/3) | Forma de pagamento e de entrega não são exigidas nem registradas | CT-CR-10, CT-CR-11 |
| [Lanchonete#4](https://github.com/RTV-QS/Lanchonete/issues/4) | Quantidades do carrinho são perdidas ao recarregar a página | CT-CR-12 |
| [Lanchonete#5](https://github.com/RTV-QS/Lanchonete/issues/5) | Carrinho não é compartilhado entre abas | CT-CR-13 |
| [Lanchonete#6](https://github.com/RTV-QS/Lanchonete/issues/6) | Adicionar o mesmo lanche duas vezes não aumenta a quantidade | CT-CR-14 |
| [Lanchonete#7](https://github.com/RTV-QS/Lanchonete/issues/7) | Pedido é aceito com a lanchonete fechada | CT-CR-15 |
| [Lanchonete#8](https://github.com/RTV-QS/Lanchonete/issues/8) | Cadastro permite usuário duplicado e o dono original perde o acesso à conta | CT-AL-06, CT-AL-07 |
| [Lanchonete#9](https://github.com/RTV-QS/Lanchonete/issues/9) | Login de usuário com apóstrofo falha com erro 500 por SQL concatenado | CT-AL-10 |
| [Lanchonete#10](https://github.com/RTV-QS/Lanchonete/issues/10) | Cadastro não valida nenhum campo: grava cliente com nome ou usuário vazio | CT-AL-03, CT-AL-04, CT-AL-05 |
| [Lanchonete#11](https://github.com/RTV-QS/Lanchonete/issues/11) | Logout invalida apenas o token apresentado e os tokens antigos não expiram | CT-AL-17 |
| [Lanchonete#12](https://github.com/RTV-QS/Lanchonete/issues/12) | Cadastro aceita senha composta apenas de espaços | CT-AL-08 |
| [Lanchonete#13](https://github.com/RTV-QS/Lanchonete/issues/13) | Tela de login pede E-mail, mas o sistema autentica por nome de usuário | CT-AL-19 |
| [Lanchonete#14](https://github.com/RTV-QS/Lanchonete/issues/14) | Conexões com o banco nunca são fechadas e esgotam o PostgreSQL | [Verificação de backend](https://github.com/RTV-QS/Lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/CadastroLogin/verificacao-backend-cadastro-login.md) |
 
## Entrega 2
 
A preencher.
