# 🧪 TESTE RÁPIDO - Diagnóstico WebSocket

## ⚠️ PROBLEMA: Mensagem não chega no destinatário

### 📋 CHECKLIST DE DIAGNÓSTICO

#### 1️⃣ Verificar se a mensagem chegou no backend

Olhe o terminal da aplicação. Quando você enviar a mensagem, DEVE aparecer:

```
📨 [WebSocket] Received message request: recipientId=1083690260503501183, content=Oi! Teste...
💾 [WebSocket] Message saved to DB: id=1, senderId=..., recipientId=...
📤 [WebSocket] Attempting to send to user: email=usuario@email.com, destination=/queue/messages
✅ [WebSocket] Message sent successfully from ... to ...
```

**SE NÃO APARECER NADA:**
- ❌ Formato STOMP incorreto
- ❌ Mensagem não está chegando no servidor

**SE APARECER OS LOGS:**
- ✅ Mensagem chegou no backend
- ✅ Foi salva no banco
- ❌ Problema está no roteamento WebSocket

---

#### 2️⃣ Verificar formato STOMP (CRÍTICO!)

**Seu frame SEND deve ser EXATAMENTE assim:**

```
SEND
destination:/app/chat.send
content-type:application/json

{"recipientId":1083690260503501183,"content":"Oi! Teste de mensagem em tempo real!"}
```

**REGRAS CRÍTICAS:**
1. ❌ **NÃO coloque espaço** após os dois pontos (`:`)
   - ✅ CORRETO: `destination:/app/chat.send`
   - ❌ ERRADO: `destination: /app/chat.send`

2. ✅ **Deixe UMA linha em branco** entre headers e JSON

3. ✅ **JSON em UMA linha só** (sem quebras)

4. ✅ **recipientId deve ser o userId global** (número grande)

---

#### 3️⃣ Verificar conexão do destinatário

**O destinatário DEVE:**

1. ✅ Estar conectado (`Connected to ws://localhost:8080/ws-chat`)
2. ✅ Ter feito CONNECT com token válido
3. ✅ Ter feito SUBSCRIBE em `/user/queue/messages`
4. ✅ Estar com a aba aberta e visível

**Frame SUBSCRIBE do destinatário:**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

---

#### 4️⃣ Verificar autenticação

**Ambos os usuários devem ter feito CONNECT com sucesso.**

Procure no terminal por:
```
✅ [WebSocket] Authentication successful for user: usuario1@email.com
✅ [WebSocket] Authentication successful for user: usuario2@email.com
```

Se aparecer `User: ANONYMOUS`, o token JWT não foi aceito.

---

## 🔧 TESTE PASSO A PASSO

### **ABA 1 - Destinatário (Abra PRIMEIRO)**

1. **Conectar:**
```
ws://localhost:8080/ws-chat
```

2. **CONNECT:**
```
CONNECT
Authorization:Bearer TOKEN_DO_DESTINATARIO
accept-version:1.1,1.0
heart-beat:10000,10000

```

3. **SUBSCRIBE:**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

**Aguarde aqui!** Deixe esta aba aberta e vá para a Aba 2.

---

### **ABA 2 - Remetente**

1. **Conectar:**
```
ws://localhost:8080/ws-chat
```

2. **CONNECT:**
```
CONNECT
Authorization:Bearer TOKEN_DO_REMETENTE
accept-version:1.1,1.0
heart-beat:10000,10000

```

3. **SUBSCRIBE (opcional, mas recomendado):**
```
SUBSCRIBE
id:sub-0
destination:/user/queue/messages

```

4. **SEND:**
```
SEND
destination:/app/chat.send
content-type:application/json

{"recipientId":USER_ID_DO_DESTINATARIO,"content":"Teste!"}
```

⚠️ **IMPORTANTE:** Substitua `USER_ID_DO_DESTINATARIO` pelo userId global do usuário da Aba 1!

---

## 📊 O QUE DEVE ACONTECER

### **No Terminal (Backend):**
```
📨 [WebSocket] Received message request: recipientId=1083690260503501183, content=Teste!
💾 [WebSocket] Message saved to DB: id=1, senderId=1578941265158776642, recipientId=1083690260503501183
📤 [WebSocket] Attempting to send to user: email=destinatario@email.com, destination=/queue/messages
📦 [WebSocket] Message payload: ChatMessageResponse(id=1, senderId=1578941265158776642, ...)
✅ [WebSocket] convertAndSendToUser() completed for email: destinatario@email.com
✅ [WebSocket] Message sent successfully from 1578941265158776642 to 1083690260503501183
```

### **Na Aba 1 (Destinatário):**
Deve aparecer a mensagem instantaneamente:
```json
{
  "id": 1,
  "senderId": 1578941265158776642,
  "senderUsername": "remetente",
  "senderName": "Nome Remetente",
  "senderType": "PLAYER",
  "recipientId": 1083690260503501183,
  "recipientUsername": "destinatario",
  "recipientName": "Nome Destinatário",
  "recipientType": "ORGANIZATION",
  "content": "Teste!",
  "isRead": false,
  "createdAt": "2025-10-22T10:15:00"
}
```

---

## 🐛 PROBLEMAS COMUNS

### ❌ Nenhum log aparece no terminal
**CAUSA:** Formato STOMP incorreto ou mensagem não está sendo enviada

**SOLUÇÃO:**
1. Copie e cole o frame SEND EXATAMENTE como mostrado acima
2. Verifique se não há espaços extras
3. Verifique se o JSON está em uma linha só
4. Clique em "Send" no Postman

---

### ❌ Logs aparecem mas mensagem não chega
**CAUSA:** Destinatário não está conectado ou não fez SUBSCRIBE

**SOLUÇÃO:**
1. Verifique se a Aba 1 está com "Connected" verde
2. Verifique se fez SUBSCRIBE em `/user/queue/messages`
3. Tente desconectar e reconectar a Aba 1

---

### ❌ Erro "User not found" ou "Recipient not found"
**CAUSA:** userId do destinatário está errado

**SOLUÇÃO:**
1. Faça login com o destinatário
2. Copie o `userId` da resposta (número grande)
3. Use este userId no campo `recipientId`

---

## 🎯 PRÓXIMO PASSO

**Faça o teste acima e me diga:**

1. ✅ Você viu os logs no terminal quando enviou a mensagem?
2. ✅ Qual foi o último log que apareceu?
3. ✅ A mensagem chegou na Aba 1 (destinatário)?

Com essas informações, posso identificar exatamente onde está o problema! 🔍
