package com.fiap.projects.apipassabola.dto.auth;

import com.fiap.projects.apipassabola.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private User.Role role;
    private Long profileId; // ID of Player, Organization, or Spectator
    
    public AuthResponse(String token, Long userId, String username, String email, User.Role role, Long profileId) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.profileId = profileId;
    }
}
