# 🔍 DEBUG COMPLETO - WebSocket Chat Universal

## 🎉 **CHAT AGORA É UNIVERSAL!**

✅ **PLAYER** pode conversar com **PLAYER**, **ORGANIZATION**, **SPECTATOR**  
✅ **ORGANIZATION** pode conversar com **PLAYER**, **ORGANIZATION**, **SPECTATOR**  
✅ **SPECTATOR** pode conversar com **PLAYER**, **ORGANIZATION**, **SPECTATOR**

---

## 🚀 PASSO 1: Reinicie a Aplicação

```bash
# Pare a aplicação (Ctrl+C)
# Inicie novamente
mvn spring-boot:run
```

---

## 📋 PASSO 2: Prepare Dois Usuários (QUALQUER TIPO)

Você precisa de **DOIS usuários diferentes** com seus tokens JWT.

### ✅ **AGORA USA userId GLOBAL (Snowflake)!**

Faça login e veja a resposta:
```json
{
  "token": "eyJhbGci...",
  "userId": "1083690260503501183",  // ✅ USE ESTE NO CHAT!
  "username": "jamilton",
  "email": "jamilton@email.com",
  "role": "PLAYER",
  "entityId": 8
}
```

### **Usuário 1 (Remetente) - Pode ser QUALQUER tipo:**
- Email: `jamilton@email.com`
- **userId (global):** `1083690260503501183` ← Use este!
- Tipo: `PLAYER`
- Token: `eyJhbGci...` (faça login e copie o token)

### **Usuário 2 (Destinatário) - Pode ser QUALQUER tipo:**
- Email: `organizacao@email.com`
- **userId (global):** `1578941265158776642` ← Use este!
- Tipo: `ORGANIZATION`
- Token: `eyJhbGci...` (faça login e copie o token)

---

## 🧪 PASSO 3: Teste no Postman

### **ABA 1 - Destinatário (Abra PRIMEIRO)**

1. **Conecte:**
   - URL: `ws://localhost:8080/ws-chat`
   - Clique em "Connect"

2. **Autentique (CONNECT):**
```
CONNECT
Authorization:Bearer TOKEN_DO_USUARIO_2
accept-version:1.1,1.0
heart-beat:10000,10000

```

3. **Subscribe:**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

**AGUARDE AQUI - NÃO FECHE ESTA ABA!**

---

### **ABA 2 - Remetente**

1. **Conecte:**
   - URL: `ws://localhost:8080/ws-chat`
   - Clique em "Connect"

2. **Autentique (CONNECT):**
```
CONNECT
Authorization:Bearer TOKEN_DO_USUARIO_1
accept-version:1.1,1.0
heart-beat:10000,10000

```

3. **Subscribe (opcional, mas recomendado):**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

4. **Envie mensagem para o Usuário 2:**
```
SEND
destination:/app/chat.send
content-type:application/json

{"recipientId":1578941265158776642,"content":"Oi! Chat universal funcionando!"}
```

**IMPORTANTE:** 
- Substitua `recipientId` pelo **userId global** do Usuário 2
- Use o número grande (snowflake) que aparece no campo `"userId"` da resposta de login
- Exemplo: se login retornou `"userId": "1578941265158776642"`, use `"recipientId":1578941265158776642`
- **Funciona entre QUALQUER tipo de usuário!**

---

## 📊 PASSO 4: Verifique os Logs

Volte ao terminal onde a aplicação está rodando e procure por:

### ✅ **Logs Esperados - Conexão do Destinatário:**

```
🔌 [WebSocket] Received STOMP command: CONNECT
🔑 [WebSocket] Authorization header present: true
🎫 [WebSocket] Token extracted, length: 200+
🔐 [WebSocket] Starting authentication...
📧 [WebSocket] Extracted email from token: usuario2@email.com
👤 [WebSocket] User found: usuario2@email.com, authorities: [ROLE_PLAYER]
✅ [WebSocket] Authentication successful for user: usuario2@email.com
🟢 [WebSocket] User CONNECTED: usuario2@email.com (sessionId: abc123)
📊 [WebSocket] Total active sessions: 1
👥 [WebSocket] Connected users: [usuario2@email.com]
```

### ✅ **Logs Esperados - Subscribe do Destinatário:**

```
📬 [WebSocket] SUBSCRIBE command - Destination: /user/queue/messages, User: usuario2@email.com
📬 [WebSocket] User SUBSCRIBED: usuario2@email.com to /user/queue/messages (sessionId: abc123)
📊 [WebSocket] Active subscriptions: 1
```

### ✅ **Logs Esperados - Conexão do Remetente:**

```
🟢 [WebSocket] User CONNECTED: usuario1@email.com (sessionId: xyz789)
📊 [WebSocket] Total active sessions: 2
👥 [WebSocket] Connected users: [usuario2@email.com, usuario1@email.com]
```

### ✅ **Logs Esperados - Envio de Mensagem:**

```
📨 [WebSocket] Received message request: recipientId=2, content=Teste de mensagem!
💾 [WebSocket] Message saved to DB: id=1, senderId=1, recipientId=2
📤 [WebSocket] Attempting to send to user: email=usuario2@email.com, destination=/queue/messages
📦 [WebSocket] Message payload: ChatMessageResponse(id=1, senderId=1, ...)
✅ [WebSocket] convertAndSendToUser() completed for email: usuario2@email.com
📤 [OUTBOUND] Command: MESSAGE, Destination: /user/usuario2@email.com/queue/messages, User: null
✅ [WebSocket] Message sent successfully from 1 to 2 (email: usuario2@email.com)
```

---

## ❌ PROBLEMAS COMUNS

### **1. User: ANONYMOUS nos logs**
**Causa:** Token JWT não foi aceito

**Solução:**
- Verifique formato: `Authorization:Bearer TOKEN` (sem espaço após `:`)
- Faça login novamente (token pode ter expirado)
- Copie o token COMPLETO

### **2. Total active sessions: 0**
**Causa:** Nenhum usuário conectado

**Solução:**
- Verifique se fez CONNECT com token válido
- Verifique se recebeu resposta CONNECTED

### **3. Message sent successfully mas não chega**
**Causa:** Destinatário não está conectado OU não fez SUBSCRIBE

**Solução:**
- Verifique se o destinatário aparece em "Connected users"
- Verifique se o destinatário fez SUBSCRIBE em `/user/queue/messages`
- Verifique se o email no log de envio está correto

### **4. Recipient not found**
**Causa:** recipientId está errado

**Solução:**
- Verifique o ID correto do destinatário no banco
- Use o ID numérico, não o email

---

## 📸 COPIE E COLE OS LOGS AQUI

Se ainda não funcionar, **copie e cole TODOS os logs** desde o momento que você:
1. Conectou o destinatário
2. Conectou o remetente
3. Enviou a mensagem

Vou analisar e te dizer exatamente onde está o problema!

---

## 🎯 CHECKLIST FINAL

Antes de testar, confirme:

- [ ] Aplicação reiniciada com novo código
- [ ] Dois usuários diferentes (emails e IDs diferentes)
- [ ] Dois tokens JWT válidos (não expirados)
- [ ] ABA 1: Destinatário conectado E subscribed
- [ ] ABA 2: Remetente conectado
- [ ] recipientId correto na mensagem (ID do destinatário)
- [ ] Ambas as abas ainda conectadas (status "Connected" verde)
