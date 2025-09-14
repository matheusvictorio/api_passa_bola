package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryResponse {
    
    private Long id;
    private String username;
    private String name;
    private String profilePhotoUrl;
    private OrganizationSummaryResponse organization;
}
