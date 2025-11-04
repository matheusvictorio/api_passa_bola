package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentResponse {
    
    private Long id;
    private Long postId;
    private Long userId;
    private String userUsername;
    private String userName;
    private UserType userType;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOwnedByCurrentUser; // Indicates if current user can edit/delete
}
