package com.fiap.projects.apipassabola.repository;

import com.fiap.projects.apipassabola.entity.PostLike;
import com.fiap.projects.apipassabola.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    
    /**
     * Find a specific like by post, user and user type
     */
    Optional<PostLike> findByPostIdAndUserIdAndUserType(Long postId, Long userId, UserType userType);
    
    /**
     * Check if a user has already liked a post
     */
    boolean existsByPostIdAndUserIdAndUserType(Long postId, Long userId, UserType userType);
    
    /**
     * Get all likes for a specific post
     */
    List<PostLike> findByPostIdOrderByCreatedAtDesc(Long postId);
    
    /**
     * Get likes for a post with pagination
     */
    Page<PostLike> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
    
    /**
     * Get all posts liked by a specific user
     */
    List<PostLike> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, UserType userType);
    
    /**
     * Get posts liked by a user with pagination
     */
    Page<PostLike> findByUserIdAndUserTypeOrderByCreatedAtDesc(Long userId, UserType userType, Pageable pageable);
    
    /**
     * Count likes for a specific post
     */
    long countByPostId(Long postId);
    
    /**
     * Count likes by a specific user
     */
    long countByUserIdAndUserType(Long userId, UserType userType);
    
    /**
     * Delete a specific like
     */
    void deleteByPostIdAndUserIdAndUserType(Long postId, Long userId, UserType userType);
    
    /**
     * Delete all likes for a post (useful when deleting a post)
     */
    void deleteByPostId(Long postId);
    
    /**
     * Get recent likes for a post (last N likes)
     */
    @Query("SELECT pl FROM PostLike pl WHERE pl.post.id = :postId ORDER BY pl.createdAt DESC")
    List<PostLike> findRecentLikesByPostId(@Param("postId") Long postId, Pageable pageable);
    
    /**
     * Check if multiple posts are liked by a user (for batch checking)
     */
    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.post.id IN :postIds AND pl.userId = :userId AND pl.userType = :userType")
    List<Long> findLikedPostIdsByUserAndPostIds(@Param("postIds") List<Long> postIds, @Param("userId") Long userId, @Param("userType") UserType userType);
}
