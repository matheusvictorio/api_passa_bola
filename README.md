# 🏆 API Passa Bola - Guia Completo

> API REST para plataforma de futebol feminino com sistema de jogadoras, organizações, espectadores, jogos e posts.

## 📋 Índice
- [🚀 Configuração Inicial](#-configuração-inicial)
- [🏗️ Arquitetura da API](#️-arquitetura-da-api)
- [🔐 Autenticação e Autorização](#-autenticação-e-autorização)
- [📡 Endpoints da API](#-endpoints-da-api)
- [💡 Exemplos Práticos](#-exemplos-práticos)
- [🔧 Troubleshooting](#-troubleshooting)

## 🚀 Configuração Inicial

### Pré-requisitos
- **Java 21+**
- **MySQL 8.0+** (ou H2 para testes)
- **Maven 3.6+**
- **Postman** ou ferramenta similar

### 1. Configurar Banco de Dados

#### MySQL (Produção)
```sql
-- Criar banco de dados
CREATE DATABASE api_passa_bola;
CREATE USER 'api_user'@'localhost' IDENTIFIED BY 'senha123';
GRANT ALL PRIVILEGES ON api_passa_bola.* TO 'api_user'@'localhost';
FLUSH PRIVILEGES;
```

#### H2 (Desenvolvimento)
```properties
# application-dev.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

### 2. Configurar Variáveis de Ambiente
```bash
export DB_USER=api_user
export DB_PASSWORD=senha123
export JWT_SECRET=minha_chave_secreta_super_segura
```

### 3. Executar a Aplicação
```bash
# Compilar
./mvnw clean compile

# Executar
./mvnw spring-boot:run

# Ou com perfil específico
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

**🌐 Aplicação disponível em:** `http://localhost:8080`

## 🏗️ Arquitetura da API

### Entidades Principais (Estrutura Flattened)

#### 👩‍⚽ Player (Jogadora)
```json
{
  "id": 1,
  "userType": "PLAYER",
  "username": "maria_silva",
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "***",
  "bio": "Atacante profissional",
  "followers": 150,
  "following": 75,
  "gamesPlayed": 45,
  "birthDate": "1995-03-15",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "organizationId": 1,
  "pastOrganization": "Santos FC",
  "phone": "(11) 99999-9999"
}
```

#### 🏟️ Organization (Time/Organização)
```json
{
  "id": 1,
  "userType": "ORGANIZATION",
  "username": "santos_fc",
  "name": "Santos FC Feminino",
  "email": "contato@santos.com",
  "cnpj": "12345678000199",
  "password": "***",
  "bio": "Time tradicional de futebol feminino",
  "followers": 5000,
  "following": 200,
  "gamesPlayed": 120,
  "profilePhotoUrl": "https://example.com/logo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "phone": "(13) 3333-3333"
}
```

#### 👥 Spectator (Espectador)
```json
{
  "id": 1,
  "userType": "SPECTATOR",
  "username": "joao_torcedor",
  "name": "João Santos",
  "email": "joao@email.com",
  "password": "***",
  "bio": "Apaixonado pelo futebol feminino",
  "followers": 50,
  "following": 100,
  "birthDate": "1988-07-20",
  "phone": "(11) 88888-8888",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "favoriteTeamId": 1
}
```

### Outras Entidades

#### ⚽ Game (Jogo)
```json
{
  "id": 1,
  "homeTeam": { "id": 1, "name": "Santos FC" },
  "awayTeam": { "id": 2, "name": "Corinthians" },
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Vila Belmiro",
  "championship": "Brasileirão Feminino",
  "round": "Semifinal",
  "status": "SCHEDULED",
  "homeGoals": 0,
  "awayGoals": 0
}
```

#### 📝 Post (Publicação)
```json
{
  "id": 1,
  "authorId": 1,
  "authorUsername": "maria_silva",
  "authorRole": "PLAYER",
  "content": "Preparando para o próximo jogo! 💪⚽",
  "imageUrl": "https://example.com/treino.jpg",
  "type": "GENERAL",
  "likes": 45,
  "comments": 12,
  "shares": 8,
  "createdAt": "2024-12-10T10:30:00"
}
```

## 🔐 Autenticação e Autorização

### Sistema JWT
Todos os endpoints protegidos requerem:
```http
Authorization: Bearer <jwt_token>
```

### Roles Disponíveis
- **PLAYER**: Jogadoras
- **ORGANIZATION**: Times/Organizações  
- **SPECTATOR**: Espectadores/Torcedores

### Fluxo de Autenticação
1. **Registro** → Criar conta com role específico
2. **Login** → Receber JWT token
3. **Usar token** → Incluir em requisições protegidas

## 📡 Endpoints da API

### 🔑 Autenticação (`/api/auth`)

#### Registro de Jogadora
```http
POST /api/auth/register/player
Content-Type: application/json

{
  "username": "maria_silva",
  "name": "Maria Silva",
  "email": "maria@email.com",
  "password": "senha123",
  "bio": "Atacante profissional",
  "birthDate": "1995-03-15",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "organizationId": 1,
  "pastOrganization": "Santos FC",
  "phone": "(11) 99999-9999"
}
```

#### Registro de Organização
```http
POST /api/auth/register/organization
Content-Type: application/json

{
  "username": "santos_fc",
  "name": "Santos FC Feminino",
  "email": "contato@santos.com",
  "cnpj": "12345678000199",
  "password": "senha123",
  "bio": "Time tradicional de futebol feminino",
  "profilePhotoUrl": "https://example.com/logo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "phone": "(13) 3333-3333"
}
```

#### Registro de Espectador
```http
POST /api/auth/register/spectator
Content-Type: application/json

{
  "username": "joao_torcedor",
  "name": "João Santos",
  "email": "joao@email.com",
  "password": "senha123",
  "bio": "Apaixonado pelo futebol feminino",
  "birthDate": "1988-07-20",
  "phone": "(11) 88888-8888",
  "profilePhotoUrl": "https://example.com/photo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "favoriteTeamId": 1
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "maria@email.com",
  "password": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userType": "PLAYER",
  "profileId": 1,
  "username": "maria_silva"
}
```

### 👩‍⚽ Jogadoras (`/api/players`)

```http
# Listar todas
GET /api/players?page=0&size=20

# Buscar por ID
GET /api/players/1

# Buscar por username
GET /api/players/username/maria_silva

# Buscar por nome
GET /api/players/search?name=Maria&page=0&size=10

# Buscar por organização
GET /api/players/organization/1?page=0&size=10

# Atualizar (requer auth PLAYER)
PUT /api/players/1
Authorization: Bearer <token>
{
  "name": "Maria Silva Santos",
  "bio": "Atacante e capitã do time",
  "organizationId": 1
}

# Seguir jogadora (requer auth)
POST /api/players/1/follow
Authorization: Bearer <token>

# Parar de seguir
DELETE /api/players/1/follow
Authorization: Bearer <token>

# Listar seguidores
GET /api/players/1/followers?page=0&size=20

# Listar seguindo
GET /api/players/1/following?page=0&size=20
```

### 🏟️ Organizações (`/api/organizations`)

```http
# Listar todas
GET /api/organizations?page=0&size=20

# Buscar por ID
GET /api/organizations/1

# Buscar por nome
GET /api/organizations/search?name=Santos&page=0&size=10

# Atualizar (requer auth ORGANIZATION)
PUT /api/organizations/1
Authorization: Bearer <token>
{
  "name": "Santos FC Feminino",
  "bio": "Tradicional time de futebol feminino"
}

# Seguir organização (requer auth)
POST /api/organizations/1/follow
Authorization: Bearer <token>
```

### 👥 Espectadores (`/api/spectators`)

```http
# Listar todos
GET /api/spectators?page=0&size=20

# Buscar por ID
GET /api/spectators/1

# Buscar por time favorito
GET /api/spectators/favorite-team/1?page=0&size=10

# Atualizar (requer auth SPECTATOR)
PUT /api/spectators/1
Authorization: Bearer <token>
{
  "name": "João Santos Silva",
  "bio": "Torcedor apaixonado",
  "favoriteTeamId": 2
}
```

### ⚽ Jogos (`/api/games`)

```http
# Listar todos
GET /api/games?page=0&size=20

# Buscar por ID
GET /api/games/1

# Buscar por organização
GET /api/games/organization/1?page=0&size=10

# Buscar por status
GET /api/games/status/SCHEDULED?page=0&size=10

# Buscar por campeonato
GET /api/games/championship?championship=Brasileirão&page=0&size=10

# Criar jogo (requer auth ORGANIZATION ou PLAYER)
POST /api/games
Authorization: Bearer <token>
{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Vila Belmiro",
  "championship": "Brasileirão Feminino",
  "round": "Semifinal"
}

# Atualizar jogo (requer auth ORGANIZATION ou PLAYER)
PUT /api/games/1
Authorization: Bearer <token>
{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Vila Belmiro",
  "championship": "Brasileirão Feminino",
  "round": "Semifinal"
}

# Deletar jogo (requer auth ORGANIZATION ou PLAYER)
DELETE /api/games/1
Authorization: Bearer <token>

# Atualizar placar (requer auth ORGANIZATION ou PLAYER)
PATCH /api/games/1/score?homeGoals=2&awayGoals=1
Authorization: Bearer <token>

# Inscrever-se no jogo (requer auth)
POST /api/games/1/subscribe
Authorization: Bearer <token>
```

### 📝 Posts (`/api/posts`)

```http
# Listar todos
GET /api/posts?page=0&size=20

# Buscar por ID
GET /api/posts/1

# Buscar por autor
GET /api/posts/author/1?page=0&size=10

# Buscar meus posts (requer auth)
GET /api/posts/my-posts?page=0&size=10
Authorization: Bearer <token>

# Buscar por role
GET /api/posts/role/PLAYER?page=0&size=10

# Buscar por tipo
GET /api/posts/type/MATCH_HIGHLIGHT?page=0&size=10

# Buscar mais curtidos
GET /api/posts/most-liked?page=0&size=10

# Buscar com imagens
GET /api/posts/with-images?page=0&size=10

# Buscar por conteúdo
GET /api/posts/search?content=gol&page=0&size=10

# Criar post (requer auth)
POST /api/posts
Authorization: Bearer <token>
{
  "content": "Preparando para o próximo jogo! 💪⚽",
  "type": "GENERAL",
  "imageUrl": "https://example.com/treino.jpg"
}

# Atualizar post (requer auth - apenas próprio post)
PUT /api/posts/1
Authorization: Bearer <token>
{
  "content": "Conteúdo atualizado",
  "type": "GENERAL"
}

# Deletar post (requer auth - apenas próprio post)
DELETE /api/posts/1
Authorization: Bearer <token>

# Curtir post (requer auth)
POST /api/posts/1/like
Authorization: Bearer <token>

# Descurtir post (requer auth)
POST /api/posts/1/unlike
Authorization: Bearer <token>

# Comentar post (requer auth)
POST /api/posts/1/comment
Authorization: Bearer <token>

# Compartilhar post (requer auth)
POST /api/posts/1/share
Authorization: Bearer <token>
```

## 💡 Exemplos Práticos

### Exemplo 1: Fluxo Completo de Jogadora

```bash
# 1. Registrar jogadora
curl -X POST http://localhost:8080/api/auth/register/player \
  -H "Content-Type: application/json" \
  -d '{
    "username": "ana_striker",
    "name": "Ana Costa",
    "email": "ana@email.com",
    "password": "senha123",
    "bio": "Atacante profissional",
    "birthDate": "1995-03-15",
    "organizationId": 1,
    "phone": "(11) 99999-9999"
  }'

# 2. Fazer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ana@email.com",
    "password": "senha123"
  }'

# 3. Criar post (usar token retornado)
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Gol no último minuto! ⚽🔥",
    "type": "MATCH_HIGHLIGHT",
    "imageUrl": "https://example.com/gol.jpg"
  }'

# 4. Seguir outra jogadora
curl -X POST http://localhost:8080/api/players/2/follow \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"

# 5. Criar jogo (jogadoras também podem criar jogos)
curl -X POST http://localhost:8080/api/games \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "gameDate": "2024-12-25T14:00:00",
    "venue": "Estádio do Pacaembu",
    "championship": "Copa São Paulo Feminina",
    "round": "Quartas de Final"
  }'
```

### Exemplo 2: Fluxo de Organização

```bash
# 1. Registrar organização
curl -X POST http://localhost:8080/api/auth/register/organization \
  -H "Content-Type: application/json" \
  -d '{
    "username": "corinthians_fem",
    "name": "Corinthians Feminino",
    "email": "feminino@corinthians.com",
    "cnpj": "12345678000199",
    "password": "senha123",
    "bio": "Time tradicional de futebol feminino",
    "phone": "(11) 2222-2222"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "feminino@corinthians.com",
    "password": "senha123"
  }'

# 3. Criar jogo
curl -X POST http://localhost:8080/api/games \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "homeTeamId": 1,
    "awayTeamId": 2,
    "gameDate": "2024-12-20T16:00:00",
    "venue": "Neo Química Arena",
    "championship": "Paulistão Feminino",
    "round": "Final"
  }'

# 4. Criar post da organização
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Preparação para a final! Vamos Corinthians! 🖤🤍",
    "type": "ORGANIZATION_UPDATE"
  }'
```

### Exemplo 3: Fluxo de Espectador

```bash
# 1. Registrar espectador
curl -X POST http://localhost:8080/api/auth/register/spectator \
  -H "Content-Type: application/json" \
  -d '{
    "username": "torcedor_fiel",
    "name": "Carlos Silva",
    "email": "carlos@email.com",
    "password": "senha123",
    "bio": "Torcedor apaixonado pelo futebol feminino",
    "birthDate": "1985-05-10",
    "favoriteTeamId": 1,
    "phone": "(11) 77777-7777"
  }'

# 2. Login e seguir jogadoras
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "carlos@email.com",
    "password": "senha123"
  }'

# 3. Seguir jogadora favorita
curl -X POST http://localhost:8080/api/players/1/follow \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"

# 4. Inscrever-se em jogo
curl -X POST http://localhost:8080/api/games/1/subscribe \
  -H "Authorization: Bearer SEU_TOKEN_AQUI"

# 5. Criar post de opinião
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Que jogo incrível! O futebol feminino está cada vez melhor! 👏⚽",
    "type": "SPECTATOR_OPINION"
  }'
```

## 📊 Códigos de Resposta HTTP

| Código | Status | Descrição |
|--------|--------|-----------|
| 200 | OK | Requisição bem-sucedida |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Operação bem-sucedida sem conteúdo |
| 400 | Bad Request | Dados inválidos na requisição |
| 401 | Unauthorized | Token inválido ou ausente |
| 403 | Forbidden | Sem permissão para a operação |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Conflito (ex: email já existe) |
| 500 | Internal Server Error | Erro interno do servidor |

## 🔧 Troubleshooting

### Problemas Comuns

#### 1. Erro de Conexão com Banco
```
Error: Access denied for user 'root'@'localhost'
```
**Solução:**
- Verificar se MySQL está rodando
- Confirmar credenciais no `application.properties`
- Criar usuário com permissões adequadas

#### 2. Token JWT Inválido
```json
{
  "error": "JWT token is invalid or expired"
}
```
**Solução:**
- Fazer login novamente para obter novo token
- Verificar se token está sendo enviado corretamente no header

#### 3. Permissão Negada
```json
{
  "error": "Access denied for this operation"
}
```
**Solução:**
- Verificar se o role do usuário tem permissão
- Confirmar se está autenticado corretamente

#### 4. Validação de Dados
```json
{
  "error": "Validation failed",
  "details": ["Email is required", "Username must be unique"]
}
```
**Solução:**
- Verificar campos obrigatórios
- Confirmar unicidade de email/username

### Configurações de Desenvolvimento

#### Perfil de Desenvolvimento (H2)
```properties
# application-dev.properties
spring.profiles.active=dev
spring.datasource.url=jdbc:h2:mem:testdb
spring.h2.console.enabled=true
spring.jpa.show-sql=true
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.springframework.security=DEBUG
```

#### Logs Úteis
```bash
# Ver logs da aplicação
tail -f logs/application.log

# Executar com debug
./mvnw spring-boot:run -Dspring-boot.run.arguments="--debug"

# Ver logs do Hibernate
./mvnw spring-boot:run -Dspring-boot.run.arguments="--logging.level.org.hibernate.SQL=DEBUG"
```

### Validação de Token JWT
Use [jwt.io](https://jwt.io) para decodificar tokens. Estrutura esperada:
```json
{
  "sub": "username",
  "userType": "PLAYER",
  "userId": 1,
  "exp": 1640995200,
  "iat": 1640908800
}
```

## 🎯 Boas Práticas

### Para Desenvolvedores Frontend
1. **Sempre incluir Authorization header** em endpoints protegidos
2. **Implementar refresh de token** quando expirar
3. **Tratar erros HTTP** adequadamente
4. **Usar paginação** para listas grandes
5. **Validar dados** antes de enviar

### Para Testes
1. **Usar Postman Collections** para automatizar testes
2. **Testar diferentes roles** e permissões
3. **Validar responses** e status codes
4. **Testar cenários de erro**

### Segurança
1. **Nunca expor JWT secrets** em código
2. **Usar HTTPS** em produção
3. **Implementar rate limiting**
4. **Validar todos os inputs**
5. **Logs de auditoria** para operações sensíveis

## 📞 Suporte e Contribuição

### Para Dúvidas
1. Consultar este README
2. Verificar logs da aplicação
3. Testar endpoints com Postman
4. Verificar configurações do banco

### Estrutura do Projeto
```
src/main/java/com/fiap/projects/apipassabola/
├── controller/          # Controllers REST
├── service/            # Lógica de negócio
├── repository/         # Acesso a dados
├── entity/            # Entidades JPA
├── dto/               # DTOs de request/response
├── security/          # Configurações de segurança
├── config/            # Configurações gerais
└── exception/         # Tratamento de exceções
```

---

**🏆 API Passa Bola v2.0**  
**📅 Última atualização:** Dezembro 2024  
**👥 Desenvolvido para:** Plataforma de Futebol Feminino
