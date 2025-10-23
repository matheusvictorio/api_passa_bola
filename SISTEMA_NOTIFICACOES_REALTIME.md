# 🔔 Sistema de Notificações em Tempo Real

## 📋 Visão Geral

Sistema completo de notificações em tempo real usando **WebSocket** com **persistência no banco de dados**. Todas as notificações são salvas no banco E enviadas instantaneamente via WebSocket para o usuário.

## 🎯 Funcionalidades Implementadas

### ✅ Tipos de Notificações

1. **TEAM_INVITE_RECEIVED** - Você recebeu um convite para entrar em um time
2. **TEAM_INVITE_ACCEPTED** - Seu convite foi aceito
3. **TEAM_INVITE_REJECTED** - Seu convite foi rejeitado
4. **NEW_FOLLOWER** - Alguém começou a seguir você
5. **POST_LIKED** - Alguém curtiu seu post
6. **GAME_INVITE_RECEIVED** - Você recebeu um convite para um jogo (preparado para futuro)
7. **GAME_INVITE_ACCEPTED** - Seu convite de jogo foi aceito (preparado para futuro)
8. **GAME_INVITE_REJECTED** - Seu convite de jogo foi rejeitado (preparado para futuro)
9. **PLAYER_JOINED_TEAM** - Uma jogadora entrou no seu time (preparado para futuro)
10. **PLAYER_LEFT_TEAM** - Uma jogadora saiu do seu time (preparado para futuro)
11. **PLAYER_REMOVED_FROM_TEAM** - Você foi removida do time (preparado para futuro)
12. **SYSTEM_ANNOUNCEMENT** - Anúncio do sistema (preparado para futuro)

### ✅ Notificações Ativas Atualmente

- ✅ **Convites de Time** - Quando alguém te convida ou aceita seu convite
- ✅ **Novos Seguidores** - Quando alguém começa a seguir você
- ✅ **Likes em Posts** - Quando alguém curte seu post

## 🏗️ Arquitetura

### Entidades

```java
Notification {
    Long id;
    Long recipientId;        // userId global do destinatário
    UserType recipientType;  // PLAYER, ORGANIZATION, SPECTATOR
    Long senderId;           // userId global do remetente
    UserType senderType;     // PLAYER, ORGANIZATION, SPECTATOR
    String senderUsername;
    String senderName;
    NotificationType type;
    String message;
    String metadata;         // JSON com dados extras
    String actionUrl;        // Link para ação (ex: /teams/123/invites/456)
    Boolean isRead;
    LocalDateTime createdAt;
    LocalDateTime readAt;
}
```

### Como Funciona

1. **Ação do Usuário** → Ex: Jogadora A convida Jogadora B para um time
2. **Service cria notificação** → `NotificationService.notifyTeamInviteReceived()`
3. **Salva no banco** → Registro persistido na tabela `notifications`
4. **Envia via WebSocket** → Notificação enviada para `/topic/notifications/player/123`
5. **Frontend recebe** → Notificação aparece instantaneamente para Jogadora B

## 📡 Endpoints REST

### Buscar Notificações

```http
GET /api/notifications
Query Params: page=0, size=20
Auth: Required
Response: Page<NotificationResponse>
```

### Buscar Apenas Não Lidas

```http
GET /api/notifications/unread
Query Params: page=0, size=20
Auth: Required
Response: Page<NotificationResponse>
```

### Contar Não Lidas

```http
GET /api/notifications/unread/count
Auth: Required
Response: { "unreadCount": 5 }
```

### Buscar Recentes (últimas 24h)

```http
GET /api/notifications/recent
Auth: Required
Response: List<NotificationResponse>
```

### Marcar Como Lida

```http
PATCH /api/notifications/{id}/read
Auth: Required
Response: { "message": "Notificação marcada como lida" }
```

### Marcar Todas Como Lidas

```http
PATCH /api/notifications/read-all
Auth: Required
Response: { "message": "Todas as notificações foram marcadas como lidas", "count": 10 }
```

### Deletar Notificação

```http
DELETE /api/notifications/{id}
Auth: Required
Response: { "message": "Notificação deletada com sucesso" }
```

## 🔌 WebSocket - Conexão em Tempo Real

### 1. Conectar ao WebSocket

