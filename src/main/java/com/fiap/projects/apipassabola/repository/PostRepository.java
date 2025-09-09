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
    
    @Query("SELECT p FROM Post p WHERE p.player.id = :playerId ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.type = :type ORDER BY p.createdAt DESC")
    Page<Post> findByType(@Param("type") Post.PostType type, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE LOWER(p.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY p.createdAt DESC")
    Page<Post> findByContentContainingIgnoreCase(@Param("content") String content, Pageable pageable);
    
    @Query("SELECT p FROM Post p ORDER BY p.likes DESC")
    Page<Post> findMostLiked(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.player.id IN :playerIds ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerIdInOrderByCreatedAtDesc(@Param("playerIds") List<Long> playerIds, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.player.organization.id = :organizationId ORDER BY p.createdAt DESC")
    Page<Post> findByPlayerOrganizationId(@Param("organizationId") Long organizationId, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Post p WHERE p.player.id = :playerId")
    Long countByPlayerId(@Param("playerId") Long playerId);
    
    @Query("SELECT p FROM Post p WHERE p.imageUrl IS NOT NULL ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithImages(Pageable pageable);
}
