package com.fiap.projects.apipassabola.dto;

import com.fiap.projects.apipassabola.entity.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowRequest {
    
    @NotNull(message = "Target user ID is required")
    private String targetUserId;
    
    @NotNull(message = "Target user type is required")
    private UserType targetUserType;
}
