package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpectatorRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String bio;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private String profilePhotoUrl;
    
    private Long favoriteTeamId;
}
