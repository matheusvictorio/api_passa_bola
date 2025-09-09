package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.Player;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRequest {
    
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    private String bio;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private Player.Position position;
    
    private String profilePhotoUrl;
    
    @Min(value = 1, message = "Jersey number must be positive")
    @Max(value = 99, message = "Jersey number must be less than 100")
    private Integer jerseyNumber;
    
    private Long organizationId;
}