```javascript
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({
    'Authorization': 'Bearer ' + token
}, function(frame) {
    console.log('Connected: ' + frame);
    subscribeToNotifications();
});
```

### 2. Inscrever-se no Tópico de Notificações

O tópico é específico para cada usuário baseado no **userId global** e **tipo**:

```javascript
// Para PLAYER com userId 123
stompClient.subscribe('/topic/notifications/player/123', function(notification) {
    const notif = JSON.parse(notification.body);
    showNotification(notif);
});

// Para ORGANIZATION com userId 456
stompClient.subscribe('/topic/notifications/organization/456', function(notification) {
    const notif = JSON.parse(notification.body);
    showNotification(notif);
});

// Para SPECTATOR com userId 789
stompClient.subscribe('/topic/notifications/spectator/789', function(notification) {
    const notif = JSON.parse(notification.body);
    showNotification(notif);
});
```

### 3. Receber Contador de Não Lidas

```javascript
// Inscrever-se no tópico de contador
stompClient.subscribe('/topic/notifications/player/123/count', function(update) {
    const data = JSON.parse(update.body);
    updateBadge(data.unreadCount);
});
```

## 📦 Estrutura da Notificação Recebida

```json
{
    "id": 1,
    "senderId": 456,
    "senderType": "PLAYER",
    "senderUsername": "maria_silva",
    "senderName": "Maria Silva",
    "type": "TEAM_INVITE_RECEIVED",
    "message": "Maria Silva convidou você para entrar no time As Incríveis",
    "metadata": "{\"teamId\":10,\"teamName\":\"As Incríveis\",\"inviteId\":25}",
    "actionUrl": "/teams/10/invites/25",
    "isRead": false,
    "createdAt": "2025-10-22T23:15:00",
    "readAt": null
}
```

## 🎨 Exemplo de Implementação Frontend

### React + SockJS + Stomp

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

class NotificationService {
    constructor(userId, userType, token) {
        this.userId = userId;
        this.userType = userType.toLowerCase(); // player, organization, spectator
        this.token = token;
        this.stompClient = null;
    }

    connect(onNotificationReceived, onCountUpdate) {
        const socket = new SockJS('http://localhost:8080/ws');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({
            'Authorization': `Bearer ${this.token}`
        }, () => {
            // Inscrever-se em notificações
            this.stompClient.subscribe(
                `/topic/notifications/${this.userType}/${this.userId}`,
                (message) => {
                    const notification = JSON.parse(message.body);
                    onNotificationReceived(notification);
                }
            );

            // Inscrever-se em atualizações de contador
            this.stompClient.subscribe(
                `/topic/notifications/${this.userType}/${this.userId}/count`,
                (message) => {
                    const data = JSON.parse(message.body);
                    onCountUpdate(data.unreadCount);
                }
            );
        });
    }

    disconnect() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}

// Uso:
const notifService = new NotificationService(123, 'PLAYER', authToken);

