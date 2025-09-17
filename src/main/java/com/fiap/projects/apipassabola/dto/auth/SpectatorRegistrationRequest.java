package com.fiap.projects.apipassabola.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpectatorRegistrationRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String bio;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private String phone;
    
    private String profilePhotoUrl;
    
    private String bannerUrl;
    
    // ID da organização favorita (opcional)
    private Long favoriteTeamId;
}
