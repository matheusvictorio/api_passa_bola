package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Post;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String authorRole;
    private String content;
    private String imageUrl;
    private Post.PostType type;
    private Integer likes;
    private Integer comments;
    private Integer shares;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
