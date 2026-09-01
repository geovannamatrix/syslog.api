# SYSLOG API

API REST em Java 21 / Spring Boot para **cadastro de usuários e gestão de credenciais**: criação de conta, atualização e exclusão de usuário, login por e-mail/senha e troca de senha. As senhas nunca são armazenadas em texto claro — são derivadas com o `DelegatingPasswordEncoder` do Spring Security (BCrypt por padrão, com o algoritmo identificado por prefixo no próprio hash).

O acesso ao banco é feito com **JDBC puro** (`JdbcTemplate` + SQL escrito à mão), sem JPA/Hibernate, e o schema é versionado por migrations do Flyway.

---

## Sumário

- [Stack](#stack)
- [Arquitetura](#arquitetura)
- [Modelo de dados](#modelo-de-dados)
- [Endpoints](#endpoints)
- [Tratamento de erros](#tratamento-de-erros)
- [Como rodar](#como-rodar)
- [Configuração](#configuração)
- [Documentação interativa (Swagger)](#documentação-interativa-swagger)
- [Estrutura de pastas](#estrutura-de-pastas)
- [Pendências conhecidas](#pendências-conhecidas)

---

## Stack

| Item | Versão / Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.7 (Web MVC) |
| Persistência | Spring JDBC (`JdbcTemplate`) |
| Banco | PostgreSQL 16 |
| Migrations | Flyway |
| Segurança | Spring Security (`PasswordEncoder`) |
| Mapeamento DTO ↔ entidade | MapStruct |
| Boilerplate | Lombok |
| Documentação | springdoc-openapi (Swagger UI) |
| Build | Maven (wrapper incluído) |
| Execução | Docker + Docker Compose |

---

## Arquitetura

Camadas clássicas, com o fluxo sempre no sentido `controller → service → repository`:

```
controller/     UserController, CredentialController      HTTP, status codes, OpenAPI
service/        UserService, CredentialService            regras de negócio e validações
                EncryptPasswordService                    hash e verificação de senha
model/repository UserRepository                           SQL (JdbcTemplate)
model/entity    User                                      POJO espelhando a tabela
model/dtos      *RequestDTO / *ResponseDTO                contratos de entrada e saída
model/mapper    UserMapper (MapStruct)                    conversões entre DTO e entidade
filter/         ControllerAdvisor                         tradução de exceção → resposta HTTP
exception/      BadRequest / NotFound / ExternalServerError
```

Pontos de projeto que valem destaque:

- **DTO nunca vira entidade na mão.** O `UserMapper` centraliza as conversões; a atualização parcial usa `NullValuePropertyMappingStrategy.IGNORE`, ou seja, campo ausente no request não sobrescreve o valor já gravado.
- **A senha só existe em claro dentro do `UserService`/`CredentialService`.** Ela é substituída pelo hash antes de chegar à entidade, e o `UserResponseDTO` devolve apenas o `userId`.
- **Toda resposta de sucesso é embrulhada em `AuthResponseDTO<T>`**, que carrega `response` e uma lista opcional de `errors`, mantendo um envelope único para o cliente.
- **E-mail é sempre normalizado para minúsculas** no controller/service antes de qualquer busca, evitando cadastros duplicados que diferem só por caixa.

### Verificação de senha

`EncryptPasswordService` encapsula três operações:

| Método | Uso |
|---|---|
| `encryptPassword(raw)` | gera o hash (salt aleatório embutido) no cadastro e na troca de senha |
| `isPasswordValid(hashArmazenado, senhaEnviada)` | comparação em tempo constante no login |
| `needsRehash(hashArmazenado)` | indica que o hash foi gerado com parâmetros/algoritmo antigos |

Como o encoder é um `DelegatingPasswordEncoder`, o hash é gravado com prefixo (`{bcrypt}...`). Isso permite trocar o algoritmo no futuro sem invalidar as senhas existentes: basta registrar o encoder antigo e regravar o hash no login quando `needsRehash` retornar `true`.

---

## Modelo de dados

Tabela `user_entity` (`src/main/resources/db/migration/V1__create_user.sql`):

| Coluna | Tipo | Observação |
|---|---|---|
| `id` | `BIGSERIAL` | chave primária |
| `name` | `TEXT NOT NULL` | nome do usuário |
| `email` | `TEXT NOT NULL` | índice único `ux_user_entity_email` |
| `username` | `TEXT NOT NULL` | login de exibição |
| `password` | `TEXT NOT NULL` | hash, nunca a senha em claro |
| `updated_at` | `TIMESTAMP NOT NULL` | `DEFAULT current_timestamp`; reescrito em todo `UPDATE` |

A data da última alteração não depende de a aplicação lembrar de preenchê-la: o próprio SQL de `INSERT`/`UPDATE` grava `current_timestamp`.

---

## Endpoints

Base: `http://localhost:8080`

### Usuário

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/v1/users` | Cria um usuário | `201 Created` |
| `PATCH` | `/api/v1/users/{email}` | Atualiza os dados do usuário identificado pelo e-mail | `200 OK` |
| `DELETE` | `/api/v1/users/{id}` | Exclui o usuário pelo id | `204 No Content` |

**Criar usuário**

```http
POST /api/v1/users
Content-Type: application/json

{
  "name": "Geovanna Duarte",
  "email": "geovanna@example.com",
  "username": "geovanna",
  "password": "senha-super-secreta"
}
```

```json
{
  "response": { "userId": 1 },
  "errors": null
}
```

Recusa com `400` se o e-mail já estiver cadastrado (`Sent Email already has a registered password`).

**Atualizar usuário** — atualização parcial: envie apenas os campos que mudam. Se `password` vier preenchido, ele é re-hasheado; se vier nulo ou vazio, a senha atual é preservada.

```http
PATCH /api/v1/users/geovanna@example.com
Content-Type: application/json

{ "username": "geovanna.duarte" }
```

**Excluir usuário**

```http
DELETE /api/v1/users/1
```

### Credencial

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `POST` | `/api/v1/credential` | Login por e-mail e senha | `200 OK` |
| `PUT` | `/api/v1/credential` | Troca de senha | `200 OK` |

**Login**

```http
POST /api/v1/credential
Content-Type: application/json

{
  "email": "geovanna@example.com",
  "password": "senha-super-secreta"
}
```

Retorna `200` sem corpo quando as credenciais conferem, `404` se o e-mail não existe e `400` se a senha está errada (`Username or Password are not Valid`).

**Trocar senha** — exige a senha atual:

```http
PUT /api/v1/credential
Content-Type: application/json

{
  "email": "geovanna@example.com",
  "password": "senha-atual",
  "newPassword": "senha-nova"
}
```

---

## Tratamento de erros

O `ControllerAdvisor` (`@ControllerAdvice`) traduz as exceções de domínio em respostas padronizadas:

| Exceção | Status HTTP |
|---|---|
| `NotFoundException` | `404 Not Found` |
| `BadRequestException` | `400 Bad Request` |
| `ExternalServerErrorException` | `500 Internal Server Error` |
| qualquer outra `Exception` | `500 Internal Server Error` |

Formato do corpo:

```json
{
  "code": "400 BAD_REQUEST",
  "message": "Sent Email already has a registered password"
}
```

---

## Como rodar

### Com Docker Compose (aplicação + banco)

```bash
docker compose up --build
```

Sobe o PostgreSQL (`database_postgres`, porta `5432`) e a API (`app_api`, porta `8080`). A aplicação só inicia depois do healthcheck (`pg_isready`) do banco passar, e as migrations do Flyway rodam automaticamente no startup.

### Só o banco, aplicação pela IDE / Maven

```bash
docker compose up database
./mvnw spring-boot:run
```

Fora do Compose, os defaults do `application.yml` apontam para `localhost:5432`, então nenhuma variável precisa ser definida.

Se uma execução anterior malsucedida deixou um volume do PostgreSQL, execute `docker compose down -v` antes de subir novamente para que a migration V1 corrigida seja aplicada.

### Build e testes

```bash
./mvnw clean package     # gera o jar em target/
./mvnw test
java -jar target/*.jar
```

O `Dockerfile` é multi-stage: o primeiro estágio (`amazoncorretto:21-alpine3.18-jdk` + Maven) compila e empacota, e o segundo (`amazoncorretto:21-alpine3.18`, só JRE) carrega apenas o `application.jar`. O `pom.xml` é copiado antes do `src` para que o download das dependências fique em cache entre builds.

---

## Configuração

Tudo em `src/main/resources/application.yml`, sem profiles. As variáveis de ambiente sobrescrevem os defaults:

| Variável | Default | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/postgres` | JDBC URL (no Compose: host `database`) |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `password` | senha do banco |
| `SWAGGER_CONFIG_URL` | `/api/v3/api-docs/swagger-config` | config do Swagger UI |
| `SWAGGER_URL` | `/api/v3/api-docs` | documento OpenAPI |
| `SWAGGER_API_URL` | `http://localhost:8080/api` | URL base exibida no Swagger |

Outros ajustes relevantes: pool Hikari limitado a 5 conexões, Flyway lendo de `classpath:db/migration` e log em `DEBUG` para `org.springframework.jdbc.core` (imprime o SQL executado — útil por o SQL ser escrito à mão, mas deve ser reduzido para `INFO` em produção).

As credenciais versionadas neste repositório servem **apenas para execução local**; qualquer ambiente real deve receber os valores por variável de ambiente ou segredo gerenciado.

---

## Documentação interativa (Swagger)

Com a aplicação no ar:

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Os controllers e DTOs estão anotados com `@Operation`, `@ApiResponses` e `@Schema`, então a documentação reflete as rotas e os contratos reais.

---

## Estrutura de pastas

```
.
├── Dockerfile                        # build multi-stage da imagem da API
├── docker-compose.yml                # API + PostgreSQL
├── pom.xml
└── src
    ├── main
    │   ├── java/com/syslog/api
    │   │   ├── Application.java
    │   │   ├── controller/           # UserController, CredentialController
    │   │   ├── service/              # UserService, CredentialService, EncryptPasswordService
    │   │   ├── model
    │   │   │   ├── dtos/             # contratos de entrada e saída
    │   │   │   ├── entity/User.java  # POJO da tabela user_entity
    │   │   │   ├── mapper/           # MapStruct
    │   │   │   └── repository/       # UserRepository (JdbcTemplate)
    │   │   ├── exception/            # exceções de domínio
    │   │   └── filter/               # ControllerAdvisor
    │   └── resources
    │       ├── application.yml
    │       └── db/migration/V1__create_user.sql
    └── test/java/com/syslog/api/ApplicationTests.java
```

---

## Pendências conhecidas

Itens que ainda divergem do comportamento esperado:

1. **Sem cobertura de testes**: existe apenas o `ApplicationTests` gerado pelo Spring Initializr.
2. **Defaults do Swagger apontam para um contexto inexistente**: os valores `SWAGGER_CONFIG_URL`, `SWAGGER_URL` e `SWAGGER_API_URL` usam o contexto `/api`, mas não há `server.servlet.context-path` configurado em `application.yml`.
