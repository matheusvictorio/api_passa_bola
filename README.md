# 🏆 API Passa Bola - Documentação Completa

> API REST completa para plataforma de futebol feminino com sistema de jogadoras, organizações, espectadores, times, jogos e interações sociais.

## 📋 Índice

- [🚀 Configuração Inicial](#-configuração-inicial)
- [🏗️ Arquitetura da API](#️-arquitetura-da-api)
- [🔐 Autenticação e Autorização](#-autenticação-e-autorização)
- [⚽ Sistema de Jogos](#-sistema-de-jogos)
- [👥 Sistema de Times](#-sistema-de-times)
- [🤝 Sistema de Seguimento](#-sistema-de-seguimento)
- [📝 Sistema de Posts](#-sistema-de-posts)
- [📡 Endpoints da API](#-endpoints-da-api)
- [💡 Exemplos Práticos](#-exemplos-práticos)
- [🔧 Troubleshooting](#-troubleshooting)

---

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

---

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
  "bio": "Atacante profissional",
  "followers": 150,
  "following": 75,
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
  "bio": "Time tradicional de futebol feminino",
  "followers": 5000,
  "following": 200,
  "city": "Santos",
  "state": "SP",
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

#### 🏆 Team (Time de Jogadoras)
```json
{
  "id": 1,
  "nameTeam": "Estrelas FC",
  "leader": {
    "id": 10,
    "username": "maria_silva",
    "name": "Maria Silva"
  },
  "players": [
    {"id": 10, "name": "Maria Silva"},
    {"id": 20, "name": "Ana Costa"},
    {"id": 30, "name": "Julia Santos"}
  ],
  "playerCount": 3,
  "createdAt": "2024-12-01T10:00:00"
}
```

---

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

#### 1. Registro de Jogadora
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

#### 2. Registro de Organização
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
  "city": "Santos",
  "state": "SP",
  "profilePhotoUrl": "https://example.com/logo.jpg",
  "bannerUrl": "https://example.com/banner.jpg",
  "phone": "(13) 3333-3333"
}
```

#### 3. Registro de Espectador
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

#### 4. Login (Email-based)
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

> **⚠️ Importante:** O sistema usa **email** para login, não username. O token JWT contém as informações do usuário autenticado.

---

## ⚽ Sistema de Jogos

O sistema suporta **3 tipos distintos** de jogos, cada um com suas próprias regras:

### Tipos de Jogos

| Tipo | Criador | Participação | Estrutura |
|------|---------|--------------|-----------|
| **FRIENDLY** (Amistoso) | PLAYER | Individual ou Time | Informal, flexível |
| **CHAMPIONSHIP** (Campeonato) | PLAYER | Individual ou Time | Competitivo, organizado |
| **CUP** (Copa) | ORGANIZATION | Apenas Times Oficiais | Formal, com convites |

---

### 🎯 Regras de Criação de Jogos

#### **Configurações Obrigatórias:**

1. **Sistema de Espectadores:**
   - `hasSpectators`: true/false
   - Se `true`, campo `maxSpectators` é **opcional**
   - Se não informado, **padrão é 5** (mínimo)
   - Se informado, `maxSpectators` deve ser **no mínimo 5**

2. **Limites de Jogadoras:**
   - **Mínimo:** 6 jogadoras (3x3)
   - **Máximo:** 22 jogadoras (11x11)
   - **Números pares obrigatórios** para times balanceados

3. **Times Balanceados:**
   - Jogo só pode começar se:
     - Atingir mínimo de jogadoras
     - Ter **exatamente o mesmo número** em cada lado

4. **Time Completo no Mesmo Lado:**
   - Quando um time entra, **TODAS as integrantes** vão para o **MESMO lado**

---

### 1️⃣ Jogos Amistosos (FRIENDLY)

#### **Características:**
- Criados por **jogadoras** (PLAYER)
- Participação individual ou com time
- Estrutura flexível e informal
- Ideal para treinos e jogos casuais

#### **Criar Jogo Amistoso:**
```http
POST /api/games/friendly
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "gameName": "Pelada do Sábado",
  "gameDate": "2025-10-15T14:00:00",
  "venue": "Campo do Parque",
  "description": "Jogo 5x5 com torcida",
  "hasSpectators": true,
  "maxSpectators": 20,
  "minPlayers": 10,
  "maxPlayers": 22
}
```

**Response:**
```json
{
  "id": 123,
  "gameType": "FRIENDLY",
  "gameName": "Pelada do Sábado",
  "hostUsername": "maria_silva",
  "hostId": 10,
  "gameDate": "2025-10-15T14:00:00",
  "venue": "Campo do Parque",
  "description": "Jogo 5x5 com torcida",
  "hasSpectators": true,
  "minPlayers": 10,
  "maxPlayers": 22,
  "maxSpectators": 20,
  "currentSpectatorCount": 0,
  "currentPlayerCount": 0,
  "team1Count": 0,
  "team2Count": 0,
  "isTeamsBalanced": true,
  "canStart": false,
  "status": "SCHEDULED",
  "homeGoals": 0,
  "awayGoals": 0,
  "team1Players": [],
  "team2Players": [],
  "createdAt": "2025-10-06T23:00:00"
}
```

> **💡 Nota:** `hostUsername` e `hostId` são extraídos automaticamente do JWT token. Não é necessário enviar no request.

---

### 2️⃣ Jogos de Campeonato (CHAMPIONSHIP)

#### **Características:**
- Criados por **jogadoras** (PLAYER)
- Mesma estrutura dos amistosos
- Diferenciados apenas pelo tipo
- Ideal para competições organizadas

#### **Criar Jogo de Campeonato:**
```http
POST /api/games/championship
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "gameName": "Campeonato Regional - Fase 1",
  "gameDate": "2025-11-20T16:00:00",
  "venue": "Estádio Municipal",
  "description": "Primeira fase do campeonato",
  "hasSpectators": true,
  "maxSpectators": 50,
  "minPlayers": 22,
  "maxPlayers": 22
}
```

---

### 3️⃣ Jogos de Copa (CUP)

#### **Características:**
- Criados por **organizações** (ORGANIZATION)
- Apenas times oficiais (Organizations)
- Sistema formal de convites
- Estrutura profissional

#### **Criar Jogo de Copa:**
```http
POST /api/games/cup
Authorization: Bearer <token_organization>
Content-Type: application/json

{
  "homeTeamId": 10,
  "awayTeamId": 20,
  "gameDate": "2025-12-05T19:00:00",
  "venue": "Arena Central",
  "championship": "Copa Nacional Feminina",
  "round": "Quartas de Final"
}
```

**Response:**
```json
{
  "id": 456,
  "gameType": "CUP",
  "homeTeam": {
    "id": 10,
    "name": "Santos FC Feminino",
    "logoUrl": "https://..."
  },
  "awayTeam": {
    "id": 20,
    "name": "Corinthians Feminino",
    "logoUrl": "https://..."
  },
  "gameDate": "2025-12-05T19:00:00",
  "venue": "Arena Central",
  "championship": "Copa Nacional Feminina",
  "round": "Quartas de Final",
  "status": "SCHEDULED",
  "homeGoals": 0,
  "awayGoals": 0
}
```

---

### 🎮 Sistema de Participação em Jogos

#### **Para Jogos FRIENDLY e CHAMPIONSHIP:**

Jogadoras podem entrar de **2 formas**:

##### **1. Individual (INDIVIDUAL)**
```http
POST /api/game-participants/join
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "gameId": 123,
  "participationType": "INDIVIDUAL",
  "teamSide": 1
}
```

**Resultado:** Apenas a jogadora é adicionada ao Time 1

##### **2. Com Time Completo (WITH_TEAM)**
```http
POST /api/game-participants/join
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "gameId": 123,
  "participationType": "WITH_TEAM",
  "teamSide": 2
}
```

**Resultado:** **TODAS as integrantes do time** são adicionadas ao Time 2

> **⚠️ Importante:** 
> - `teamSide` é **obrigatório** (1 ou 2)
> - Jogadora **escolhe** qual lado quer entrar
> - Com `WITH_TEAM`, todas as integrantes vão para o **mesmo lado**

#### **Validações de Participação:**

✅ Jogo não pode ter começado  
✅ Jogadora não pode estar duplicada  
✅ Não pode exceder máximo de jogadoras  
✅ `teamSide` deve ser 1 ou 2  
✅ Com `WITH_TEAM`, jogadora deve ter time cadastrado  

#### **Sair de um Jogo:**
```http
DELETE /api/game-participants/leave/{gameId}
Authorization: Bearer <token_player>
```

**Comportamento:**
- **INDIVIDUAL:** Remove apenas a jogadora
- **WITH_TEAM:** Remove **TODAS as integrantes do time**

#### **Ver Participantes:**
```http
# Listar todas as participantes do jogo
GET /api/game-participants/game/{gameId}

# Minhas participações
GET /api/game-participants/my-participations?page=0&size=20
Authorization: Bearer <token_player>

# Participações de uma jogadora específica
GET /api/game-participants/player/{playerId}?page=0&size=20
```

---

### 👥 Sistema de Espectadores em Jogos

#### **Para Jogos FRIENDLY e CHAMPIONSHIP:**

Espectadores podem se inscrever para assistir jogos que aceitam público.

#### **Confirmar Presença como Espectador:**
```http
POST /api/games/{id}/spectate
Authorization: Bearer <token_spectator>
```

**Validações:**
- ✅ Apenas usuários SPECTATOR podem se inscrever
- ✅ Jogo deve ter `hasSpectators = true`
- ✅ Apenas jogos FRIENDLY e CHAMPIONSHIP aceitam espectadores
- ✅ Não pode exceder `maxSpectators` (mínimo 5 quando habilitado)
- ✅ Não pode se inscrever duas vezes no mesmo jogo

**Response:**
```json
{
  "id": 789,
  "gameId": 123,
  "gameName": "Pelada do Sábado",
  "spectatorId": 45,
  "spectatorUsername": "joao_torcedor",
  "spectatorName": "João Santos",
  "status": "CONFIRMED",
  "joinedAt": "2025-10-07T14:30:00",
  "createdAt": "2025-10-07T14:30:00"
}
```

#### **Cancelar Presença:**
```http
DELETE /api/games/{id}/spectate
Authorization: Bearer <token_spectator>
```

#### **Ver Espectadores de um Jogo:**
```http
# Lista de espectadores confirmados (público)
GET /api/games/{id}/spectators

# Contagem de espectadores
GET /api/games/{id}/spectators/count

# Verificar se estou inscrito
GET /api/games/{id}/spectators/is-subscribed
Authorization: Bearer <token_spectator>
```

#### **Meus Jogos Inscritos:**
```http
GET /api/games/spectators/my-subscriptions?page=0&size=20
Authorization: Bearer <token_spectator>
```

**Response:**
```json
{
  "content": [
    {
      "id": 789,
      "gameId": 123,
      "gameName": "Pelada do Sábado",
      "spectatorId": 45,
      "spectatorUsername": "joao_torcedor",
      "spectatorName": "João Santos",
      "status": "CONFIRMED",
      "joinedAt": "2025-10-07T14:30:00"
    }
  ],
  "totalElements": 5,
  "totalPages": 1
}
```

#### **Regras de Negócio:**

| Regra | Descrição |
|-------|-----------|
| **Tipo de Usuário** | Apenas SPECTATOR pode se inscrever como espectador |
| **Tipo de Jogo** | Apenas FRIENDLY e CHAMPIONSHIP aceitam espectadores |
| **Habilitação** | Jogo deve ter `hasSpectators = true` |
| **Limite Mínimo** | Quando habilitado, mínimo de 5 espectadores |
| **Limite Máximo** | Definido pelo criador do jogo (`maxSpectators`) |
| **Duplicação** | Um espectador não pode se inscrever duas vezes |
| **Contagem Automática** | `currentSpectatorCount` atualizado em tempo real |

---

### 📊 Campos de Status do Jogo

Todos os jogos FRIENDLY e CHAMPIONSHIP retornam:

```json
{
  "hasSpectators": true,
  "minPlayers": 10,
  "maxPlayers": 22,
  "maxSpectators": 20,
  "currentSpectatorCount": 0,
  "currentPlayerCount": 8,
  "team1Count": 4,
  "team2Count": 4,
  "isTeamsBalanced": true,
  "canStart": false
}
```

| Campo | Descrição |
|-------|-----------|
| `hasSpectators` | Se o jogo permite espectadores |
| `minPlayers` | Mínimo de jogadoras para começar |
| `maxPlayers` | Máximo de jogadoras permitido |
| `maxSpectators` | Máximo de espectadores permitido (mínimo 5 se habilitado) |
| `currentSpectatorCount` | Total de espectadores confirmados |
| `currentPlayerCount` | Total de jogadoras (team1 + team2) |
| `team1Count` | Jogadoras no Time 1 |
| `team2Count` | Jogadoras no Time 2 |
| `isTeamsBalanced` | Se times têm mesmo número |
| `canStart` | Se pode começar (mínimo + balanceado) |

---

### 🔄 Atualizar Jogos

#### **Atualizar Jogo Amistoso:**
```http
PUT /api/games/friendly/{id}
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "gameName": "Pelada do Sábado - ATUALIZADO",
  "gameDate": "2025-10-15T15:00:00",
  "venue": "Campo do Parque Central",
  "description": "Jogo 5x5 atualizado",
  "homeGoals": 3,
  "awayGoals": 2,
  "status": "FINISHED",
  "notes": "Jogo muito disputado!"
}
```

> **🔒 Validação:** Apenas o **host** (criador) pode atualizar

#### **Atualizar Jogo de Campeonato:**
```http
PUT /api/games/championship/{id}
Authorization: Bearer <token_player>
```
> Mesma estrutura do amistoso

#### **Atualizar Jogo de Copa:**
```http
PUT /api/games/cup/{id}
Authorization: Bearer <token_organization>
Content-Type: application/json

{
  "homeTeamId": 10,
  "awayTeamId": 20,
  "gameDate": "2025-12-05T20:00:00",
  "venue": "Arena Central",
  "championship": "Copa Nacional Feminina",
  "round": "Quartas de Final",
  "homeGoals": 2,
  "awayGoals": 1,
  "status": "FINISHED",
  "notes": "Vitória do time da casa"
}
```

> **🔒 Validação:** Apenas a **organização criadora** pode atualizar

#### **Atualizar Placar:**
```http
PATCH /api/games/{id}/score?homeGoals=3&awayGoals=2
Authorization: Bearer <token>
```

#### **Deletar Jogo:**
```http
DELETE /api/games/{id}
Authorization: Bearer <token>
```

---

### 📋 Consultar Jogos

```http
# Listar todos os jogos
GET /api/games?page=0&size=20

# Buscar por ID
GET /api/games/{id}

# Buscar por tipo
GET /api/games/type/FRIENDLY?page=0&size=20
GET /api/games/type/CHAMPIONSHIP?page=0&size=20
GET /api/games/type/CUP?page=0&size=20

# Buscar por host (jogadora criadora)
GET /api/games/host/{hostId}?page=0&size=20

# Buscar por organização
GET /api/games/organization/{organizationId}?page=0&size=20

# Buscar por status
GET /api/games/status/SCHEDULED?page=0&size=20
GET /api/games/status/LIVE?page=0&size=20
GET /api/games/status/FINISHED?page=0&size=20

# Buscar por campeonato
GET /api/games/championship?championship=Copa%20Nacional&page=0&size=20

# Buscar por período
GET /api/games/date-range?startDate=2025-10-01T00:00:00&endDate=2025-10-31T23:59:59&page=0&size=20
```

---

## 👥 Sistema de Times

### Características
- Apenas **jogadoras** (PLAYER) podem criar times
- Criadora torna-se **líder** automaticamente
- Sistema de **convites** com validação de seguimento mútuo
- Jogadoras podem estar em **múltiplos times**

### Criar Time
```http
POST /api/teams
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "nameTeam": "Estrelas FC"
}
```

**Response:**
```json
{
  "id": 1,
  "nameTeam": "Estrelas FC",
  "leader": {
    "id": 10,
    "username": "maria_silva",
    "name": "Maria Silva",
    "profilePhotoUrl": "https://..."
  },
  "players": [
    {
      "id": 10,
      "username": "maria_silva",
      "name": "Maria Silva"
    }
  ],
  "playerCount": 1,
  "createdAt": "2025-10-06T23:00:00"
}
```

### Convidar Jogadora
```http
POST /api/teams/{teamId}/invites
Authorization: Bearer <token_player>
Content-Type: application/json

{
  "invitedPlayerId": 20
}
```

**Validações:**
- ✅ Apenas **líder** pode convidar
- ✅ **Seguimento mútuo obrigatório** (ambas devem se seguir)
- ✅ Jogadora não pode estar já no time
- ✅ Não pode ter convite pendente

### Aceitar/Rejeitar Convite
```http
# Aceitar
POST /api/teams/invites/{inviteId}/accept
Authorization: Bearer <token_player>

# Rejeitar
POST /api/teams/invites/{inviteId}/reject
Authorization: Bearer <token_player>
```

### Cancelar Convite
```http
DELETE /api/teams/invites/{inviteId}
Authorization: Bearer <token_player>
```
> Apenas o **líder** pode cancelar

### Ver Convites
```http
# Meus convites recebidos
GET /api/teams/my-invites?page=0&size=20
Authorization: Bearer <token_player>

# Convites do time (apenas líder)
GET /api/teams/{teamId}/invites?page=0&size=20
Authorization: Bearer <token_player>
```

### Sair do Time
```http
POST /api/teams/leave
Authorization: Bearer <token_player>
```
> **⚠️ Líder não pode sair** (deve transferir liderança ou dissolver time)

### Remover Jogadora
```http
DELETE /api/teams/{teamId}/players/{playerId}
Authorization: Bearer <token_player>
```
> Apenas o **líder** pode remover

### Consultar Times
```http
# Listar todos
GET /api/teams?page=0&size=20

# Buscar por ID
GET /api/teams/{id}

# Buscar por nome
GET /api/teams/search?name=Estrelas&page=0&size=20
```

---

## 🤝 Sistema de Seguimento Universal

### Características
- **Qualquer usuário** pode seguir **qualquer outro**
- PLAYER ↔ ORGANIZATION ↔ SPECTATOR
- Relacionamentos bidirecionais automáticos
- Endpoints universais simplificados

### Seguir Usuário
```http
POST /api/follow
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": 123,
  "targetUserType": "PLAYER"
}
```

**Tipos válidos:** `PLAYER`, `ORGANIZATION`, `SPECTATOR`

### Deixar de Seguir
```http
DELETE /api/follow
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": 123,
  "targetUserType": "PLAYER"
}
```

### Verificar se Está Seguindo
```http
POST /api/follow/check
Authorization: Bearer <token>
Content-Type: application/json

{
  "targetUserId": 123,
  "targetUserType": "PLAYER"
}
```

**Response:**
```json
{
  "isFollowing": true
}
```

### Ver Seguidores
```http
# Seguidores de um usuário (público)
GET /api/follow/followers/{userId}/{userType}?page=0&size=20

# Meus seguidores
GET /api/follow/my-followers?page=0&size=20
Authorization: Bearer <token>
```

### Ver Seguindo
```http
# Quem um usuário está seguindo (público)
GET /api/follow/following/{userId}/{userType}?page=0&size=20

# Quem estou seguindo
GET /api/follow/my-following?page=0&size=20
Authorization: Bearer <token>
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "followerId": 10,
      "followerUsername": "maria_silva",
      "followerName": "Maria Silva",
      "followerType": "PLAYER",
      "followedId": 20,
      "followedUsername": "ana_costa",
      "followedName": "Ana Costa",
      "followedType": "PLAYER",
      "createdAt": "2025-10-06T10:00:00"
    }
  ],
  "totalElements": 150,
  "totalPages": 8
}
```

---

## 📝 Sistema de Posts

### Características
- **Todos os usuários** autenticados podem criar posts
- Sistema de likes com rastreamento individual
- Informações de quem curtiu
- Suporte a imagens e diferentes tipos

### Criar Post
```http
POST /api/posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Preparando para o próximo treino! 💪⚽",
  "imageUrl": "https://example.com/treino.jpg",
  "type": "GENERAL"
}
```

**Tipos de Post:**
- `GENERAL`: Post geral
- `MATCH_ANNOUNCEMENT`: Anúncio de jogo
- `TRAINING_UPDATE`: Atualização de treino
- `ACHIEVEMENT`: Conquista
- `ORGANIZATION_UPDATE`: Atualização de organização
- `SPECTATOR_OPINION`: Opinião de espectador

**Response:**
```json
{
  "id": 1,
  "authorId": 10,
  "authorUsername": "maria_silva",
  "authorName": "Maria Silva",
  "content": "Preparando para o próximo treino! 💪⚽",
  "imageUrl": "https://example.com/treino.jpg",
  "type": "GENERAL",
  "totalLikes": 0,
  "isLikedByCurrentUser": false,
  "recentLikes": [],
  "createdAt": "2025-10-06T23:00:00"
}
```

> **💡 Nota:** `authorId`, `authorUsername` e `authorName` são extraídos automaticamente do JWT token.

### Curtir Post
```http
POST /api/posts/{id}/like
Authorization: Bearer <token>
```

**Response:**
```json
{
  "id": 1,
  "userId": 10,
  "userUsername": "maria_silva",
  "userName": "Maria Silva",
  "userType": "PLAYER",
  "createdAt": "2025-10-06T23:05:00"
}
```

### Descurtir Post
```http
DELETE /api/posts/{id}/like
Authorization: Bearer <token>
```

### Ver Quem Curtiu
```http
GET /api/posts/{id}/likes?page=0&size=20
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": 10,
      "userUsername": "maria_silva",
      "userName": "Maria Silva",
      "userType": "PLAYER",
      "createdAt": "2025-10-06T23:05:00"
    },
    {
      "id": 2,
      "userId": 20,
      "userUsername": "ana_costa",
      "userName": "Ana Costa",
      "userType": "PLAYER",
      "createdAt": "2025-10-06T23:06:00"
    }
  ],
  "totalElements": 45
}
```

### Verificar se Curtiu
```http
GET /api/posts/{id}/liked
Authorization: Bearer <token>
```

**Response:**
```json
{
  "liked": true
}
```

### Atualizar Post
```http
PUT /api/posts/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Treino finalizado! Foi intenso! 💪⚽",
  "imageUrl": "https://example.com/treino_final.jpg",
  "type": "TRAINING_UPDATE"
}
```

> **🔒 Validação:** Apenas o **autor** pode atualizar

### Deletar Post
```http
DELETE /api/posts/{id}
Authorization: Bearer <token>
```

> **🔒 Validação:** Apenas o **autor** pode deletar

### Consultar Posts
```http
# Listar todos
GET /api/posts?page=0&size=20

