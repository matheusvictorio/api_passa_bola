package com.fiap.projects.apipassabola.dto;

import com.fiap.projects.apipassabola.entity.UserType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowResponse {
    
    private Long id;
    private String username;
    private String name;
    private String email;
    private UserType userType;
    private String bio;
    private String profilePhotoUrl;
    private String bannerUrl;
    private String phone;
    private LocalDateTime createdAt;
    
    // Campos específicos para Player
    private String birthDate;
    private Long organizationId;
    private String pastOrganization;
    
    // Campos específicos para Organization
    private String cnpj;
    private String city;
    private String state;
    
    // Campos específicos para Spectator
    private Long favoriteTeamId;
}
