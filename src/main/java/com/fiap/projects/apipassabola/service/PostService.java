package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.PostRequest;
import com.fiap.projects.apipassabola.dto.response.PostResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    
    private final PostRepository postRepository;
    private final PlayerRepository playerRepository;
    private final OrganizationRepository organizationRepository;
    private final SpectatorRepository spectatorRepository;
    private final UserContextService userContextService;
    
    public Page<PostResponse> findAll(Pageable pageable) {
        return postRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public PostResponse findById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return convertToResponse(post);
    }
    
    public Page<PostResponse> findByPlayer(Long playerId, Pageable pageable) {
        return postRepository.findByPlayerId(playerId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findByCurrentUser(Pageable pageable) {
        Long currentUserId = userContextService.getCurrentUserId();
        return postRepository.findByAuthorId(currentUserId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findByRole(String role, Pageable pageable) {
        return postRepository.findByAuthorRole(role.toUpperCase(), pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findByType(Post.PostType type, Pageable pageable) {
        return postRepository.findByType(type, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findByContentContaining(String content, Pageable pageable) {
        return postRepository.findByContentContainingIgnoreCase(content, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findMostLiked(Pageable pageable) {
        return postRepository.findMostLiked(pageable)
                .map(this::convertToResponse);
    }
    
    public Page<PostResponse> findPostsWithImages(Pageable pageable) {
        return postRepository.findPostsWithImages(pageable)
                .map(this::convertToResponse);
    }
    
    public PostResponse create(PostRequest request) {
        Long currentUserId = userContextService.getCurrentUserId();
        UserType currentUserType = userContextService.getCurrentUserType();
        String currentUsername = userContextService.getCurrentUsername();
        
        // Auto-detect player if the current user is a PLAYER and no specific playerId is provided
        if (request.getPlayerId() != null) {
            // Validate that the specified player exists (for backward compatibility)
            playerRepository.findById(request.getPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Player", "id", request.getPlayerId()));
        }
        
        Post post = new Post();
        post.setAuthorId(currentUserId);
        post.setAuthorUsername(currentUsername);
        post.setAuthorType(currentUserType);
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setType(request.getType());
        post.setLikes(0);
        post.setComments(0);
        post.setShares(0);
        
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public PostResponse update(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        Long currentUserId = userContextService.getCurrentUserId();
        
        // Verify ownership - only the author can update their post
        if (!post.isOwnedBy(currentUserId)) {
            throw new BusinessException("You can only update your own posts");
        }
        
        // Only allow updating content, imageUrl and type
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setType(request.getType());
        
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public void delete(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        Long currentUserId = userContextService.getCurrentUserId();
        
        // Verify ownership - only the author can delete their post
        if (!post.isOwnedBy(currentUserId)) {
            throw new BusinessException("You can only delete your own posts");
        }
        
        postRepository.deleteById(id);
    }
    
    public PostResponse likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        post.incrementLikes();
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public PostResponse unlikePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        if (post.getLikes() <= 0) {
            throw new BusinessException("Cannot unlike a post with no likes");
        }
        
        post.decrementLikes();
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public PostResponse commentPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        post.incrementComments();
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public PostResponse sharePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        
        post.incrementShares();
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    private PostResponse convertToResponse(Post post) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setAuthorId(post.getAuthorId());
        response.setAuthorUsername(post.getAuthorUsername());
        response.setAuthorRole(post.getAuthorType().name());
        response.setContent(post.getContent());
        response.setImageUrl(post.getImageUrl());
        response.setType(post.getType());
        response.setLikes(post.getLikes());
        response.setComments(post.getComments());
        response.setShares(post.getShares());
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        
        return response;
    }
}
