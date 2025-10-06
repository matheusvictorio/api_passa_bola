package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.FollowRequest;
import com.fiap.projects.apipassabola.dto.FollowResponse;
import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.service.FollowService;
import com.fiap.projects.apipassabola.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class FollowController {
    
    private final FollowService followService;
    private final UserContextService userContextService;
    
    /**
     * Seguir qualquer usuário (Player, Organization ou Spectator)
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> followUser(@Valid @RequestBody FollowRequest request) {
        String result = followService.followUser(request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Deixar de seguir qualquer usuário
     */
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> unfollowUser(@Valid @RequestBody FollowRequest request) {
        String result = followService.unfollowUser(request);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Verificar se está seguindo um usuário específico
     */
    @PostMapping("/check")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> isFollowing(@Valid @RequestBody FollowRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Converter String userId para Long e buscar entity ID
        Long targetUserId = Long.parseLong(request.getTargetUserId());
        Long targetEntityId = followService.getEntityIdByUserId(targetUserId, request.getTargetUserType());
        
        boolean isFollowing = followService.isFollowing(
            currentUser.getUserId(), 
            currentUser.getUserType(), 
            targetEntityId, 
            request.getTargetUserType()
        );
        return ResponseEntity.ok(isFollowing);
    }
    
    /**
     * Listar seguidores de qualquer usuário (público)
     * @param userId - O userId GLOBAL do usuário
     */
    @GetMapping("/followers/{userId}/{userType}")
    public ResponseEntity<Page<FollowResponse>> getFollowers(
            @PathVariable String userId,
            @PathVariable UserType userType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        // Converter String userId para Long e depois para entity ID
        Long userIdLong = Long.parseLong(userId);
        Long entityId = followService.getEntityIdByUserId(userIdLong, userType);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FollowResponse> followers = followService.getFollowers(entityId, userType, pageable);
        return ResponseEntity.ok(followers);
    }
    
    /**
     * Listar quem um usuário está seguindo (público)
     * @param userId - O userId GLOBAL do usuário
     */
    @GetMapping("/following/{userId}/{userType}")
    public ResponseEntity<Page<FollowResponse>> getFollowing(
            @PathVariable String userId,
            @PathVariable UserType userType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        // Converter String userId para Long e depois para entity ID
        Long userIdLong = Long.parseLong(userId);
        Long entityId = followService.getEntityIdByUserId(userIdLong, userType);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FollowResponse> following = followService.getFollowing(entityId, userType, pageable);
        return ResponseEntity.ok(following);
    }
    
    /**
     * Listar meus seguidores (usuário autenticado)
     */
    @GetMapping("/my-followers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowResponse>> getMyFollowers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FollowResponse> followers = followService.getFollowers(currentUser.getUserId(), currentUser.getUserType(), pageable);
        return ResponseEntity.ok(followers);
    }
    
    /**
     * Listar quem estou seguindo (usuário autenticado)
     */
    @GetMapping("/my-following")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FollowResponse>> getMyFollowing(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FollowResponse> following = followService.getFollowing(currentUser.getUserId(), currentUser.getUserType(), pageable);
        return ResponseEntity.ok(following);
    }
}
