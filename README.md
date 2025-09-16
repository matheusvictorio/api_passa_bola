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

### ⚽ Sistema de Jogos (`/api/games`)

O sistema de jogos foi **completamente refatorado** para suportar **três tipos distintos de jogos**, cada um com suas próprias regras de negócio e funcionalidades específicas.

#### 🎯 Tipos de Jogos Disponíveis

| Tipo | Criado Por | Participação | Descrição |
|------|------------|--------------|-----------|
| **🤝 FRIENDLY** | Jogadoras | Individual ou Time | Jogos amistosos casuais |
| **🏆 CHAMPIONSHIP** | Jogadoras | Individual ou Time | Jogos de campeonato competitivos |
| **🏅 CUP** | Organizações | Sistema de Convites | Jogos oficiais de copa |

#### 📊 Estrutura Atualizada do Game

```json
{
  "id": 1,
  "gameType": "FRIENDLY",
  "gameName": "Pelada do Final de Semana",
  "hostUsername": "maria_silva",
  "hostId": 123,
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Campo do Bairro",
  "description": "Jogo descontraído entre amigas",
  "status": "SCHEDULED",
  "homeGoals": 0,
  "awayGoals": 0,
  "winner": null,
  "winningTeamSide": null,
  "createdAt": "2024-12-10T10:00:00",
  "updatedAt": "2024-12-10T10:00:00"
}
```

#### 🚀 Endpoints Principais

##### 📍 Criação de Jogos por Tipo

```http
# Criar Jogo Amistoso (requer auth PLAYER)
POST /api/games/friendly
Authorization: Bearer <token>
{
  "gameName": "Pelada do Final de Semana",
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Campo do Bairro",
  "description": "Jogo descontraído entre amigas"
}

# Criar Jogo de Campeonato (requer auth PLAYER)
POST /api/games/championship
Authorization: Bearer <token>
{
  "gameName": "Copa Feminina Regional",
  "gameDate": "2024-12-20T16:00:00",
  "venue": "Estádio Municipal",
  "description": "Semifinal do campeonato regional"
}

# Criar Jogo de Copa (requer auth ORGANIZATION)
POST /api/games/cup
Authorization: Bearer <token>
{
  "homeTeamId": 1,
  "awayTeamId": 2,
  "gameDate": "2024-12-25T14:00:00",
  "venue": "Arena Principal",
  "championship": "Copa Nacional Feminina",
  "round": "Final"
}
```

##### 📍 Consultas por Tipo

```http
# Listar todos os jogos
GET /api/games?page=0&size=20

# Buscar por tipo específico
GET /api/games/type/FRIENDLY?page=0&size=10
GET /api/games/type/CHAMPIONSHIP?page=0&size=10
GET /api/games/type/CUP?page=0&size=10

# Buscar jogos criados por um host (jogadoras)
GET /api/games/host/123?page=0&size=10

# Buscar por ID
GET /api/games/1

# Buscar por organização (jogos de copa)
GET /api/games/organization/1?page=0&size=10

# Buscar por status
GET /api/games/status/SCHEDULED?page=0&size=10

# Buscar por campeonato
GET /api/games/championship?championship=Copa%20Nacional&page=0&size=10
```

##### 📍 Operações Gerais

```http
# Atualizar jogo (requer auth - apenas criador)
PUT /api/games/1
Authorization: Bearer <token>
{
  "gameName": "Nome Atualizado",
  "gameDate": "2024-12-16T15:00:00",
  "venue": "Novo Local",
  "description": "Descrição atualizada"
}

# Deletar jogo (requer auth - apenas criador)
DELETE /api/games/1
Authorization: Bearer <token>

# Atualizar placar (requer auth - apenas criador)
PATCH /api/games/1/score?homeGoals=2&awayGoals=1
Authorization: Bearer <token>

# Inscrever-se no jogo (requer auth)
POST /api/games/1/subscribe
Authorization: Bearer <token>
```

#### 🤝 Sistema de Participações (Amistosos e Campeonatos)

Para jogos **FRIENDLY** e **CHAMPIONSHIP**, jogadoras podem participar individualmente ou com seu time.

##### 📍 Endpoints de Participação

