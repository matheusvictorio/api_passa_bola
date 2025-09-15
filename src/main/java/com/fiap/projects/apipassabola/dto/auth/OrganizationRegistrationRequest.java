package com.fiap.projects.apipassabola.dto.auth;

import com.fiap.projects.apipassabola.validation.ValidCnpj;
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
    
    @NotBlank(message = "CNPJ is required")
    @ValidCnpj(message = "CNPJ format is invalid")
    private String cnpj;
    
    private String bio;
    
    private String profilePhotoUrl;
    
    private String bannerUrl;
    
    private String phone;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
}