# Buscar por ID
GET /api/posts/{id}

# Posts de um autor
GET /api/posts/author/{authorId}?page=0&size=20

# Meus posts
GET /api/posts/my-posts?page=0&size=20
Authorization: Bearer <token>

# Posts por tipo de usuário
GET /api/posts/role/PLAYER?page=0&size=20
GET /api/posts/role/ORGANIZATION?page=0&size=20
GET /api/posts/role/SPECTATOR?page=0&size=20

# Meus posts curtidos
GET /api/post-likes/my-likes?page=0&size=20
Authorization: Bearer <token>
```

---

## 📡 Endpoints Completos da API

### 🔑 Autenticação (`/api/auth`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/auth/register/player` | ❌ | Registrar jogadora |
| POST | `/api/auth/register/organization` | ❌ | Registrar organização |
| POST | `/api/auth/register/spectator` | ❌ | Registrar espectador |
| POST | `/api/auth/login` | ❌ | Login (email + senha) |

### 👩‍⚽ Jogadoras (`/api/players`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| GET | `/api/players` | ❌ | Listar todas |
| GET | `/api/players/{id}` | ❌ | Buscar por ID |
| GET | `/api/players/username/{username}` | ❌ | Buscar por username |
| GET | `/api/players/search?name={name}` | ❌ | Buscar por nome |
| GET | `/api/players/organization/{id}` | ❌ | Buscar por organização |
| PUT | `/api/players/{id}` | PLAYER | Atualizar perfil |
| PUT | `/api/players/{id}/profile-photo` | PLAYER | Atualizar foto |
| PUT | `/api/players/{id}/banner` | PLAYER | Atualizar banner |