```http
# Entrar em jogo (individual ou com time)
POST /api/game-participants/join
Authorization: Bearer <token>
{
  "gameId": 1,
  "participationType": "INDIVIDUAL",  // ou "WITH_TEAM"
  "teamSide": 1  // 1 ou 2
}

# Sair de jogo
DELETE /api/game-participants/leave/1
Authorization: Bearer <token>

# Ver participantes de um jogo
GET /api/game-participants/game/1?page=0&size=20

# Ver minhas participações
GET /api/game-participants/my-participations?page=0&size=10

# Ver participações por jogadora
GET /api/game-participants/player/123?page=0&size=10

# Ver participações por time
GET /api/game-participants/team/456?page=0&size=10
```

##### 🎯 Tipos de Participação

| Tipo | Descrição |
|------|-----------|
| `INDIVIDUAL` | Jogadora participa sozinha |
| `WITH_TEAM` | Jogadora participa com seu time |

##### 🎯 Status de Participação

| Status | Descrição |
|--------|-----------|
| `PENDING` | Participação pendente de confirmação |
| `CONFIRMED` | Participação confirmada |
| `CANCELLED` | Participação cancelada |

#### 🏅 Sistema de Convites (Jogos de Copa)

Para jogos **CUP**, organizações enviam convites formais para times específicos.

##### 📍 Endpoints de Convites

```http
# Enviar convite para time (requer auth ORGANIZATION)
POST /api/game-invites/send
Authorization: Bearer <token>
{
  "gameId": 1,
  "teamId": 456,
  "teamPosition": "HOME",  // ou "AWAY"
  "message": "Convite oficial para participar da final"
}

# Aceitar convite (requer auth ORGANIZATION do time)
POST /api/game-invites/accept/10
Authorization: Bearer <token>

# Rejeitar convite (requer auth ORGANIZATION do time)
POST /api/game-invites/reject/10
Authorization: Bearer <token>

# Cancelar convite (requer auth ORGANIZATION que enviou)
DELETE /api/game-invites/cancel/10
Authorization: Bearer <token>

# Ver convites de um jogo
GET /api/game-invites/game/1?page=0&size=20

# Ver convites por organização
GET /api/game-invites/organization/123?page=0&size=10

# Ver convites por time
GET /api/game-invites/team/456?page=0&size=10

# Ver convites pendentes (organização atual)
GET /api/game-invites/pending
Authorization: Bearer <token>

# Ver convites enviados (organização atual)
GET /api/game-invites/sent?page=0&size=10
Authorization: Bearer <token>
```

##### 🎯 Status de Convites

| Status | Descrição |
|--------|-----------|
| `PENDING` | Convite enviado, aguardando resposta |
| `ACCEPTED` | Convite aceito pelo time |
| `REJECTED` | Convite rejeitado pelo time |
| `CANCELLED` | Convite cancelado pela organização |
| `EXPIRED` | Convite expirado |

##### 🎯 Posições no Jogo

| Posição | Descrição |
|---------|-----------|
| `HOME` | Time da casa |
| `AWAY` | Time visitante |

#### 🔒 Regras de Negócio e Validações

##### 🎯 Permissões por Tipo de Usuário

| Ação | PLAYER | ORGANIZATION |
|------|--------|-----------|
| Criar Amistoso | ✅ | ✅ |
| Criar Campeonato | ✅ | ✅ |
| Criar Copa | ❌ | ✅ |
| Participar de Amistoso/Campeonato | ✅ | ✅ |
| Enviar Convites para Copa | ❌ | ✅ |
| Aceitar/Rejeitar Convites | ❌ | ✅ (apenas do próprio time) |

##### 🎯 Validações de Participação

- **Amistosos/Campeonatos**: Jogadoras podem participar individualmente ou com seu time
- **Copa**: Apenas times podem participar através de convites formais
- **Capacidade**: Máximo de jogadoras por lado (configurável)
- **Conflitos**: Validação de horários conflitantes
- **Status**: Apenas jogos com status `SCHEDULED` aceitam participações/convites

##### 🎯 Lógica de Vencedores

| Cenário | Retorno |
|---------|---------|
| Copa - Organizações diferentes | `winner` (Organization) |
| Copa - Mesma organização | `winningTeamSide` (1 ou 2) |
| Amistoso/Campeonato | `winningTeamSide` (1 ou 2) |
| Empate ou não finalizado | `null` |

#### 📋 Exemplos de Resposta

##### 🎯 GameResponse (Amistoso)

