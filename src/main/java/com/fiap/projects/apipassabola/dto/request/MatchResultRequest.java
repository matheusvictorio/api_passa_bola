package com.fiap.projects.apipassabola.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class MatchResultRequest {
    
    @NotNull(message = "Placar do time 1 é obrigatório")
    @Min(value = 0, message = "Placar não pode ser negativo")
    private Integer team1Score;
    
    @NotNull(message = "Placar do time 2 é obrigatório")
    @Min(value = 0, message = "Placar não pode ser negativo")
    private Integer team2Score;
}