### 🏟️ Organizações (`/api/organizations`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| GET | `/api/organizations` | ❌ | Listar todas |
| GET | `/api/organizations/{id}` | ❌ | Buscar por ID |
| GET | `/api/organizations/username/{username}` | ❌ | Buscar por username |
| GET | `/api/organizations/search?name={name}` | ❌ | Buscar por nome |
| PUT | `/api/organizations/{id}` | ORG | Atualizar perfil |
| PUT | `/api/organizations/{id}/profile-photo` | ORG | Atualizar logo |
| PUT | `/api/organizations/{id}/banner` | ORG | Atualizar banner |

### 👥 Espectadores (`/api/spectators`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| GET | `/api/spectators` | ❌ | Listar todos |
| GET | `/api/spectators/{id}` | ❌ | Buscar por ID |
| GET | `/api/spectators/username/{username}` | ❌ | Buscar por username |
| GET | `/api/spectators/search?name={name}` | ❌ | Buscar por nome |
| PUT | `/api/spectators/{id}` | SPEC | Atualizar perfil |
| PUT | `/api/spectators/{id}/profile-photo` | SPEC | Atualizar foto |
| PUT | `/api/spectators/{id}/banner` | SPEC | Atualizar banner |

### ⚽ Jogos (`/api/games`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/games/friendly` | PLAYER | Criar jogo amistoso |
| POST | `/api/games/championship` | PLAYER | Criar jogo de campeonato |
| POST | `/api/games/cup` | ORG | Criar jogo de copa |
| GET | `/api/games` | ❌ | Listar todos |
| GET | `/api/games/{id}` | ❌ | Buscar por ID |
| GET | `/api/games/type/{gameType}` | ❌ | Buscar por tipo |
| GET | `/api/games/host/{hostId}` | ❌ | Buscar por host |
| GET | `/api/games/organization/{id}` | ❌ | Buscar por organização |
| GET | `/api/games/status/{status}` | ❌ | Buscar por status |
| GET | `/api/games/championship?championship={name}` | ❌ | Buscar por campeonato |
| GET | `/api/games/date-range?startDate={}&endDate={}` | ❌ | Buscar por período |
| PUT | `/api/games/friendly/{id}` | PLAYER | Atualizar amistoso |
| PUT | `/api/games/championship/{id}` | PLAYER | Atualizar campeonato |
| PUT | `/api/games/cup/{id}` | ORG | Atualizar copa |
| PATCH | `/api/games/{id}/score?homeGoals={}&awayGoals={}` | PLAYER/ORG | Atualizar placar |
| DELETE | `/api/games/{id}` | PLAYER/ORG | Deletar jogo |

