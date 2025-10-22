# 🧪 Teste Rápido WebSocket no Postman - Chat Universal

## 🎉 **CHAT UNIVERSAL ATIVO!**
✅ PLAYER, ORGANIZATION e SPECTATOR podem conversar entre si  
✅ Usa **userId global (snowflake)** para identificação

## ⚠️ **ATENÇÃO - FORMATO STOMP CRÍTICO**

O protocolo STOMP é **MUITO SENSÍVEL** ao formato. Siga **EXATAMENTE** como mostrado:

### ✅ **FORMATO CORRETO:**
```
CONNECT
Authorization:Bearer TOKEN_AQUI
accept-version:1.1,1.0
heart-beat:10000,10000

```

### ❌ **ERROS COMUNS:**
```
❌ ERRADO - Espaço após dois pontos:
Authorization: Bearer TOKEN_AQUI

❌ ERRADO - Sem dois pontos:
Authorization Bearer TOKEN_AQUI

❌ ERRADO - Sem linha em branco no final
```

---

## 📋 Pré-requisitos
1. Aplicação rodando: `mvn spring-boot:run`
2. Dois usuários cadastrados (para testar conversa)
3. Tokens JWT dos dois usuários

---

## 🚀 PASSO A PASSO

### **PASSO 1: Obter Token JWT**

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "jamilton@email.com",
  "password": "sua_senha"
}
```

**Copie o token E o userId da resposta:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "userId": "1083690260503501183",  // ← COPIE ESTE userId!
  "username": "jamilton",
  "email": "jamilton@email.com",
  "role": "PLAYER",
  "entityId": 8
}
```

⚠️ **IMPORTANTE:** Anote o **userId** (número grande) - você vai usar no chat!

---

### **PASSO 2: Conectar WebSocket**

1. No Postman, clique em **"New"** → **"WebSocket Request"**
2. **URL:** `ws://localhost:8080/ws-chat`
3. Clique em **"Connect"**
4. Aguarde: `Connected to ws://localhost:8080/ws-chat`

---

### **PASSO 3: Autenticar (Frame CONNECT)**

Na área de mensagem do Postman, **copie e cole EXATAMENTE assim**:

```
CONNECT
Authorization:Bearer SEU_TOKEN_AQUI
accept-version:1.1,1.0
heart-beat:10000,10000

```

**⚠️ REGRAS CRÍTICAS DO STOMP:**
1. **Substitua `SEU_TOKEN_AQUI`** pelo seu token JWT completo
2. **NÃO coloque espaço** após os dois pontos (`:`)
3. **Deixe UMA linha em branco** no final (pressione Enter duas vezes)
4. **NÃO adicione espaços extras** em nenhum lugar
5. Cada header deve estar em **UMA linha separada**

**Resposta esperada:**
```
CONNECTED
version:1.1
heart-beat:0,0
```

---

### **PASSO 4: Subscribe para Receber Mensagens**

**Copie e cole EXATAMENTE assim:**

```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

**⚠️ ATENÇÃO:**
- **NÃO coloque espaço** após os dois pontos
- Deixe **UMA linha em branco** no final
- Não terá resposta, mas agora você está inscrito!

---

### **PASSO 5: Enviar Mensagem**

**Copie e cole EXATAMENTE assim:**

```
SEND
destination:/app/chat.send
content-type:application/json

{"recipientId":1578941265158776642,"content":"Oi! Teste de mensagem em tempo real!"}
```

**⚠️ REGRAS CRÍTICAS:**
1. **NÃO coloque espaço** após os dois pontos nos headers
2. Deixe **UMA linha em branco** entre headers e JSON
3. Substitua `recipientId` pelo **userId global (snowflake)** do destinatário
4. O JSON deve estar em **UMA linha só**
5. O `senderId` é detectado automaticamente do seu token JWT

**IMPORTANTE:** Use o **userId** (número grande), NÃO o entityId!

---

### **PASSO 6: Abrir Segunda Aba (Destinatário)**

Para testar o recebimento em tempo real:

1. Abra **OUTRA aba WebSocket** no Postman
2. Repita os passos 2-4 com o **token do destinatário**
3. Quando você enviar mensagem na primeira aba, verá aparecer na segunda **INSTANTANEAMENTE**!

---

## 📊 **EXEMPLO COMPLETO**

### Aba 1 (Jamilton - PLAYER - userId: 1083690260503501183)
```
// 1. Conectar
ws://localhost:8080/ws-chat

// 2. CONNECT
CONNECT
Authorization:Bearer TOKEN_DO_JAMILTON
accept-version:1.1,1.0
heart-beat:10000,10000

// 3. SUBSCRIBE
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

// 4. SEND (para uma ORGANIZATION)
SEND
destination:/app/chat.send
content-type:application/json

{"recipientId":1578941265158776642,"content":"Oi Clube! Posso treinar aí?"}
```

### Aba 2 (Clube ABC - ORGANIZATION - userId: 1578941265158776642)
```
// 1. Conectar
ws://localhost:8080/ws-chat

// 2. CONNECT
CONNECT
Authorization:Bearer TOKEN_DA_MARIA
accept-version:1.1,1.0
heart-beat:10000,10000

// 3. SUBSCRIBE
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

