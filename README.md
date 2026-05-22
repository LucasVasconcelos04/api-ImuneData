# ImuniData — Sistema de Monitoramento de Vacinação

API REST desenvolvida em Java + Spring Boot para consulta e análise de dados sobre cobertura vacinal por região e faixa etária. O objetivo é transformar dados brutos em uma ferramenta de apoio à tomada de decisão para secretarias de saúde e unidades de pronto atendimento.

---

## Repositório do Frontend

O frontend React (interface visual) deste projeto está em um repositório separado:

[api-imunedata-frontend](https://github.com/LucasVasconcelos04/api-imunedata-frontend)

Para utilizar o sistema completo, é necessário clonar e executar ambos os projetos.

---

## Sumário

- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Como executar](#como-executar)
- [Dicionário de Dados](#dicionário-de-dados)
- [Mapeamento de Rotas](#mapeamento-de-rotas)
- [Importação de CSV](#importação-de-csv)
- [Justificativa Técnica: Uso de Optional](#justificativa-técnica-uso-de-optional)
- [Prints de Funcionamento](#prints-de-funcionamento)
- [Autores](#autores)

---

## Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Persistência | Spring Data JPA + Hibernate |
| Banco de Dados | H2 Database (modo arquivo) |
| Leitura de CSV | OpenCSV 5.9 |
| Build | Maven |
| Testes de API | Insomnia |
| Frontend | React (Vite) |

---

## Arquitetura

O projeto segue rigorosamente a arquitetura em camadas:

```
br.com.luquinhas.apiimunedata/
├── controller/    → Camada de exposição HTTP (endpoints REST)
├── service/       → Camada de regra de negócio
├── repository/    → Camada de acesso a dados (Spring Data JPA)
└── entity/        → Camada de modelo (entidades JPA)
```

Fluxo de uma requisição:

```
Cliente HTTP → Controller → Service → Repository → Banco H2
```

Cada camada conhece apenas a camada imediatamente abaixo, garantindo baixo acoplamento e alta coesão.

---

## Como executar

### Pré-requisitos

- JDK 21 instalado
- Maven 3.6+ ou IntelliJ IDEA com suporte a Maven

### Passos

1. Clone o repositório:
```bash
   git clone https://github.com/LucasVasconcelos04/api-ImuneData.git
   cd api-ImuneData
```

2. Execute via Maven:
```bash
   mvn spring-boot:run
```

   Ou abra no IntelliJ e clique em ▶ na classe `ApiImuneDataApplication`.

3. A API estará disponível em: `http://localhost:8080`

4. O console do banco H2 estará disponível em: `http://localhost:8080/h2-console`
   - **JDBC URL:** `jdbc:h2:file:./data/imunedata`
   - **User:** `sa`
   - **Password:** (em branco)

---

## Dicionário de Dados

Entidade: **`RegistroVacinacao`** (tabela `registro_vacinacao`)

| Campo | Tipo Java | Tipo SQL | Obrigatório | Descrição |
|---|---|---|---|---|
| `id` | `Long` | `BIGINT` (auto-incremento) | Gerado | Identificador único do registro. Gerado automaticamente pelo banco. |
| `municipio` | `String` | `VARCHAR(100)` | Sim | Nome do município onde a vacinação foi aplicada. |
| `estado` | `String` | `VARCHAR(2)` | Sim | Sigla da unidade federativa (ex: SP, RJ, MG). |
| `vacina` | `String` | `VARCHAR(50)` | Sim | Tipo da vacina aplicada (ex: BCG, Gripe, COVID-19). |
| `dose` | `Dose` (enum) | `VARCHAR(20)` | Sim | Dose aplicada. Valores possíveis: `PRIMEIRA`, `SEGUNDA`, `REFORCO`. |
| `quantidadeAplicada` | `Integer` | `INTEGER` | Sim | Número total de doses aplicadas no registro. |
| `dataRegistro` | `LocalDate` | `DATE` | Sim | Data em que o registro foi efetuado. |

### Por que o campo dose é um Enum?

Optei por modelar `dose` como enum em vez de `String` livre para:
- **Garantir integridade dos dados:** impede que valores inválidos sejam salvos no banco (ex: "primeria", "1a dose", "DOSE_UM").
- **Facilitar manutenção:** centralizar os valores válidos em um único ponto do código.
- **Autocomplete na IDE:** desenvolvedores não precisam decorar os valores possíveis.

A persistência usa `@Enumerated(EnumType.STRING)` para salvar o nome do enum como texto no banco, garantindo legibilidade e segurança em caso de reordenação no código.

### Enum auxiliar Regiao

Para o filtro por região geográfica, foi criado o enum `Regiao` que mapeia as cinco regiões do Brasil para suas respectivas siglas de estado:

| Região | Estados |
|---|---|
| `NORTE` | AC, AP, AM, PA, RO, RR, TO |
| `NORDESTE` | AL, BA, CE, MA, PB, PE, PI, RN, SE |
| `CENTRO_OESTE` | DF, GO, MT, MS |
| `SUDESTE` | ES, MG, RJ, SP |
| `SUL` | PR, RS, SC |

Esse enum não é persistido no banco — é usado apenas como utilitário para traduzir uma região em uma lista de estados na consulta `findByEstadoIn`.

---

## Mapeamento de Rotas

Base URL: `http://localhost:8080`

### Operações CRUD

| Método | Endpoint | Descrição | Status de Sucesso | Status de Erro |
|---|---|---|---|---|
| `GET` | `/api/vacinacao` | Lista todos os registros | 200 OK | — |
| `GET` | `/api/vacinacao/{id}` | Busca um registro por ID | 200 OK | 404 Not Found |
| `POST` | `/api/vacinacao` | Cria um novo registro | 201 Created | 400 Bad Request |
| `PUT` | `/api/vacinacao/{id}` | Atualiza um registro existente | 200 OK | 404 Not Found |
| `DELETE` | `/api/vacinacao/{id}` | Remove um registro | 204 No Content | 404 Not Found |

### Consultas Especializadas

| Método | Endpoint | Descrição | Status |
|---|---|---|---|
| `GET` | `/api/vacinacao/vacina/{vacina}` | Filtra registros por tipo de vacina | 200 OK |
| `GET` | `/api/vacinacao/estado/{estado}` | Filtra registros por estado (sigla UF) | 200 OK |
| `GET` | `/api/vacinacao/regiao/{regiao}` | Filtra registros por região geográfica | 200 OK / 400 Bad Request |

Valores aceitos para `{regiao}`: `NORTE`, `NORDESTE`, `CENTRO_OESTE`, `SUDESTE`, `SUL` (case-insensitive).

### Importação de Dados

| Método | Endpoint | Descrição | Status de Sucesso | Status de Erro |
|---|---|---|---|---|
| `POST` | `/api/vacinacao/upload-csv` | Importa registros via arquivo CSV | 201 Created | 400 Bad Request, 500 Internal Server Error |

### Exemplo de payload para POST/PUT

```json
{
  "municipio": "Santo André",
  "estado": "SP",
  "vacina": "BCG",
  "dose": "PRIMEIRA",
  "quantidadeAplicada": 150,
  "dataRegistro": "2026-05-15"
}
```

---

## Importação de CSV

O endpoint `POST /api/vacinacao/upload-csv` permite popular o banco a partir de um arquivo CSV.

### Formato esperado

- **Separador:** ponto-e-vírgula (`;`) — padrão brasileiro
- **Encoding:** UTF-8
- **Primeira linha:** cabeçalho (será ignorado)

### Exemplo de arquivo

```csv
municipio;estado;vacina;dose;quantidadeAplicada;dataRegistro
Santo André;SP;BCG;PRIMEIRA;150;2026-01-15
São Paulo;SP;Gripe;REFORCO;8000;2026-02-10
Rio de Janeiro;RJ;COVID-19;SEGUNDA;3200;2026-03-05
```

### Como testar no Insomnia

1. Método: `POST`
2. URL: `http://localhost:8080/api/vacinacao/upload-csv`
3. Body: `Multipart Form`
4. Campo: `arquivo` (tipo: File), apontando para o `.csv` desejado

Resposta esperada:

```json
{
  "mensagem": "Importacao realizada com sucesso",
  "totalRegistrosImportados": 20,
  "nomeArquivo": "vacinacao.csv"
}
```

### Sobre os dados

Como os arquivos reais do OpenDataSUS têm dezenas de gigabytes e milhões de linhas (inviáveis para fins acadêmicos), foi criada uma amostra simplificada com as mesmas colunas da entidade, baseada no formato dos dados públicos do DataSUS.

---

## Justificativa Técnica: Uso de Optional

### O problema dos valores nulos

Em Java, qualquer referência pode ser `null`. Isso significa que ao chamar um método que retorna um objeto, o desenvolvedor pode esquecer de verificar se o retorno é `null` antes de usá-lo, causando `NullPointerException` em tempo de execução — um dos erros mais comuns e perigosos da linguagem.

Tony Hoare, criador do conceito de referência nula em 1965, chamou sua própria invenção de "o erro de um bilhão de dólares", justamente pelo prejuízo que causa em sistemas reais.

### Como Optional resolve

`Optional<T>` é uma "caixa" que pode conter um valor (`Optional.of(valor)`) ou estar vazia (`Optional.empty()`). Ao retornar `Optional`, o método comunica explicitamente que o valor pode não existir, forçando quem consome o método a tratar esse caso.

### Aplicação no projeto

No método `buscarPorId` do `RegistroVacinacaoService`:

```java
public Optional<RegistroVacinacao> buscarPorId(Long id) {
    return repository.findById(id);
}
```

E no Controller, o uso de `Optional` permite uma resposta HTTP elegante:

```java
return service.buscarPorId(id)
        .map(registro -> ResponseEntity.ok(registro))
        .orElse(ResponseEntity.notFound().build());
```

### Benefícios obtidos

1. **Segurança em tempo de compilação:** o compilador exige que o desenvolvedor trate o caso "não encontrado", eliminando a possibilidade de `NullPointerException`.

2. **Código autodocumentado:** ao ver o retorno `Optional<RegistroVacinacao>`, qualquer desenvolvedor entende que o registro pode não existir.

3. **Programação funcional:** métodos como `map`, `orElse`, `ifPresent` permitem código mais conciso e expressivo do que `if (x != null)` espalhado pelo código.

4. **Tradução natural para HTTP:** `Optional` vazio se traduz perfeitamente em `404 Not Found`, alinhando o modelo de dados ao protocolo REST.

---

## Prints de Funcionamento

### Backend — testes no Insomnia

**1. Criação de registro (POST 201 Created)**

<img width="1413" height="465" alt="01-post-criar" src="https://github.com/user-attachments/assets/b4bb007e-d575-4cc0-80dc-db03d5add617" />

**2. Listagem de todos os registros (GET 200 OK)**

<img width="1394" height="925" alt="02-get-listar" src="https://github.com/user-attachments/assets/afe8076f-e06a-474e-b507-23df82906516" />

**3. Busca por ID inexistente (GET 404 Not Found)**

<img width="1396" height="496" alt="03-get-404" src="https://github.com/user-attachments/assets/6c80bf65-abb0-4cac-a168-8ecddb3f9bd3" />

**4. Filtro por região (GET 200 OK)**

<img width="1357" height="909" alt="04-filtro-regiao" src="https://github.com/user-attachments/assets/159a74c3-1cf5-4983-ab9f-cd15e08eb0c7" />

**5. Importação de CSV (POST 201 Created)**

<img width="1250" height="374" alt="05-upload-csv" src="https://github.com/user-attachments/assets/0851b124-f56b-433d-8efd-103f27b2add1" />

### Banco de dados — H2 Console

**6. Dados persistidos no H2**

<img width="1246" height="945" alt="06-h2-console" src="https://github.com/user-attachments/assets/3db7135f-8bd3-4759-b5a3-e291fbd7fc84" />

### Frontend — React

**7. Dashboard com filtros aplicados (Aba Registros)**

<img width="1875" height="939" alt="07-frontend-tabela" src="https://github.com/user-attachments/assets/3ae2d9f2-171e-4e73-ac63-68ef926a7545" />

**8. Formulário de cadastro (Aba Cadastrar)**

<img width="1888" height="956" alt="08-frontend-cadastro" src="https://github.com/user-attachments/assets/ffc91e38-463b-452a-b13d-6654e08a425b" />

---

## Autores

- **Lucas Vasconcelos Gonçalves de Souza** — [GitHub](https://github.com/LucasVasconcelos04)

Trabalho desenvolvido para a disciplina ministrada pela Prof. Mestre Sirley Ambrosia Vitorio Addão, FATEC Ipiranga.

---

## Licença

Lucas Vasconcelos G. de Souza.
