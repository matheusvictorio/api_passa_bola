package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.PostCommentRequest;
import com.fiap.projects.apipassabola.dto.response.PostCommentResponse;
import com.fiap.projects.apipassabola.service.PostCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-comments")
@RequiredArgsConstructor
public class PostCommentController {
    
    private final PostCommentService commentService;
    
    /**
     * Create a comment on a post
     * POST /api/post-comments/post/{postId}
     */
    @PostMapping("/post/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostCommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody PostCommentRequest request) {
        PostCommentResponse response = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update a comment
     * PUT /api/post-comments/{commentId}
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostCommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody PostCommentRequest request) {
        PostCommentResponse response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a comment
     * DELETE /api/post-comments/{commentId}
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get all comments for a post with pagination
     * GET /api/post-comments/post/{postId}
     */
    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<PostCommentResponse>> getCommentsByPost(
            @PathVariable Long postId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostCommentResponse> comments = commentService.getCommentsByPost(postId, pageable);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Get recent comments for a post (for UI display)
     * GET /api/post-comments/post/{postId}/recent?limit=5
     */
    @GetMapping("/post/{postId}/recent")
    public ResponseEntity<List<PostCommentResponse>> getRecentComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "5") int limit) {
        List<PostCommentResponse> comments = commentService.getRecentCommentsByPost(postId, limit);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Get comments by current user
     * GET /api/post-comments/my-comments
     */
    @GetMapping("/my-comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PostCommentResponse>> getMyComments(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostCommentResponse> comments = commentService.getMyComments(pageable);
        return ResponseEntity.ok(comments);
    }
    
    /**
     * Get comment count for a post
     * GET /api/post-comments/post/{postId}/count
     */
    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Long> getCommentCount(@PathVariable Long postId) {
        Long count = commentService.getCommentCount(postId);
        return ResponseEntity.ok(count);
    }
    
    /**
     * Check if current user has commented on a post
     * GET /api/post-comments/post/{postId}/has-commented
     */
    @GetMapping("/post/{postId}/has-commented")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> hasUserCommented(@PathVariable Long postId) {
        Boolean hasCommented = commentService.hasUserCommented(postId);
        return ResponseEntity.ok(hasCommented);
    }
}
