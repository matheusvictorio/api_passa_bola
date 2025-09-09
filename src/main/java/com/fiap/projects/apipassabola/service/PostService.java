package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.PostRequest;
import com.fiap.projects.apipassabola.dto.response.PostResponse;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Post;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import com.fiap.projects.apipassabola.repository.PostRepository;
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
    private final PlayerService playerService;
    
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
        Player player = playerRepository.findById(request.getPlayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Player", "id", request.getPlayerId()));
        
        Post post = new Post();
        post.setPlayer(player);
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
        
        // Only allow updating content, imageUrl and type
        post.setContent(request.getContent());
        post.setImageUrl(request.getImageUrl());
        post.setType(request.getType());
        
        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost);
    }
    
    public void delete(Long id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post", "id", id);
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
        response.setPlayer(playerService.convertToSummaryResponse(post.getPlayer()));
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