```json
{
  "id": 1,
  "gameType": "FRIENDLY",
  "gameName": "Pelada do Final de Semana",
  "hostUsername": "maria_silva",
  "hostId": 123,
  "gameDate": "2024-12-15T15:00:00",
  "venue": "Campo do Bairro",
  "description": "Jogo descontraído entre amigas",
  "status": "SCHEDULED",
  "homeGoals": 0,
  "awayGoals": 0,
  "winner": null,
  "winningTeamSide": null,
  "createdAt": "2024-12-10T10:00:00",
  "updatedAt": "2024-12-10T10:00:00",
  "homeTeam": null,
  "awayTeam": null,
  "championship": null,
  "round": null
}
```

##### 🎯 GameResponse (Copa)

```json
{
  "id": 2,
  "gameType": "CUP",
  "gameName": null,
  "hostUsername": null,
  "hostId": null,
  "gameDate": "2024-12-25T14:00:00",
  "venue": "Arena Principal",
  "description": null,
  "status": "FINISHED",
  "homeGoals": 3,
  "awayGoals": 1,
  "winner": {
    "id": 1,
    "name": "Federação Paulista",
    "description": "Organização oficial do futebol feminino"
  },
  "winningTeamSide": 1,
  "createdAt": "2024-12-20T09:00:00",
  "updatedAt": "2024-12-25T16:00:00",
  "homeTeam": {
    "id": 1,
    "name": "Santos FC Feminino",
    "city": "Santos"
  },
  "awayTeam": {
    "id": 2,
    "name": "Corinthians Feminino",
    "city": "São Paulo"
  },
  "championship": "Copa Nacional Feminina",
  "round": "Final"
}
```

##### 🎯 GameParticipantResponse

```json
{
  "id": 10,
  "game": {
    "id": 1,
    "gameName": "Pelada do Final de Semana",
    "gameType": "FRIENDLY"
  },
  "player": {
    "id": 123,
    "name": "Maria Silva",
    "username": "maria_silva"
  },
  "team": {
    "id": 456,
    "name": "Time das Amigas",
    "city": "São Paulo"
  },
  "teamSide": 1,
  "status": "CONFIRMED",
  "joinedAt": "2024-12-11T14:30:00"
}
```

##### 🎯 GameInviteResponse

```json
{
  "id": 20,
  "game": {
    "id": 2,
    "championship": "Copa Nacional Feminina",
    "gameType": "CUP"
  },
  "organization": {
    "id": 1,
    "name": "Federação Paulista",
    "description": "Organização oficial"
  },
  "team": {
    "id": 1,
    "name": "Santos FC Feminino",
    "city": "Santos"
  },
  "teamPosition": "HOME",
  "message": "Convite oficial para participar da final",
  "status": "ACCEPTED",
  "sentAt": "2024-12-20T10:00:00",
  "respondedAt": "2024-12-20T15:30:00"
}
```

#### 🚨 Códigos de Erro Específicos

| Código | Descrição |
|--------|-----------|
| `GAME_001` | Tipo de usuário não autorizado para esta ação |
| `GAME_002` | Jogo não encontrado |
| `GAME_003` | Participação já existe |
| `GAME_004` | Convite já enviado |
| `GAME_005` | Capacidade máxima atingida |
| `GAME_006` | Conflito de horário |
| `GAME_007` | Status do jogo não permite esta ação |
| `GAME_008` | Convite expirado |
| `GAME_009` | Apenas criador pode modificar o jogo |
| `GAME_010` | Time já possui convite pendente |

### 📝 Posts (`/api/posts`)

Sistema completo de posts com **sistema avançado de likes** que rastreia individualmente quem curtiu cada post.

#### 🔑 Características do Sistema de Likes
- ✅ **Rastreamento Individual**: Sabe exatamente quem curtiu cada post
- ✅ **Informação Automática**: Todo GET de posts inclui informações de likes
- ✅ **Validações**: Usuário não pode curtir o mesmo post duas vezes
- ✅ **Batch Operations**: Verificação de múltiplos posts de uma vez
- ✅ **Contagem Precisa**: Sincronização entre contador e tabela de likes

#### 📍 Endpoints de Posts

##### 📖 Consultas de Posts

```http
# Listar todos os posts (inclui informações de likes automaticamente)
GET /api/posts?page=0&size=20

# Buscar por ID (inclui informações de likes automaticamente)
GET /api/posts/1

# Buscar por autor
GET /api/posts/author/1?page=0&size=10

# Buscar meus posts (requer auth)
GET /api/posts/my-posts?page=0&size=10
Authorization: Bearer <token>

# Buscar por role
GET /api/posts/role/PLAYER?page=0&size=10

# Buscar por tipo
GET /api/posts/type/GENERAL?page=0&size=10

# Buscar mais curtidos
GET /api/posts/most-liked?page=0&size=10

# Buscar com imagens
GET /api/posts/with-images?page=0&size=10

# Buscar por conteúdo
GET /api/posts/search?content=gol&page=0&size=10
```

