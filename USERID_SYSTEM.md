# Sistema de User ID Global

## 🎯 Problema Resolvido

Anteriormente, o sistema de seguimento usava apenas o `id` da entidade específica (Player.id, Organization.id, Spectator.id), o que causava confusão porque:
- Player com id=1, Organization com id=1 e Spectator com id=1 eram três usuários diferentes
- O `targetUserId` no `FollowRequest` não era único globalmente
- Isso causava erros 403 Forbidden ao tentar seguir usuários

## ✅ Solução Implementada

Adicionamos um campo **`userId` único e global** em todas as entidades de usuário:

### Estrutura das Entidades

```java
// Player.java
@Column(name = "user_id", unique = true, nullable = false)
private Long userId;

// Organization.java
@Column(name = "user_id", unique = true, nullable = false)
private Long userId;

// Spectator.java
@Column(name = "user_id", unique = true, nullable = false)
private Long userId;
```

### Características do userId

1. **Único Globalmente**: Não há dois usuários com o mesmo userId, independente do tipo
2. **Gerado Automaticamente**: Criado durante o registro pelo `UserIdGeneratorService`
3. **Imutável**: Uma vez criado, nunca muda
4. **Independente do ID da Entidade**: O `id` continua existindo para uso interno

## 📊 Comparação: id vs userId

| Campo | Escopo | Unicidade | Uso |
|-------|--------|-----------|-----|
| `id` | Por entidade | Único dentro da entidade | Queries específicas, relacionamentos JPA |
| `userId` | Global | Único em todo o sistema | Sistema de seguimento, identificação cross-type |

### Exemplo Prático

```
Player:
  - id: 1
  - userId: 8472639485726394

Organization:
  - id: 1  
  - userId: 9384756283947562

Spectator:
  - id: 1
  - userId: 1029384756102938
```

Agora cada usuário tem um `userId` único, mesmo que tenham o mesmo `id` na sua tabela.

## 🔧 Como Usar

### 1. Registro de Usuário

O `userId` é gerado automaticamente durante o registro:

```java
// AuthService.java
Player player = new Player();
player.setUserId(userIdGeneratorService.generateUniqueUserId()); // Gerado automaticamente
player.setUsername(request.getUsername());
// ... outros campos
```

### 2. Sistema de Seguimento

Agora use o `userId` no `FollowRequest`:

```json
POST /api/follow
{
  "targetUserId": 8472639485726394,  // Use o userId, não o id
  "targetUserType": "PLAYER"
}
```

### 3. Respostas da API

Todas as respostas agora incluem ambos os campos:

```json
{
  "id": 1,                    // ID da entidade (uso interno)
  "userId": 8472639485726394, // ID global único
  "username": "maria_silva",
  "name": "Maria Silva",
  "userType": "PLAYER"
}
```

## 🔍 Endpoints Afetados

### GET /api/players/{id}
```json
{
  "id": 1,
  "userId": 8472639485726394,  // ← Novo campo
  "username": "maria_silva",
  "name": "Maria Silva"
}
```

### GET /api/follow/my-following
```json
[
  {
    "id": 2,
    "userId": 9384756283947562,  // ← Novo campo
    "username": "clube_abc",
    "userType": "ORGANIZATION"
  }
]
```

### POST /api/follow
```json
{
  "targetUserId": 9384756283947562,  // ← Use userId aqui
  "targetUserType": "ORGANIZATION"
}
```

## 🛠️ Componentes Técnicos

### UserIdGeneratorService

Serviço responsável por gerar IDs únicos:

```java
@Service
public class UserIdGeneratorService {
    
    public Long generateUniqueUserId() {
        Long userId;
        do {
            userId = Math.abs(random.nextLong());
        } while (userIdExists(userId));
        return userId;
    }
    
    private boolean userIdExists(Long userId) {
        return playerRepository.existsByUserId(userId) ||
               organizationRepository.existsByUserId(userId) ||
               spectatorRepository.existsByUserId(userId);
    }
}
```

### Métodos Adicionados nos Repositories

```java
// PlayerRepository, OrganizationRepository, SpectatorRepository
boolean existsByUserId(Long userId);
Optional<T> findByUserId(Long userId);
```

## 📝 Migração de Dados Existentes

Para usuários já cadastrados no banco de dados, será necessário:

1. **Adicionar coluna `user_id`** nas tabelas:
```sql
ALTER TABLE players ADD COLUMN user_id BIGINT UNIQUE;
ALTER TABLE organizations ADD COLUMN user_id BIGINT UNIQUE;
ALTER TABLE spectators ADD COLUMN user_id BIGINT UNIQUE;
```

2. **Popular com valores únicos**:
```sql
-- Script de migração necessário para gerar userId únicos
-- para registros existentes
```

## ✨ Benefícios

1. **Simplicidade**: Um único ID para identificar qualquer usuário
2. **Clareza**: Não há confusão entre IDs de diferentes entidades
3. **Escalabilidade**: Fácil adicionar novos tipos de usuário
4. **Consistência**: Mesmo padrão para todo o sistema de seguimento
5. **Debugging**: Mais fácil rastrear problemas com IDs únicos globais

## 🚀 Próximos Passos

1. ✅ Implementar geração de userId no registro
2. ✅ Atualizar DTOs para incluir userId
3. ✅ Atualizar FollowService para usar userId
4. ⏳ Criar script de migração para dados existentes
5. ⏳ Atualizar documentação da API
6. ⏳ Testar sistema de seguimento com userId

## 📚 Referências

- `UserIdGeneratorService.java` - Geração de IDs únicos
- `Player.java`, `Organization.java`, `Spectator.java` - Entidades com userId
- `FollowService.java` - Sistema de seguimento atualizado
- `FollowResponse.java` - DTO com userId