### 🎮 Participação em Jogos (`/api/game-participants`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/game-participants/join` | PLAYER | Entrar em jogo |
| DELETE | `/api/game-participants/leave/{gameId}` | PLAYER | Sair de jogo |
| GET | `/api/game-participants/game/{gameId}` | ❌ | Ver participantes |
| GET | `/api/game-participants/player/{playerId}` | ❌ | Participações de jogadora |
| GET | `/api/game-participants/my-participations` | PLAYER/ORG | Minhas participações |
| GET | `/api/game-participants/team/{teamId}` | ❌ | Participações do time |

### 🏆 Times (`/api/teams`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/teams` | PLAYER | Criar time |
| GET | `/api/teams` | ❌ | Listar todos |
| GET | `/api/teams/{id}` | ❌ | Buscar por ID |
| GET | `/api/teams/search?name={name}` | ❌ | Buscar por nome |
| POST | `/api/teams/{teamId}/invites` | PLAYER | Enviar convite |
| GET | `/api/teams/{teamId}/invites` | PLAYER | Ver convites do time |
| DELETE | `/api/teams/invites/{inviteId}` | PLAYER | Cancelar convite |
| POST | `/api/teams/invites/{inviteId}/accept` | PLAYER | Aceitar convite |
| POST | `/api/teams/invites/{inviteId}/reject` | PLAYER | Rejeitar convite |
| GET | `/api/teams/my-invites` | PLAYER | Meus convites |
| POST | `/api/teams/leave` | PLAYER | Sair do time |
| DELETE | `/api/teams/{teamId}/players/{playerId}` | PLAYER | Remover jogadora |