##### ✏️ Operações de Posts

```http
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
```

##### ❤️ Sistema de Likes

```http
# Curtir post (requer auth)
POST /api/posts/1/like
Authorization: Bearer <token>
# Retorna: PostLikeResponse com informações do like

# Descurtir post (requer auth)
DELETE /api/posts/1/like
Authorization: Bearer <token>

# Verificar se usuário curtiu o post (requer auth)
GET /api/posts/1/liked
Authorization: Bearer <token>
# Retorna: {"hasLiked": true}

# Listar todos que curtiram o post
GET /api/posts/1/likes
# Retorna: Lista de PostLikeResponse

# Obter contagem total de likes
GET /api/posts/1/likes/count
# Retorna: {"totalLikes": 15}

# Ver posts curtidos pelo usuário atual (requer auth)
GET /api/post-likes/my-likes
Authorization: Bearer <token>

# Verificar múltiplos posts de uma vez (batch) (requer auth)
POST /api/post-likes/check-liked
Authorization: Bearer <token>
[1, 2, 3, 4, 5]
# Retorna: {"likedPostIds": [1, 3, 5]}
```

##### 📊 Outras Interações

```http
# Comentar post (requer auth)
POST /api/posts/1/comment
Authorization: Bearer <token>

# Compartilhar post (requer auth)
POST /api/posts/1/share
Authorization: Bearer <token>
```

#### 📋 Estrutura de Resposta dos Posts

Todos os endpoints de consulta de posts agora retornam informações completas de likes:

```json
{
  "id": 1,
  "authorId": 123,
  "authorUsername": "maria_silva",
  "authorName": "Maria Silva",
  "authorType": "PLAYER",
  "content": "Preparando para o próximo jogo! 💪⚽",
  "imageUrl": "https://example.com/treino.jpg",
  "type": "GENERAL",
  "likes": 15,
  "comments": 3,
  "shares": 2,
  "createdAt": "2025-09-16T14:30:00",
  "updatedAt": "2025-09-16T14:30:00",
  
  // ✨ NOVAS INFORMAÇÕES DE LIKES
  "isLikedByCurrentUser": true,
  "totalLikes": 15,
  "recentLikes": [
    {
      "id": 45,
      "userId": 456,
      "userUsername": "ana_costa",
      "userName": "Ana Costa",
      "userType": "PLAYER",
      "createdAt": "2025-09-16T15:20:00"
    },
    {
      "id": 44,
      "userId": 789,
      "userUsername": "santos_fc",
      "userName": "Santos FC Feminino",
      "userType": "ORGANIZATION",
      "createdAt": "2025-09-16T15:15:00"
    }
  ]
}
```

#### 📝 Tipos de Posts Disponíveis

| Tipo | Descrição | Usado por |
|------|-----------|-----------|
| `GENERAL` | Posts gerais | Todos |
| `TRAINING` | Posts sobre treinos | PLAYER |
| `MATCH` | Posts sobre jogos | Todos |
| `ACHIEVEMENT` | Conquistas e vitórias | Todos |
| `NEWS` | Notícias e atualizações | ORGANIZATION |
| `ORGANIZATION_UPDATE` | Atualizações da organização | ORGANIZATION |
| `SPECTATOR_OPINION` | Opiniões de espectadores | SPECTATOR |

### 🏆 Sistema de Times (`/api/teams`)

O sistema de times permite que **jogadoras (PLAYER)** criem e participem de **múltiplos times**, enviando convites apenas para jogadoras que seguem mutuamente.

#### 🔑 Características Principais
- ✅ **Múltiplos Times**: Jogadoras podem participar de vários times simultaneamente
- ✅ **Seguimento Mútuo**: Convites só podem ser enviados entre jogadoras que se seguem mutuamente
- ✅ **Sistema de Convites**: Convites com status (PENDING, ACCEPTED, REJECTED, CANCELLED)
- ✅ **Liderança**: Criadora do time torna-se líder automaticamente
- ✅ **Gerenciamento**: Líderes podem convidar, remover jogadoras e cancelar convites

#### 📍 Endpoints de Times

