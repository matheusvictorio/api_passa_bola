package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.PostCommentRequest;
import com.fiap.projects.apipassabola.dto.response.PostCommentResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentService {
    
    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    
    /**
     * Create a comment on a post
     */
    public PostCommentResponse createComment(Long postId, PostCommentRequest request) {
        // Get current user info
        Long currentUserId = userContextService.getCurrentUserId();
        UserType currentUserType = userContextService.getCurrentUserType();
        String currentUsername = userContextService.getCurrentRealUsername();
        String currentUserName = getCurrentUserName(currentUserId, currentUserType);
        
        // Validate post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        
        // Create comment
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUserId(currentUserId);
        comment.setUserUsername(currentUsername);
        comment.setUserName(currentUserName);
        comment.setUserType(currentUserType);
        comment.setContent(request.getContent());
        
        PostComment savedComment = commentRepository.save(comment);
        
        // Increment post comment count
        post.incrementComments();
        postRepository.save(post);
        
        return convertToResponse(savedComment);
    }
    
    /**
     * Update a comment
     */
    public PostCommentResponse updateComment(Long commentId, PostCommentRequest request) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // Verify ownership
        Long currentUserId = userContextService.getCurrentUserId();
        UserType currentUserType = userContextService.getCurrentUserType();
        
        if (!comment.belongsTo(currentUserId, currentUserType)) {
            throw new BusinessException("You can only update your own comments");
        }
        
        comment.setContent(request.getContent());
        PostComment savedComment = commentRepository.save(comment);
        
        return convertToResponse(savedComment);
    }
    
    /**
     * Delete a comment
     */
    public void deleteComment(Long commentId) {
        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));
        
        // Verify ownership
        Long currentUserId = userContextService.getCurrentUserId();
        UserType currentUserType = userContextService.getCurrentUserType();
        
        if (!comment.belongsTo(currentUserId, currentUserType)) {
            throw new BusinessException("You can only delete your own comments");
        }
        
        // Decrement post comment count
        Post post = comment.getPost();
        post.decrementComments();
        postRepository.save(post);
        
        commentRepository.deleteById(commentId);
    }
    
    /**
     * Get all comments for a post with pagination
     */
    @Transactional(readOnly = true)
    public Page<PostCommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        // Validate post exists
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        
        return commentRepository.findByPostId(postId, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Get recent comments for a post (for UI display)
     */
    @Transactional(readOnly = true)
    public List<PostCommentResponse> getRecentCommentsByPost(Long postId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return commentRepository.findRecentCommentsByPostId(postId, pageable)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get comments by current user
     */
    @Transactional(readOnly = true)
    public Page<PostCommentResponse> getMyComments(Pageable pageable) {
        Long currentUserId = userContextService.getCurrentUserId();
        UserType currentUserType = userContextService.getCurrentUserType();
        
        return commentRepository.findByUserIdAndUserType(currentUserId, currentUserType, pageable)
                .map(this::convertToResponse);
    }
    
    /**
     * Get comment count for a post
     */
    @Transactional(readOnly = true)
    public Long getCommentCount(Long postId) {
        return commentRepository.countByPostId(postId);
    }
    
    /**
     * Check if current user has commented on a post
     */
    @Transactional(readOnly = true)
    public boolean hasUserCommented(Long postId) {
        try {
            Long currentUserId = userContextService.getCurrentUserId();
            UserType currentUserType = userContextService.getCurrentUserType();
            return commentRepository.existsByPostIdAndUserIdAndUserType(postId, currentUserId, currentUserType);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Convert PostComment entity to response DTO
     */
    private PostCommentResponse convertToResponse(PostComment comment) {
        PostCommentResponse response = new PostCommentResponse();
        response.setId(comment.getId());
        response.setPostId(comment.getPost().getId());
        response.setUserId(comment.getUserId());
        response.setUserUsername(comment.getUserUsername());
        response.setUserName(comment.getUserName());
        response.setUserType(comment.getUserType());
        response.setContent(comment.getContent());
        response.setCreatedAt(comment.getCreatedAt());
        response.setUpdatedAt(comment.getUpdatedAt());
        
        // Check if current user owns this comment
        try {
            Long currentUserId = userContextService.getCurrentUserId();
            UserType currentUserType = userContextService.getCurrentUserType();
            response.setIsOwnedByCurrentUser(comment.belongsTo(currentUserId, currentUserType));
        } catch (Exception e) {
            response.setIsOwnedByCurrentUser(false);
        }
        
        return response;
    }
    
    /**
     * Get user's real name based on ID and type
     */
    private String getCurrentUserName(Long userId, UserType userType) {
        switch (userType) {
            case PLAYER:
                return playerRepository.findById(userId)
                        .map(Player::getName)
                        .orElse("Unknown Player");
            case ORGANIZATION:
                return organizationRepository.findById(userId)
                        .map(Organization::getName)
                        .orElse("Unknown Organization");
            case SPECTATOR:
                return spectatorRepository.findById(userId)
                        .map(Spectator::getName)
                        .orElse("Unknown Spectator");
            default:
                return "Unknown User";
        }
    }
}