### 🤝 Seguimento (`/api/follow`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/follow` | ✅ | Seguir usuário |
| DELETE | `/api/follow` | ✅ | Deixar de seguir |
| POST | `/api/follow/check` | ✅ | Verificar se segue |
| GET | `/api/follow/followers/{userId}/{userType}` | ❌ | Ver seguidores |
| GET | `/api/follow/following/{userId}/{userType}` | ❌ | Ver seguindo |
| GET | `/api/follow/my-followers` | ✅ | Meus seguidores |
| GET | `/api/follow/my-following` | ✅ | Quem estou seguindo |

### 📝 Posts (`/api/posts`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| POST | `/api/posts` | ✅ | Criar post |
| GET | `/api/posts` | ❌ | Listar todos |
| GET | `/api/posts/{id}` | ❌ | Buscar por ID |
| GET | `/api/posts/author/{authorId}` | ❌ | Posts de um autor |
| GET | `/api/posts/my-posts` | ✅ | Meus posts |
| GET | `/api/posts/role/{role}` | ❌ | Posts por tipo de usuário |
| PUT | `/api/posts/{id}` | ✅ | Atualizar post |
| DELETE | `/api/posts/{id}` | ✅ | Deletar post |
| POST | `/api/posts/{id}/like` | ✅ | Curtir post |
| DELETE | `/api/posts/{id}/like` | ✅ | Descurtir post |
| GET | `/api/posts/{id}/likes` | ❌ | Ver quem curtiu |
| GET | `/api/posts/{id}/liked` | ✅ | Verificar se curtiu |
| GET | `/api/posts/{id}/likes/count` | ❌ | Contagem de likes |

