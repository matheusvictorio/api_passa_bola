package com.fiap.projects.apipassabola.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id", "user_type"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"post"})
public class PostLike {
    
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
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    /**
     * Checks if this like belongs to the given user
     */
    public boolean belongsTo(Long userId, UserType userType) {
        return this.userId != null && userId != null && 
               this.userId.equals(userId) && 
               this.userType.equals(userType);
    }
}
