package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.response.PostLikeResponse;
import com.fiap.projects.apipassabola.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post-likes")
@RequiredArgsConstructor
@Slf4j
public class PostLikeController {
    
    private final PostLikeService postLikeService;
    
    /**
     * Like a post
     */
    @PostMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostLikeResponse> likePost(@PathVariable Long postId) {
        log.info("Request to like post: {}", postId);
        try {
            PostLikeResponse response = postLikeService.likePost(postId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error liking post {}: {}", postId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Unlike a post
     */
    @DeleteMapping("/posts/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikePost(@PathVariable Long postId) {
        log.info("Request to unlike post: {}", postId);
        try {
            postLikeService.unlikePost(postId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error unliking post {}: {}", postId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Check if current user has liked a post
     */
    @GetMapping("/posts/{postId}/liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Boolean>> hasLikedPost(@PathVariable Long postId) {
        boolean hasLiked = postLikeService.hasUserLikedPost(postId);
        return ResponseEntity.ok(Map.of("hasLiked", hasLiked));
    }
    
    /**
     * Get all likes for a post
     */
    @GetMapping("/posts/{postId}/likes")
    public ResponseEntity<List<PostLikeResponse>> getPostLikes(@PathVariable Long postId) {
        List<PostLikeResponse> likes = postLikeService.getPostLikes(postId);
        return ResponseEntity.ok(likes);
    }
    
    /**
     * Get recent likes for a post (for UI display)
     */
    @GetMapping("/posts/{postId}/likes/recent")
    public ResponseEntity<List<PostLikeResponse>> getRecentPostLikes(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "5") int limit) {
        List<PostLikeResponse> likes = postLikeService.getRecentPostLikes(postId, limit);
        return ResponseEntity.ok(likes);
    }
    
    /**
     * Get total likes count for a post
     */
    @GetMapping("/posts/{postId}/likes/count")
    public ResponseEntity<Map<String, Long>> getPostLikesCount(@PathVariable Long postId) {
        long count = postLikeService.getPostLikesCount(postId);
        return ResponseEntity.ok(Map.of("totalLikes", count));
    }
    
    /**
     * Get posts liked by current user
     */
    @GetMapping("/my-likes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PostLikeResponse>> getCurrentUserLikes() {
        List<PostLikeResponse> likes = postLikeService.getCurrentUserLikes();
        return ResponseEntity.ok(likes);
    }
    
    /**
     * Batch check which posts are liked by current user
     */
    @PostMapping("/check-liked")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, List<Long>>> checkLikedPosts(@RequestBody List<Long> postIds) {
        List<Long> likedPostIds = postLikeService.getLikedPostIds(postIds);
        return ResponseEntity.ok(Map.of("likedPostIds", likedPostIds));
    }
}