### ❤️ Likes (`/api/post-likes`)

| Método | Endpoint | Auth | Descrição |
|--------|----------|------|-----------|
| GET | `/api/post-likes/my-likes` | ✅ | Posts que curtí |
| POST | `/api/post-likes/check-liked` | ✅ | Verificar múltiplos posts |

---

## 💡 Exemplos Práticos

### 🎯 Fluxo Completo: Criar e Participar de um Jogo

#### **1. Jogadora se registra e faz login**
```bash
# Registrar
curl -X POST http://localhost:8080/api/auth/register/player \
  -H "Content-Type: application/json" \
  -d '{
    "username": "maria_silva",
    "name": "Maria Silva",
    "email": "maria@email.com",
    "password": "senha123",
    "bio": "Atacante profissional",
    "birthDate": "1995-03-15",
    "phone": "(11) 99999-9999"
  }'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "maria@email.com",
    "password": "senha123"
  }'

# Resposta: { "token": "eyJhbGc...", "userType": "PLAYER", "profileId": 1 }
```

#### **2. Criar jogo amistoso**
```bash
curl -X POST http://localhost:8080/api/games/friendly \
  -H "Authorization: Bearer eyJhbGc..." \
  -H "Content-Type: application/json" \
  -d '{
    "gameName": "Pelada do Sábado",
    "gameDate": "2025-10-15T14:00:00",
    "venue": "Campo do Parque",
    "description": "Jogo 5x5",
    "hasSpectators": true,
    "maxSpectators": 15,
    "minPlayers": 10,
    "maxPlayers": 22
  }'

# Resposta: { "id": 123, "gameName": "Pelada do Sábado", ... }
```

