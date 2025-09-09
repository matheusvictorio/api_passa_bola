package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Player;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSummaryResponse {
    
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePhotoUrl;
    private Player.Position position;
    private OrganizationSummaryResponse organization;
}