notifService.connect(
    (notification) => {
        // Mostrar notificação
        toast.info(notification.message);
        // Adicionar à lista
        addNotificationToList(notification);
    },
    (unreadCount) => {
        // Atualizar badge
        updateNotificationBadge(unreadCount);
    }
);
```

### Vue.js Exemplo

```javascript
export default {
    data() {
        return {
            notifications: [],
            unreadCount: 0,
            stompClient: null
        }
    },
    
    mounted() {
        this.connectWebSocket();
        this.loadNotifications();
    },
    
    methods: {
        connectWebSocket() {
            const socket = new SockJS('http://localhost:8080/ws');
            this.stompClient = Stomp.over(socket);
            
            this.stompClient.connect({
                'Authorization': `Bearer ${this.$store.state.token}`
            }, () => {
                const userId = this.$store.state.user.userId;
                const userType = this.$store.state.user.userType.toLowerCase();
                
                // Notificações
                this.stompClient.subscribe(
                    `/topic/notifications/${userType}/${userId}`,
                    (message) => {
                        const notif = JSON.parse(message.body);
                        this.notifications.unshift(notif);
                        this.unreadCount++;
                        this.showToast(notif);
                    }
                );
                
                // Contador
                this.stompClient.subscribe(
                    `/topic/notifications/${userType}/${userId}/count`,
                    (message) => {
                        const data = JSON.parse(message.body);
                        this.unreadCount = data.unreadCount;
                    }
                );
            });
        },
        
        async loadNotifications() {
            const response = await fetch('/api/notifications?page=0&size=20', {
                headers: {
                    'Authorization': `Bearer ${this.$store.state.token}`
                }
            });
            const data = await response.json();
            this.notifications = data.content;
        },
        
        async markAsRead(notificationId) {
            await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'PATCH',
                headers: {
                    'Authorization': `Bearer ${this.$store.state.token}`
                }
            });
        },
        
        showToast(notification) {
            this.$toast.info(notification.message, {
                onClick: () => {
                    if (notification.actionUrl) {
                        this.$router.push(notification.actionUrl);
                    }
                }
            });
        }
    },
    
    beforeUnmount() {
        if (this.stompClient) {
            this.stompClient.disconnect();
        }
    }
}
```

## 🔍 Metadados por Tipo de Notificação

### TEAM_INVITE_RECEIVED
```json
{
    "teamId": 10,
    "teamName": "As Incríveis",
    "inviteId": 25
}
```

### TEAM_INVITE_ACCEPTED
```json
{
    "teamId": 10,
    "teamName": "As Incríveis",
    "playerId": 456
}
```

### NEW_FOLLOWER
```json
{
    "followerId": 456,
    "followerType": "PLAYER"
}
```

### POST_LIKED
```json
{
    "postId": 789,
    "likerId": 456
}
```

## 🔐 Segurança

- ✅ Todas as operações requerem autenticação JWT
- ✅ Usuários só podem ver suas próprias notificações
- ✅ Usuários só podem marcar como lidas suas próprias notificações
- ✅ WebSocket usa o mesmo token JWT para autenticação

## 📊 Performance

- ✅ Notificações antigas (>30 dias) podem ser limpas automaticamente
- ✅ Paginação em todos os endpoints de listagem
- ✅ Índices no banco para queries rápidas por recipientId e recipientType
- ✅ WebSocket mantém conexão persistente (baixo overhead)

## 🧪 Testando o Sistema

### 1. Testar Convite de Time

```bash
# Jogadora A convida Jogadora B
POST /api/teams/1/invites
{
    "invitedPlayerId": 2
}

# Jogadora B recebe notificação instantaneamente via WebSocket
# E também pode buscar via REST:
GET /api/notifications
```

### 2. Testar Like em Post

```bash
# Usuário A curte post do Usuário B
POST /api/posts/123/like

# Usuário B recebe notificação instantaneamente
```

### 3. Testar Follow

```bash
# Usuário A segue Usuário B
POST /api/follow
{
    "targetUserId": "456",
    "targetUserType": "PLAYER"
}

# Usuário B recebe notificação instantaneamente
```

## 🚀 Próximos Passos (Preparado para Futuro)

O sistema já está preparado para adicionar facilmente:

1. **Notificações de Jogos** - Convites e atualizações de jogos
2. **Notificações de Time** - Quando jogadoras entram/saem
3. **Anúncios do Sistema** - Mensagens gerais para todos
4. **Notificações Push** - Integração com Firebase/OneSignal
5. **Preferências de Notificação** - Usuário escolher quais receber

## 📝 Notas Importantes

1. **userId Global**: Usamos o `userId` global (único em todo sistema) + `userType` para identificar usuários
2. **Persistência**: Todas as notificações são salvas no banco, mesmo que o usuário esteja offline
3. **Tempo Real**: Se o usuário estiver online, recebe instantaneamente via WebSocket
4. **Fallback**: Se WebSocket falhar, usuário ainda pode buscar via REST
5. **Não Duplicação**: Notificações não são enviadas para o próprio usuário (ex: não notifica quando você curte seu próprio post)

## 🎯 Resumo Técnico

- **Backend**: Spring Boot + WebSocket (STOMP) + JPA
- **Banco**: MySQL com tabela `notifications`
- **Tempo Real**: SockJS + STOMP sobre WebSocket
- **Autenticação**: JWT Bearer Token
- **Padrão**: Pub/Sub com tópicos específicos por usuário
- **Escalabilidade**: Pronto para Redis/RabbitMQ se necessário no futuro