#### **3. Outra jogadora entra no jogo**
```bash
# Ana faz login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ana@email.com",
    "password": "senha123"
  }'

# Ana entra no Time 1
curl -X POST http://localhost:8080/api/game-participants/join \
  -H "Authorization: Bearer <token_ana>" \
  -H "Content-Type: application/json" \
  -d '{
    "gameId": 123,
    "participationType": "INDIVIDUAL",
    "teamSide": 1
  }'
```

#### **4. Verificar status do jogo**
```bash
curl -X GET http://localhost:8080/api/games/123

# Resposta:
{
  "id": 123,
  "gameName": "Pelada do Sábado",
  "currentPlayerCount": 1,
  "team1Count": 1,
  "team2Count": 0,
  "isTeamsBalanced": false,
  "canStart": false,
  "team1Players": [
    {
      "player": {
        "id": 20,
        "name": "Ana Costa"
      },
      "participationType": "INDIVIDUAL",
      "teamSide": 1
    }
  ],
  "team2Players": []
}
```

---

### 🏆 Fluxo Completo: Criar Time e Convidar Jogadoras

#### **1. Maria cria um time**
```bash
curl -X POST http://localhost:8080/api/teams \
  -H "Authorization: Bearer <token_maria>" \
  -H "Content-Type: application/json" \
  -d '{
    "nameTeam": "Estrelas FC"
  }'

# Resposta: { "id": 1, "nameTeam": "Estrelas FC", "leader": {...}, "playerCount": 1 }
```

#### **2. Maria segue Ana (requisito para convite)**
```bash
curl -X POST http://localhost:8080/api/follow \
  -H "Authorization: Bearer <token_maria>" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": 20,
    "targetUserType": "PLAYER"
  }'
```

#### **3. Ana segue Maria de volta (seguimento mútuo)**
```bash
curl -X POST http://localhost:8080/api/follow \
  -H "Authorization: Bearer <token_ana>" \
  -H "Content-Type: application/json" \
  -d '{
    "targetUserId": 10,
    "targetUserType": "PLAYER"
  }'
```

#### **4. Maria convida Ana para o time**
```bash
curl -X POST http://localhost:8080/api/teams/1/invites \
  -H "Authorization: Bearer <token_maria>" \
  -H "Content-Type: application/json" \
  -d '{
    "invitedPlayerId": 20
  }'

# Resposta: { "id": 1, "team": {...}, "invitedPlayer": {...}, "status": "PENDING" }
```

#### **5. Ana aceita o convite**
```bash
curl -X POST http://localhost:8080/api/teams/invites/1/accept \
  -H "Authorization: Bearer <token_ana>"

# Resposta: { "message": "Invite accepted successfully" }
```

