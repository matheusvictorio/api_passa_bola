package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {
    
    private Long id;
    private String content;
    private String imageUrl;
    private String authorUsername;
    private String authorName;
    private String postType;
    private LocalDateTime createdAt;
}
