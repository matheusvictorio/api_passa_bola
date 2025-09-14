package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.Post;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {
    
    // Optional: Only used for backward compatibility or when creating posts for specific players
    private Long playerId;
    
    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content cannot exceed 2000 characters")
    private String content;
    
    private String imageUrl;
    
    private Post.PostType type = Post.PostType.GENERAL;
}
