# Trabalho de Qualidade de Software 
## Grupo RTV: Rafaella Lenzi, Thiago Moutinho e Leonardo Vaz
Este repositório é o ponto central da entrega: todos os artefatos estão linkados abaixo.

## Sistemas sob teste
 
Usamos dois sistemas, que se complementam: o Adopet tem as classes de maior complexidade ciclomática (testes unitários) e a Lanchonete tem interface web (testes manuais e, na Entrega 2, testes de sistema).
 
| Sistema | Repositório do grupo | Repositório de origem |
| --- | --- | --- |
| Adopet (API REST de adoção de animais) | Este repositório | [repo-software-testing-courses/Adopet](https://github.com/repo-software-testing-courses/Adopet) |
| Lanchonete Online (sistema de pedidos) | [RTV-QS/lanchonete](https://github.com/RTV-QS/lanchonete) | [repo-software-testing-courses/APS-04-Lanchonete-Online-em-Java](https://github.com/repo-software-testing-courses/APS-04-Lanchonete-Online-em-Java) |

## Entrega 1
 
| Item | Artefato | Link |
| --- | --- | --- |
| 1. Escopo dos sistemas | Plano de Teste, seção 1.1 | [Plano de Teste](https://docs.google.com/document/d/140v4290iqe8Y4YQBjYshYHFi0e81EmsRfP9LbHvgTYg/edit) |
| 2. Testes unitários | Classes de teste (ver tabela abaixo) | [`src/test/java`](src/test/java/com/trabappcorp/Adopet) |
| 3. Plano de Teste (IEEE 829) | Documento no Google Docs | [Plano de Teste](https://docs.google.com/document/d/140v4290iqe8Y4YQBjYshYHFi0e81EmsRfP9LbHvgTYg/edit) |
| 4. Testes manuais | Casos e execução no TestLink | [Relatório de execução](https://github.com/RTV-QS/lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/TestPlanExecutionReportCarrinho.pdf) · [Métricas](https://github.com/RTV-QS/lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/GeneralTestPlanReportCarrinho.pdf) · [Casos (XML)](https://github.com/RTV-QS/lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/testlink-carrinho.xml) |
| 5. Issues | GitHub Issues dos dois forks | [Adopet](https://github.com/RTV-QS/Adopet/issues) · [Lanchonete](https://github.com/RTV-QS/lanchonete/issues) |
 
### Testes unitários (Adopet)
 
| Integrante | Classe sob teste | Classe de teste | Casos de teste |
| --- | --- | --- | --- |
| [Integrante 1] | `AuthenticationResource` | [preencher] | [preencher] |
| Rafaella Lenzi | `AdotantesResource` | [`AdotantesResourceTest`](src/test/java/com/trabappcorp/Adopet/AdotantesResourceTest.java) | [Planilha CT-AR](https://docs.google.com/spreadsheets/d/1APhWzYmzxmuvqNNjlrEM5k-AwzITs-DdwDaY5x-xIso/edit?usp=sharing) |
| [Integrante 3] | `PetDAO` | [preencher] | [preencher] |
 
### Testes manuais (Lanchonete)
 
| Integrante | Funcionalidade | Registro |
| --- | --- | --- |
| Rafaella Lenzi | Carrinho e finalização do pedido | TestLink: [relatório](https://github.com/RTV-QS/lanchonete/blob/trabalho-qs/docs/entrega1/testes-manuais/TestPlanExecutionReportCarrinho.pdf) |
| Thiago Moutinho | [funcionalidade] | [preencher] |
| Leonardo Vaz | [funcionalidade] | [preencher] |
 
### Defeitos encontrados

**Adopet (testes unitários)**

| Issue | Resumo | Caso de teste |
| --- | --- | --- |
| [Adopet#2](https://github.com/RTV-QS/Adopet/issues/2) | `updateFiltro` aceita valor negativo como peso máximo | CT-AR-11 |
| [Adopet#3](https://github.com/RTV-QS/Adopet/issues/3) | `updateFiltro` aceita NaN como valor numérico | CT-AR-12 |
| [Adopet#4](https://github.com/RTV-QS/Adopet/issues/4) | `updateFiltro` altera o filtro mesmo quando a requisição é rejeitada | CT-AR-15 |
| [Adopet#5](https://github.com/RTV-QS/Adopet/issues/5) | `getFiltro` retorna 500 quando o filtro contém NaN | CT-AR-20 |

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
 
## Entrega 2
 
A preencher.
