package com.fiap.projects.apipassabola.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fiap.projects.apipassabola.util.StringToLongDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for conversation summary (used in conversation list)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long otherUserId;
    private String otherUsername;
    private String otherName;
    private String otherProfilePhotoUrl;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long unreadCount;
}
