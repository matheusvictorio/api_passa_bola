package com.fiap.projects.apipassabola.dto.auth;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRegistrationRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotBlank(message = "Organization name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    private String logoUrl;
    
    private String primaryColors;
    
    @Min(value = 1800, message = "Founded year must be after 1800")
    @Max(value = 2024, message = "Founded year cannot be in the future")
    private Integer foundedYear;
    
    private String websiteUrl;
    
    @Email(message = "Contact email should be valid")
    private String contactEmail;
    
    private String contactPhone;
}