// 4. Aguardar mensagem do Jamilton aparecer automaticamente!
```

**Clube ABC receberá:**
```json
{
  "id": 1,
  "senderId": 1083690260503501183,
  "senderUsername": "jamilton",
  "senderName": "Jamilton Santos",
  "senderType": "PLAYER",
  "recipientId": 1578941265158776642,
  "recipientUsername": "maria_silva",
  "recipientName": "Maria Silva",
  "content": "Oi Maria! Vamos treinar?",
  "isRead": false,
  "createdAt": "2025-10-21T23:20:00"
}
```

---

## ✅ **CHECKLIST DE TESTE**

- [ ] Aplicação rodando na porta 8080
- [ ] Login realizado (token JWT obtido)
- [ ] WebSocket conectado (`ws://localhost:8080/ws-chat`)
- [ ] Frame CONNECT enviado com token
- [ ] Resposta CONNECTED recebida
- [ ] SUBSCRIBE realizado em `/user/queue/messages`
- [ ] Mensagem enviada para `/app/chat.send`
- [ ] Segunda aba aberta com outro usuário
- [ ] Mensagem recebida em tempo real na segunda aba

---

## 🐛 **TROUBLESHOOTING**

### ❌ Erro: "Illegal header: 'SEND' or 'CONNECT'"
**CAUSA:** Formato STOMP incorreto - falta dois pontos (`:`) nos headers

**SOLUÇÃO:**
```
❌ ERRADO:
SEND
destination /app/chat.send    <-- SEM dois pontos!

✅ CORRETO:
SEND
destination:/app/chat.send    <-- COM dois pontos!
```

**CHECKLIST:**
- [ ] Todos os headers têm `:` (dois pontos) após o nome
- [ ] NÃO há espaço após os dois pontos
- [ ] Há UMA linha em branco no final do frame
- [ ] Copiou e colou exatamente como no guia

### 📭 Mensagem não chega no destinatário

**PASSO 1: Verificar logs do servidor**

Abra o terminal onde a aplicação está rodando e procure por:

```
✅ Logs esperados ao enviar mensagem:
📨 [WebSocket] Received message request: recipientId=1578941265158776642, content=Oi!
💾 [WebSocket] Message saved to DB: id=1, senderId=1083690260503501183, recipientId=1578941265158776642
📤 [WebSocket] Attempting to send to user: email=clube@email.com, destination=/queue/messages
✅ [WebSocket] Message sent successfully from 1083690260503501183 to 1578941265158776642 (email: clube@email.com)
```

**PASSO 2: Verificar autenticação**

Procure por logs de autenticação:

```
✅ Logs esperados ao conectar:
🔌 [WebSocket] Received STOMP command: CONNECT
🔑 [WebSocket] Authorization header present: true
🎫 [WebSocket] Token extracted, length: 200+
🔐 [WebSocket] Starting authentication...
📧 [WebSocket] Extracted email from token: usuario@email.com
👤 [WebSocket] User found: usuario@email.com, authorities: [ROLE_PLAYER]
✅ [WebSocket] Authentication successful for user: usuario@email.com
```

**PASSO 3: Verificar SUBSCRIBE**

Procure por:

```
✅ Log esperado ao fazer SUBSCRIBE:
📬 [WebSocket] SUBSCRIBE command - Destination: /user/queue/messages, User: usuario@email.com
```

**PROBLEMAS COMUNS:**

1. **User: ANONYMOUS** - Token JWT não foi aceito
   - Verifique se o token está correto
   - Verifique se não expirou (faça login novamente)
   - Verifique formato: `Authorization:Bearer TOKEN` (sem espaço após `:`)

2. **Mensagem salva mas não chega** - Destinatário não está conectado
   - Abra segunda aba no Postman com token do destinatário
   - Faça CONNECT e SUBSCRIBE antes de enviar mensagem

3. **recipientId não existe** - userId do destinatário está errado
   - Verifique se o usuário existe no banco
   - Use o **userId global (snowflake)** correto do destinatário
   - Exemplo: `1083690260503501183` (número grande)

### Erro 400 ao conectar
- ✅ Verifique se a URL é `ws://localhost:8080/ws-chat` (sem SockJS)
- ✅ Verifique se a aplicação está rodando

### Não recebe CONNECTED
- ✅ Verifique se o token JWT está correto e completo
- ✅ Verifique se não há espaço entre `Bearer` e o token
- ✅ Verifique se deixou linha em branco no final do frame CONNECT
- ✅ Verifique se todos os headers têm `:` (dois pontos)

### Não recebe mensagens
- ✅ Verifique se fez SUBSCRIBE em `/user/queue/messages`
- ✅ Verifique se o `recipientId` está correto
- ✅ Verifique os logs do servidor
- ✅ Verifique se o JSON está em uma linha só

### Token expirado
- ✅ Faça login novamente para obter novo token
- ✅ Tokens JWT expiram em 24 horas

---

## 📝 **NOTAS**

### **Como funciona o roteamento:**
- **senderId:** Detectado automaticamente do token JWT (não precisa enviar)
- **recipientId:** **userId global (snowflake)** do destinatário (você envia isso)
- **Roteamento interno:** Servidor busca o EMAIL do destinatário pelo userId e roteia a mensagem
- **Identificação WebSocket:** Spring usa EMAIL (não userId) para identificar usuários conectados
- **Universal:** Funciona entre PLAYER, ORGANIZATION e SPECTATOR

### **Outras informações:**
- **content:** Texto da mensagem (obrigatório)
- **Tempo real:** Ambos os usuários precisam estar conectados via WebSocket
- **Persistência:** Todas as mensagens são salvas no banco de dados

---

## 🎯 **PRÓXIMOS PASSOS**

Após validar no Postman:
1. ✅ Commit e push para main
2. ✅ Frontend pode integrar usando SockJS (`ws://localhost:8080/ws-chat-sockjs`)
3. ✅ Consultar `README.md` para exemplos de código JavaScript/React
