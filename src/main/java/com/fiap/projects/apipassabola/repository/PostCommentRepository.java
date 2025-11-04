package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.PostComment;
import com.fiap.projects.apipassabola.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    
    /**
     * Find all comments for a specific post
     */
    Page<PostComment> findByPostId(Long postId, Pageable pageable);
    
    /**
     * Find all comments by a specific user
     */
    Page<PostComment> findByUserIdAndUserType(Long userId, UserType userType, Pageable pageable);
    
    /**
     * Count comments for a specific post
     */
    Long countByPostId(Long postId);
    
    /**
     * Count comments by a specific user
     */
    Long countByUserIdAndUserType(Long userId, UserType userType);
    
    /**
     * Find recent comments for a post (for UI display)
     */
    @Query("SELECT c FROM PostComment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<PostComment> findRecentCommentsByPostId(@Param("postId") Long postId, Pageable pageable);
    
    /**
     * Check if a user has commented on a post
     */
    boolean existsByPostIdAndUserIdAndUserType(Long postId, Long userId, UserType userType);
    
    /**
     * Delete all comments for a specific post
     */
    void deleteByPostId(Long postId);
}