```http
# Criar time (requer auth PLAYER)
POST /api/teams
Authorization: Bearer <token>
{
  "nameTeam": "Meu Time Incrível"
}

# Listar todos os times (público)
GET /api/teams?page=0&size=10&sortBy=createdAt&sortDir=desc

# Buscar time por ID (público)
GET /api/teams/1

# Buscar times por nome (público)
GET /api/teams/search?name=Incrível&page=0&size=10

# Enviar convite para jogadora (requer auth PLAYER - apenas líderes)
POST /api/teams/1/invites
Authorization: Bearer <token>
{
  "invitedPlayerId": 456
}

# Ver convites do time (requer auth PLAYER - apenas líder)
GET /api/teams/1/invites
Authorization: Bearer <token>

# Ver meus convites pendentes (requer auth PLAYER)
GET /api/teams/my-invites
Authorization: Bearer <token>

# Aceitar convite (requer auth PLAYER)
POST /api/teams/invites/10/accept
Authorization: Bearer <token>

# Rejeitar convite (requer auth PLAYER)
POST /api/teams/invites/10/reject
Authorization: Bearer <token>

# Cancelar convite (requer auth PLAYER - apenas líder)
DELETE /api/teams/invites/10
Authorization: Bearer <token>

# Sair do time (requer auth PLAYER - exceto líder)
POST /api/teams/1/leave
Authorization: Bearer <token>

# Remover jogadora do time (requer auth PLAYER - apenas líder)
DELETE /api/teams/1/players/456
Authorization: Bearer <token>
```

#### 🎯 Status de Convites

| Status | Descrição |
|--------|-----------|
| `PENDING` | Convite enviado, aguardando resposta |
| `ACCEPTED` | Convite aceito, jogadora adicionada ao time |
| `REJECTED` | Convite rejeitado pela jogadora |
| `CANCELLED` | Convite cancelado pelo líder |

#### 🔒 Regras de Negócio - Times

**✅ Permitido:**
- Jogadoras podem participar de **múltiplos times**
- Convidar apenas jogadoras que seguem mutuamente
- Aceitar/rejeitar convites enviados para você
- Sair de times (exceto se for líder)
- Líderes podem remover jogadoras e cancelar convites

**❌ Não Permitido:**
- Organizações ou espectadores criarem times
- Convidar jogadoras que não seguem mutuamente
- Convidar jogadoras já presentes no time
- Líderes saírem do time sem transferir liderança
- Aceitar convites de outros jogadores
- Remover jogadoras sem ser líder

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

# 5. Criar um time
curl -X POST http://localhost:8080/api/teams \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "nameTeam": "Estrelas do Futebol"
  }'

# 6. Enviar convite para jogadora (ID 2 que foi seguida)
curl -X POST http://localhost:8080/api/teams/1/invites \
  -H "Authorization: Bearer SEU_TOKEN_AQUI" \
  -H "Content-Type: application/json" \
  -d '{
    "invitedPlayerId": 2
  }'

# 7. Criar jogo (jogadoras também podem criar jogos)
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

### Exemplo 2: Fluxo Completo do Sistema de Times

```bash
# Cenário: Maria cria um time e convida Ana

# 1. Maria faz login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@email.com",
    "password": "senha123"
  }'

# 2. Maria cria um time (torna-se líder automaticamente)
curl -X POST http://localhost:8080/api/teams \
  -H "Authorization: Bearer MARIA_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nameTeam": "Estrelas do Futebol"
  }'

# 3. Maria segue Ana (necessário para seguimento mútuo)
curl -X POST http://localhost:8080/api/players/2/follow \
  -H "Authorization: Bearer MARIA_TOKEN"

# 4. Ana segue Maria de volta (seguimento mútuo estabelecido)
curl -X POST http://localhost:8080/api/players/1/follow \
  -H "Authorization: Bearer ANA_TOKEN"

# 5. Maria envia convite para Ana
curl -X POST http://localhost:8080/api/teams/1/invites \
  -H "Authorization: Bearer MARIA_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "invitedPlayerId": 2
  }'

# 6. Ana verifica seus convites pendentes
curl -X GET http://localhost:8080/api/teams/my-invites \
  -H "Authorization: Bearer ANA_TOKEN"

# 7. Ana aceita o convite (é adicionada automaticamente ao time)
curl -X POST http://localhost:8080/api/teams/invites/1/accept \
  -H "Authorization: Bearer ANA_TOKEN"

# 8. Verificar time atualizado com ambas jogadoras
curl -X GET http://localhost:8080/api/teams/1
```

### Exemplo 3: Fluxo de Organização

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

### Exemplo 4: Fluxo de Espectador

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
