package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PostComment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "user_username", nullable = false)
    private String userUsername;
    
    @Column(name = "user_name", nullable = false)
    private String userName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if the comment belongs to the given user
     * @param userId The user ID to check
     * @param userType The user type to check
     * @return true if the comment belongs to the user, false otherwise
     */
    public boolean belongsTo(Long userId, UserType userType) {
        return this.userId != null && this.userId.equals(userId) && 
               this.userType != null && this.userType.equals(userType);
    }
}
