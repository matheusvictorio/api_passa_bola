package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRequest {
    
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
