package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinishGameRequest {
    
    @NotNull(message = "Home goals is required")
    @Min(value = 0, message = "Home goals must be >= 0")
    private Integer homeGoals;
    
    @NotNull(message = "Away goals is required")
    @Min(value = 0, message = "Away goals must be >= 0")
    private Integer awayGoals;
    
    @Valid
    private List<GoalRequest> goals; // Lista de gols com as jogadoras que marcaram
    
    private String notes; // Observações sobre o jogo
}
