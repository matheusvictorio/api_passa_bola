# API Passa Bola - Guia de Uso Completo

## 📋 Índice
1. [Configuração Inicial](#configuração-inicial)
2. [Estrutura da API](#estrutura-da-api)
3. [Autenticação e Autorização](#autenticação-e-autorização)
4. [Endpoints Disponíveis](#endpoints-disponíveis)
5. [Exemplos de Uso](#exemplos-de-uso)
6. [Códigos de Resposta](#códigos-de-resposta)
7. [Troubleshooting](#troubleshooting)

## 🚀 Configuração Inicial

### Pré-requisitos
- Java 21+
- MySQL 8.0+ (ou configurar H2 para testes)
- Maven 3.6+
- Postman ou similar para testes de API

### 1. Configurar Banco de Dados

#### Opção A: MySQL (Recomendado para produção)
```bash
# Instalar MySQL e criar banco
mysql -u root -p
CREATE DATABASE api_passa_bola;
```

#### Opção B: H2 (Para testes rápidos)
```properties
# No application.properties, substituir configuração MySQL por:
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=password
spring.datasource.driver-class-name=org.h2.Driver
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### 2. Configurar Variáveis de Ambiente
```bash
export DB_USER=root
export DB_PASSWORD=sua_senha_mysql
```

### 3. Iniciar a Aplicação
```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## 🏗️ Estrutura da API

### Roles (Papéis) Disponíveis
- **PLAYER**: Jogadoras de futebol
- **ORGANIZATION**: Times/Organizações
- **SPECTATOR**: Espectadores/Torcedores

### Entidades Principais
- **User**: Usuário base com autenticação
- **Player**: Jogadora (referencia User)
- **Organization**: Time/Organização (referencia User)
- **Spectator**: Espectador (referencia User)
- **Game**: Jogos entre times
- **Post**: Publicações das jogadoras

## 🔐 Autenticação e Autorização

### Sistema JWT
A API utiliza JWT (JSON Web Tokens) para autenticação. Todos os endpoints protegidos requerem o header:
```
Authorization: Bearer <seu_jwt_token>
```

### Fluxo de Autenticação
1. **Registro** → Criar conta com role específico
2. **Login** → Receber JWT token
3. **Usar token** → Incluir em todas as requisições protegidas

## 📡 Endpoints Disponíveis

### 🔑 Autenticação (`/api/auth`)

#### Registro de Jogadora
```http
POST /api/auth/register/player
Content-Type: application/json

{
  "username": "maria_silva",
  "email": "maria@email.com",
  "password": "senha123",
  "firstName": "Maria",
  "lastName": "Silva",
  "bio": "Atacante profissional",
  "birthDate": "1995-03-15",
  "position": "FORWARD",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "jerseyNumber": 10,
  "organizationId": 1
}
```

#### Registro de Organização
```http
POST /api/auth/register/organization
Content-Type: application/json

{
  "username": "santos_fc",
  "email": "contato@santos.com",
  "password": "senha123",
  "name": "Santos FC Feminino",
  "description": "Time profissional de futebol feminino",
  "city": "Santos",
  "state": "SP",
  "foundedYear": 1912,
  "logoUrl": "https://example.com/logo.jpg",
  "primaryColors": "Branco e Preto",
  "websiteUrl": "https://santos.com",
  "contactEmail": "feminino@santos.com",
  "contactPhone": "(13) 99999-9999"
}
```

#### Registro de Espectador
```http
POST /api/auth/register/spectator
Content-Type: application/json

{
  "username": "joao_torcedor",
  "email": "joao@email.com",
  "password": "senha123",
  "firstName": "João",
  "lastName": "Santos",
  "bio": "Torcedor apaixonado pelo futebol feminino",
  "birthDate": "1988-07-20",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "favoriteTeamId": 1
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "maria_silva",
  "password": "senha123"
}
```

**Resposta do Login:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "maria_silva",
  "role": "PLAYER",
  "profileId": 1
}
```

### 👩‍⚽ Jogadoras (`/api/players`)

#### Listar Jogadoras
```http
GET /api/players?page=0&size=20
```

#### Buscar Jogadora por ID
```http
GET /api/players/1
```

#### Buscar por Username
```http
GET /api/players/username/maria_silva
```

#### Buscar por Nome
```http
GET /api/players/search?name=Maria&page=0&size=10
```

#### Buscar por Time
```http
GET /api/players/organization/1?page=0&size=10
```

#### Buscar por Posição
```http
GET /api/players/position/FORWARD?page=0&size=10
```

#### Atualizar Jogadora (Requer autenticação PLAYER ou ORGANIZATION)
```http
PUT /api/players/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "firstName": "Maria",
  "lastName": "Silva Santos",
  "bio": "Atacante e capitã do time",
  "position": "FORWARD",
  "jerseyNumber": 10,
  "organizationId": 1
}
```

#### Seguir Jogadora (Requer autenticação PLAYER ou SPECTATOR)
```http
POST /api/players/2/follow/1
Authorization: Bearer <token>
```

#### Parar de Seguir Jogadora
```http
DELETE /api/players/2/follow/1
Authorization: Bearer <token>
```

#### Listar Seguidores
```http
GET /api/players/1/followers?page=0&size=20
```

#### Listar Seguindo
```http
GET /api/players/1/following?page=0&size=20
```

### 🏟️ Organizações (`/api/organizations`)

#### Listar Organizações
```http
GET /api/organizations?page=0&size=20
```

#### Buscar Organização por ID
```http
GET /api/organizations/1
```

#### Buscar por Nome
```http
GET /api/organizations/search?name=Santos&page=0&size=10
```

#### Buscar por Cidade
```http
GET /api/organizations/city/Santos?page=0&size=10
```

#### Buscar por Estado
```http
GET /api/organizations/state/SP?page=0&size=10
```

#### Atualizar Organização (Requer autenticação ORGANIZATION)
```http
PUT /api/organizations/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Santos FC Feminino",
  "description": "Tradicional time de futebol feminino",
  "city": "Santos",
  "state": "SP",
  "primaryColors": "Branco e Preto"
}
```

### ⚽ Jogos (`/api/games`)

#### Listar Jogos
```http
GET /api/games?page=0&size=20
```

#### Buscar Jogo por ID
```http
GET /api/games/1
```

#### Buscar Jogos por Time
```http
GET /api/games/organization/1?page=0&size=10
```

#### Buscar por Status
```http
GET /api/games/status/SCHEDULED?page=0&size=10
```

#### Buscar por Campeonato
```http
GET /api/games/championship?championship=Brasileirão&page=0&size=10
```

#### Criar Jogo (Requer autenticação ORGANIZATION)
```http
POST /api/games
Authorization: Bearer <token>
Content-Type: application/json

{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Estádio Vila Belmiro",
  "championship": "Brasileirão Feminino",
  "round": "Semifinal"
}
```

#### Atualizar Placar (Requer autenticação ORGANIZATION)
```http
PATCH /api/games/1/score?homeGoals=2&awayGoals=1
Authorization: Bearer <token>
```

### 📝 Posts (`/api/posts`)

#### Listar Posts
```http
GET /api/posts?page=0&size=20
```

#### Buscar Posts por Jogadora
```http
GET /api/posts/player/1?page=0&size=10
```

#### Buscar Posts Mais Curtidos
```http
GET /api/posts/most-liked?page=0&size=10
```

#### Criar Post (Requer autenticação PLAYER)
```http
POST /api/posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "playerId": 1,
  "content": "Preparando para o próximo jogo! 💪⚽",
  "type": "GENERAL",
  "imageUrl": "https://example.com/treino.jpg"
}
```

#### Curtir Post (Requer autenticação)
```http
POST /api/posts/1/like
Authorization: Bearer <token>
```

#### Descurtir Post
```http
DELETE /api/posts/1/like
Authorization: Bearer <token>
```

### 👥 Espectadores (`/api/spectators`)

#### Listar Espectadores
```http
GET /api/spectators?page=0&size=20
```

#### Buscar por Time Favorito
```http
GET /api/spectators/favorite-team/1?page=0&size=10
```

## 💡 Exemplos de Uso Completos

### Exemplo 1: Fluxo Completo de Jogadora

```bash
# 1. Registrar jogadora
curl -X POST http://localhost:8080/api/auth/register/player \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ana_striker",
    "email": "ana@email.com",
    "password": "senha123",
    "firstName": "Ana",
    "lastName": "Costa",
    "position": "FORWARD",
    "jerseyNumber": 9
  }'

# 2. Fazer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ana_striker",
    "password": "senha123"
  }'

# 3. Usar o token retornado para criar um post
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "playerId": 1,
    "content": "Gol no último minuto! ⚽🔥",
    "type": "MATCH_HIGHLIGHT"
  }'
