package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorId(@Param("authorId") Long authorId, Pageable pageable);
    
    // Backward compatibility method for existing player-based queries
    @Query("SELECT p FROM Post p WHERE p.authorId = :playerId AND p.authorType = 'PLAYER' ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.type = :type ORDER BY p.createdAt DESC")
    Page<Post> findByType(@Param("type") Post.PostType type, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY p.createdAt DESC")
    Page<Post> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likes DESC")
    Page<Post> findMostLiked(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.authorId IN :authorIds ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorIdInOrderByCreatedAtDesc(@Param("authorIds") List<Long> authorIds, Pageable pageable);
    
    // Backward compatibility method
    @Query("SELECT p FROM Post p WHERE p.authorId IN :playerIds AND p.authorType = 'PLAYER' ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerIdInOrderByCreatedAtDesc(@Param("playerIds") List<Long> playerIds, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.authorType = 'ORGANIZATION' AND p.authorId = :organizationId ORDER BY p.createdAt DESC")
    Page<Post> findByOrganizationAuthor(@Param("organizationId") Long organizationId, Pageable pageable);
    
    // For posts by players of a specific organization
    @Query("SELECT p FROM Post p JOIN Player pl ON p.authorId = pl.id WHERE pl.organization.id = :organizationId AND p.authorType = 'PLAYER' ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.authorId = :authorId")
    Long countByAuthorId(@Param("authorId") Long authorId);
    
    // Backward compatibility method
    @Query("SELECT COUNT(p) FROM Post p WHERE p.authorId = :playerId AND p.authorType = 'PLAYER'")
    Long countByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT p FROM Post p WHERE p.imageUrl IS NOT NULL ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithImages(Pageable pageable);
    
    // New methods for multi-user support
    @Query("SELECT p FROM Post p WHERE p.authorType = :role ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorRole(@Param("role") String role, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.authorUsername = :username ORDER BY p.createdAt DESC")
    Page<Post> findByAuthorUsername(@Param("username") String username, Pageable pageable);
}
