package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type = PostType.GENERAL;
    
    @Column(nullable = false)
    private Integer likes = 0;
    
    @Column(nullable = false)
    private Integer comments = 0;
    
    @Column(nullable = false)
    private Integer shares = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum PostType {
        GENERAL, TRAINING, MATCH, ACHIEVEMENT, NEWS, ORGANIZATION_UPDATE, SPECTATOR_OPINION
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Helper methods
        public void incrementLikes() {
        this.likes++;
    }
    
    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }
    
    public void incrementShares() {
        this.shares++;
    }
    
    public void incrementComments() {
        this.comments++;
    }
    
    public void decrementComments() {
        if (this.comments > 0) {
            this.comments--;
        }
    }
    
    /**
     * Checks if the post is owned by the given user
     * @param user The user to check ownership against
     * @return true if the user owns this post, false otherwise
     */
    public boolean isOwnedBy(User user) {
        return this.author != null && user != null && this.author.getId().equals(user.getId());
    }
}
