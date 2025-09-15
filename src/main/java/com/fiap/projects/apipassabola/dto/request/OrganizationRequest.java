package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.UserType;
import com.fiap.projects.apipassabola.validation.ValidCnpj;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    
    private UserType userType = UserType.ORGANIZATION;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Organization name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "CNPJ is required")
    @ValidCnpj(message = "CNPJ format is invalid")
    private String cnpj;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    private String bio;
    
    private String profilePhotoUrl;
    
    private String bannerUrl;
    
    private String phone;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "State is required")
    private String state;
    
    private Integer gamesPlayed;
}