```

### Exemplo 2: Fluxo de Organização

```bash
# 1. Registrar organização
curl -X POST http://localhost:8080/api/auth/register/organization \
  -H "Content-Type: application/json" \
  -d '{
    "username": "corinthians_fem",
    "email": "feminino@corinthians.com",
    "password": "senha123",
    "name": "Corinthians Feminino",
    "city": "São Paulo",
    "state": "SP",
    "foundedYear": 1910
  }'

# 2. Login e criar jogo
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "corinthians_fem",
    "password": "senha123"
  }'

curl -X POST http://localhost:8080/api/games \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "gameDate": "2024-12-20T16:00:00",
    "venue": "Neo Química Arena",
    "championship": "Paulistão Feminino"
  }'
```

## 📊 Códigos de Resposta

| Código | Significado | Descrição |
|--------|-------------|-----------|
| 200 | OK | Requisição bem-sucedida |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Operação bem-sucedida sem conteúdo |
| 400 | Bad Request | Dados inválidos na requisição |
| 401 | Unauthorized | Token inválido ou ausente |
| 403 | Forbidden | Sem permissão para a operação |
| 404 | Not Found | Recurso não encontrado |
| 500 | Internal Server Error | Erro interno do servidor |

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. Erro de Conexão com Banco
```
Error: Cannot create connection to database
```
**Solução**: Verificar se MySQL está rodando e credenciais estão corretas.

#### 2. Token Inválido
```json
{
  "error": "JWT token is invalid"
}
```
**Solução**: Fazer login novamente para obter novo token.

#### 3. Permissão Negada
```json
{
  "error": "Access denied"
}
```
**Solução**: Verificar se o role do usuário tem permissão para a operação.

### Logs Úteis
```bash
# Ver logs da aplicação
tail -f logs/application.log

# Ver logs do Spring Boot
./mvnw spring-boot:run --debug
```

### Validação de Token JWT
Para verificar se um token JWT é válido, você pode usar ferramentas online como [jwt.io](https://jwt.io) ou verificar os claims:

```json
{
  "sub": "username",
  "role": "PLAYER",
  "userId": 1,
  "playerId": 1,
  "exp": 1640995200
}
```

## 🎯 Dicas de Uso

1. **Sempre incluir o header Authorization** em endpoints protegidos
2. **Usar paginação** para listas grandes (`?page=0&size=20`)
3. **Verificar roles** antes de fazer operações específicas
4. **Tratar erros** adequadamente nas aplicações cliente
5. **Renovar tokens** quando necessário (expiram em 24h por padrão)

## 📞 Suporte

Para dúvidas ou problemas:
1. Verificar logs da aplicação
2. Consultar este guia
3. Testar endpoints com Postman
4. Verificar configurações do banco de dados

---

**Versão da API**: 1.0.0  
**Última atualização**: Dezembro 2024
