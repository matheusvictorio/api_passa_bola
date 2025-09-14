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
    private Long userId;
    private String username;
    private String email;
    private User.Role role;
    private Long profileId; // ID of Player, Organization, or Spectator
    
}