#### **6. Verificar time atualizado**
```bash
curl -X GET http://localhost:8080/api/teams/1

# Resposta:
{
  "id": 1,
  "nameTeam": "Estrelas FC",
  "leader": {
    "id": 10,
    "name": "Maria Silva"
  },
  "players": [
    {"id": 10, "name": "Maria Silva"},
    {"id": 20, "name": "Ana Costa"}
  ],
  "playerCount": 2
}
```

---

### 📝 Fluxo Completo: Criar Post e Curtir

#### **1. Maria cria um post**
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer <token_maria>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Preparando para o próximo treino! 💪⚽",
    "imageUrl": "https://example.com/treino.jpg",
    "type": "TRAINING_UPDATE"
  }'

# Resposta: { "id": 1, "authorName": "Maria Silva", "totalLikes": 0, ... }
```

#### **2. Ana curte o post**
```bash
curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer <token_ana>"

# Resposta: { "id": 1, "userName": "Ana Costa", "userType": "PLAYER", ... }
```

#### **3. Verificar post atualizado**
```bash
curl -X GET http://localhost:8080/api/posts/1

# Resposta:
{
  "id": 1,
  "authorName": "Maria Silva",
  "content": "Preparando para o próximo treino! 💪⚽",
  "totalLikes": 1,
  "isLikedByCurrentUser": false,
  "recentLikes": [
    {
      "id": 1,
      "userName": "Ana Costa",
      "userType": "PLAYER"
    }
  ]
}
```

---

## 🔧 Troubleshooting

### Problemas Comuns

#### **1. Erro 401 Unauthorized**
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required"
}
```

**Solução:**
- Verificar se o token JWT está sendo enviado no header
- Verificar se o token não expirou
- Fazer login novamente para obter novo token

#### **2. Erro 403 Forbidden**
```json
{
  "error": "Forbidden",
  "message": "Access Denied"
}
```

**Solução:**
- Verificar se o usuário tem a role correta para o endpoint
- Exemplo: Apenas PLAYER pode criar jogos amistosos

#### **3. Erro de Validação**
```json
{
  "error": "Bad Request",
  "message": "Minimum players must be an even number for balanced teams"
}
```

**Solução:**
- Verificar os campos obrigatórios
- Garantir que números de jogadoras sejam pares
- Verificar formato de datas (ISO 8601)

#### **4. Erro de Negócio**
```json
{
  "error": "Business Exception",
  "message": "Player is already participating in this game"
}
```

**Solução:**
- Verificar regras de negócio específicas
- Exemplo: Não pode entrar duas vezes no mesmo jogo

#### **5. JWT Malformado**
```json
{
  "error": "Invalid JWT token"
}
```

**Solução:**
- Verificar se o token está completo
- Verificar se não há espaços extras
- Fazer login novamente

---

### Códigos de Status HTTP

| Código | Significado | Quando Ocorre |
|--------|-------------|---------------|
| 200 | OK | Requisição bem-sucedida |
| 201 | Created | Recurso criado com sucesso |
| 204 | No Content | Operação bem-sucedida sem retorno |
| 400 | Bad Request | Dados inválidos ou faltando |
| 401 | Unauthorized | Token ausente ou inválido |
| 403 | Forbidden | Sem permissão para a operação |
| 404 | Not Found | Recurso não encontrado |
| 409 | Conflict | Conflito (ex: usuário já existe) |
| 500 | Internal Server Error | Erro no servidor |

---

### Validações Importantes

#### **Jogos:**
- ✅ `minPlayers` e `maxPlayers` devem ser pares
- ✅ `minPlayers` ≤ `maxPlayers`
- ✅ `gameDate` deve ser no futuro
- ✅ `teamSide` deve ser 1 ou 2

#### **Times:**
- ✅ Apenas líder pode convidar/remover
- ✅ Seguimento mútuo obrigatório para convites
- ✅ Líder não pode sair do time

#### **Participação:**
- ✅ Jogo não pode ter começado
- ✅ Não pode exceder máximo de jogadoras
- ✅ Com `WITH_TEAM`, jogadora deve ter time

#### **Posts:**
- ✅ Apenas autor pode editar/deletar
- ✅ Não pode curtir duas vezes
- ✅ `content` é obrigatório

---

## 📚 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.x**
- **Spring Security** (JWT)
- **Spring Data JPA**
- **MySQL 8.0**
- **Lombok**
- **Maven**

---

## 🎯 Próximos Passos

1. **Implementar sistema de notificações**
2. **Adicionar chat em tempo real**
3. **Sistema de rankings e estatísticas**
4. **Upload de imagens direto na API**
5. **Sistema de comentários em posts**
6. **Filtros avançados de busca**
7. **Dashboard de analytics**

---

## 📄 Licença

Este projeto está sob a licença MIT.

---

## 👥 Contribuidores

- **Equipe API Passa Bola**

---

## 📞 Suporte

Para dúvidas ou problemas:
- 📧 Email: suporte@apipassabola.com
- 📱 WhatsApp: (11) 99999-9999
- 🌐 Website: https://apipassabola.com

---

**🏆 API Passa Bola - Conectando o futebol feminino! ⚽**
