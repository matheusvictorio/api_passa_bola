package com.fiap.projects.apipassabola.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.util.StringToLongDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chat messages (universal - supports all user types)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long id;
    
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long senderId;
    private String senderUsername;
    private String senderName;
    private UserType senderType;
    
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonDeserialize(using = StringToLongDeserializer.class)
    private Long recipientId;
    private String recipientUsername;
    private String recipientName;
    private UserType recipientType;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
