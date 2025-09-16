package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Post;
import com.fiap.projects.apipassabola.entity.UserType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {
    
    private Long id;
    private Long authorId;
    private String authorUsername;
    private String authorName;
    private UserType authorType;
    private String content;
    private String imageUrl;
    private Post.PostType type;
    private Integer likes;
    private Integer comments;
    private Integer shares;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Like information for frontend
    private Boolean isLikedByCurrentUser;
    private List<PostLikeResponse> recentLikes; // Last few users who liked (for UI display)
    private Long totalLikes; // Total count of likes
}
