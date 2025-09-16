package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.PostRequest;
import com.fiap.projects.apipassabola.dto.response.PostResponse;
import com.fiap.projects.apipassabola.dto.response.PostLikeResponse;
import com.fiap.projects.apipassabola.entity.Post;
import com.fiap.projects.apipassabola.service.PostService;
import com.fiap.projects.apipassabola.service.PostLikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {
    
    private final PostService postService;
    private final PostLikeService postLikeService;
    
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findAll(pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse post = postService.findById(id);
        return ResponseEntity.ok(post);
    }
    
    @GetMapping("/player/{playerId}")
    public ResponseEntity<Page<PostResponse>> getPostsByPlayer(
            @PathVariable Long playerId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByPlayer(playerId, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<PostResponse>> getPostsByAuthor(
            @PathVariable Long authorId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByAuthor(authorId, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/my-posts")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByCurrentUser(pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/role/{role}")
    public ResponseEntity<Page<PostResponse>> getPostsByRole(
            @PathVariable String role,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByRole(role, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<Page<PostResponse>> getPostsByType(
            @PathVariable Post.PostType type,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByType(type, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPostsByContent(
            @RequestParam String content,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findByContentContaining(content, pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/most-liked")
    public ResponseEntity<Page<PostResponse>> getMostLikedPosts(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findMostLiked(pageable);
        return ResponseEntity.ok(posts);
    }
    
    @GetMapping("/with-images")
    public ResponseEntity<Page<PostResponse>> getPostsWithImages(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PostResponse> posts = postService.findPostsWithImages(pageable);
        return ResponseEntity.ok(posts);
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse createdPost = postService.create(request);
        return ResponseEntity.ok(createdPost);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        PostResponse updatedPost = postService.update(id, request);
        return ResponseEntity.ok(updatedPost);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostLikeResponse> likePost(@PathVariable Long id) {
        PostLikeResponse likeResponse = postLikeService.likePost(id);
        return ResponseEntity.ok(likeResponse);
    }
    
    @DeleteMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikePost(@PathVariable Long id) {
        postLikeService.unlikePost(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> hasLikedPost(@PathVariable Long id) {
        boolean hasLiked = postLikeService.hasUserLikedPost(id);
        return ResponseEntity.ok(Map.of("hasLiked", hasLiked));
    }
    
    @GetMapping("/{id}/likes")
    public ResponseEntity<List<PostLikeResponse>> getPostLikes(@PathVariable Long id) {
        List<PostLikeResponse> likes = postLikeService.getPostLikes(id);
        return ResponseEntity.ok(likes);
    }
    
    @GetMapping("/{id}/likes/count")
    public ResponseEntity<Map<String, Long>> getPostLikesCount(@PathVariable Long id) {
        long count = postLikeService.getPostLikesCount(id);
        return ResponseEntity.ok(Map.of("totalLikes", count));
    }
    
    @PostMapping("/{id}/comment")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR') or hasRole('ORGANIZATION')")
    public ResponseEntity<PostResponse> commentPost(@PathVariable Long id) {
        PostResponse commentedPost = postService.commentPost(id);
        return ResponseEntity.ok(commentedPost);
    }
    
    @PostMapping("/{id}/share")
    @PreAuthorize("hasRole('PLAYER') or hasRole('SPECTATOR') or hasRole('ORGANIZATION')")
    public ResponseEntity<PostResponse> sharePost(@PathVariable Long id) {
        PostResponse sharedPost = postService.sharePost(id);
        return ResponseEntity.ok(sharedPost);
    }
}
