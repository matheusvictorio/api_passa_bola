package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.Division;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DivisionStatsResponse {
    
    private Division division;
    private String divisionName;
    private Integer minPoints;
    private Integer maxPoints;
    private Long totalPlayers;
    private Long totalTeams;
}
