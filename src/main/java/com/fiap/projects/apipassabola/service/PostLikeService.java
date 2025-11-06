package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.PostLikeResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {
    
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserContextService userContextService;
    private final NotificationService notificationService;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    
    /**
     * Like a post
     */
    @Transactional
    public PostLikeResponse likePost(Long postId) {
        log.debug("Attempting to like post with ID: {}", postId);
        
        // Get current user info
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        Long userId = currentUser.getUserId();
        UserType userType = currentUser.getUserType();
        
        // Check if post exists
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        // Check if user already liked this post
        if (postLikeRepository.existsByPostIdAndUserIdAndUserType(postId, userId, userType)) {
            throw new RuntimeException("User has already liked this post");
        }
        
        // Get user details based on type
        String username = getCurrentUserUsername(userId, userType);
        String name = getCurrentUserName(userId, userType);
        
        // Create new like
        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setUserId(userId);
        postLike.setUserUsername(username);
        postLike.setUserName(name);
        postLike.setUserType(userType);
        
        PostLike savedLike = postLikeRepository.save(postLike);
        
        // Update post likes count
        post.incrementLikes();
        postRepository.save(post);
        
        log.info("User {} ({}) liked post {}", username, userType, postId);
        
        // Enviar notificação ao autor do post (se não for o próprio usuário curtindo)
        if (!post.getAuthorId().equals(userId) || !post.getAuthorType().equals(userType)) {
            // Buscar userId global do autor do post
            Long authorGlobalUserId = getGlobalUserId(post.getAuthorId(), post.getAuthorType());
            
            // Buscar userId global do usuário que curtiu
            UserContextService.UserIdAndType currentGlobalUser = userContextService.getCurrentGlobalUserIdAndType();
            
            log.debug("Enviando notificação de like: authorGlobalUserId={}, likerGlobalUserId={}", 
                    authorGlobalUserId, currentGlobalUser.getUserId());
            
            notificationService.notifyPostLiked(
                    authorGlobalUserId,           // ✅ userId global do autor
                    post.getAuthorType(),
                    currentGlobalUser.getUserId(), // ✅ userId global de quem curtiu
                    userType,
                    username,
                    name,
                    postId
            );
        }
        
        return convertToResponse(savedLike);
    }
    
    /**
     * Unlike a post
     */
    @Transactional
    public void unlikePost(Long postId) {
        log.debug("Attempting to unlike post with ID: {}", postId);
        
        // Get current user info
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        Long userId = currentUser.getUserId();
        UserType userType = currentUser.getUserType();
        
        // Check if post exists
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        
        // Check if user has liked this post
        Optional<PostLike> existingLike = postLikeRepository.findByPostIdAndUserIdAndUserType(postId, userId, userType);
        if (existingLike.isEmpty()) {
            throw new RuntimeException("User has not liked this post");
        }
        
        // Remove the like
        postLikeRepository.delete(existingLike.get());
        
        // Update post likes count
        post.decrementLikes();
        postRepository.save(post);
        
        log.info("User {} ({}) unliked post {}", getCurrentUserUsername(userId, userType), userType, postId);
    }
    
    /**
     * Check if current user has liked a post
     */
    public boolean hasUserLikedPost(Long postId) {
        try {
            UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
            return postLikeRepository.existsByPostIdAndUserIdAndUserType(
                postId, currentUser.getUserId(), currentUser.getUserType());
        } catch (Exception e) {
            // User not authenticated or other error
            return false;
        }
    }
    
    /**
     * Check if a specific user has liked a post
     */
    public boolean hasUserLikedPost(Long postId, Long userId, UserType userType) {
        return postLikeRepository.existsByPostIdAndUserIdAndUserType(postId, userId, userType);
    }
    
    /**
     * Get all likes for a post
     */
    public List<PostLikeResponse> getPostLikes(Long postId) {
        List<PostLike> likes = postLikeRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return likes.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get recent likes for a post (for UI display)
     */
    public List<PostLikeResponse> getRecentPostLikes(Long postId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<PostLike> likes = postLikeRepository.findRecentLikesByPostId(postId, pageable);
        return likes.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Get total likes count for a post
     */
    public long getPostLikesCount(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }
    
    /**
     * Get posts liked by current user
     */
    public List<PostLikeResponse> getCurrentUserLikes() {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        List<PostLike> likes = postLikeRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(
            currentUser.getUserId(), currentUser.getUserType());
        return likes.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Check which posts from a list are liked by current user (for batch checking)
     */
    public List<Long> getLikedPostIds(List<Long> postIds) {
        try {
            UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
            return postLikeRepository.findLikedPostIdsByUserAndPostIds(
                postIds, currentUser.getUserId(), currentUser.getUserType());
        } catch (Exception e) {
            // User not authenticated
            return List.of();
        }
    }
    
    /**
     * Convert PostLike entity to response DTO
     */
    private PostLikeResponse convertToResponse(PostLike postLike) {
        return new PostLikeResponse(
            postLike.getId(),
            String.valueOf(postLike.getUserId()),  // Convert Long to String
            postLike.getUserUsername(),
            postLike.getUserName(),
            postLike.getUserType(),
            postLike.getCreatedAt()
        );
    }
    
    /**
     * Get username based on user type and ID
     */
    private String getCurrentUserUsername(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return userContextService.getCurrentPlayer().getUsername();
            case ORGANIZATION:
                return userContextService.getCurrentOrganization().getUsername();
            case SPECTATOR:
                return userContextService.getCurrentSpectator().getUsername();
            default:
                throw new RuntimeException("Unknown user type: " + userType);
        }
    }
    
    /**
     * Get user name based on user type and ID
     */
    private String getCurrentUserName(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return userContextService.getCurrentPlayer().getName();
            case ORGANIZATION:
                return userContextService.getCurrentOrganization().getName();
            case SPECTATOR:
                return userContextService.getCurrentSpectator().getName();
            default:
                throw new RuntimeException("Unknown user type: " + userType);
        }
    }
    
    /**
     * Get global userId from entity ID and type
     * CRITICAL: This ensures notifications use the correct global userId
     */
    private Long getGlobalUserId(Long entityId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.findById(entityId)
                        .map(Player::getUserId)
                        .orElseThrow(() -> new RuntimeException("Player not found: " + entityId));
            case ORGANIZATION:
                return organizationRepository.findById(entityId)
                        .map(Organization::getUserId)
                        .orElseThrow(() -> new RuntimeException("Organization not found: " + entityId));
            case SPECTATOR:
                return spectatorRepository.findById(entityId)
                        .map(Spectator::getUserId)
                        .orElseThrow(() -> new RuntimeException("Spectator not found: " + entityId));
            default:
                throw new RuntimeException("Unknown user type: " + userType);
        }
    }
    
}
