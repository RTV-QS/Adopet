# Execução dos testes de PetDAO — Thiago Moutinho

Execução em 12/10/2026, às 15:34:26 (-03:00), Windows, Temurin JDK 25.0.4.1 e Maven 3.9.9. Foi utilizada a configuração de Mockito/Byte Buddy e Surefire do `pom.xml` do grupo, incluindo o modo experimental para este JDK.

Comando utilizado a partir da pasta Adopet:

```powershell
mvn -o "-Dmaven.repo.local=E:/ferramentas/maven-repository" test
```

O parâmetro do repositório identifica o cache desta máquina. Em outro ambiente com as dependências disponíveis, o comando do grupo é `mvn test`.

Resultado: **BUILD SUCCESS**. A suíte completa contabilizou 156 testes, sem falhas ou erros, com 14 desabilitados por defeitos conhecidos documentados pelos demais integrantes. Os **84 testes de PetDAO passaram**, sem testes desabilitados. O resumo Surefire de PetDAO está em [DAO.PetDAOTest.txt](DAO.PetDAOTest.txt).

Os casos CT-PD compreendem 64 combinações de presença/ausência dos seis filtros, 18 entradas limite/textuais e dois casos de retorno vazio e propagação de falha da dependência. A [planilha](CT-PD_PetDAO_Thiago.xlsx) e o [CSV](casos-teste-CT-PD.csv) relacionam entradas, oráculos, resultados e vereditos.

Os mocks isolam `EntityManagerSingleton`, `EntityManager` e `TypedQuery`. As asserções verificam a consulta montada, a vinculação dos parâmetros, a identidade da lista retornada e as interações com as dependências. Não comprovam a sintaxe aceita pelo provedor JPA nem quais pets um banco real seleciona. Essa verificação pertence aos testes de integração da Entrega 2. Nenhum defeito novo foi confirmado nesta execução unitária.
