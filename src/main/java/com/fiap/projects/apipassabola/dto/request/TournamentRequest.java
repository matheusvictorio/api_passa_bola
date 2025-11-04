package com.fiap.projects.apipassabola.dto.request;

import com.fiap.projects.apipassabola.entity.GameType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TournamentRequest {
    
    @NotBlank(message = "Nome do torneio é obrigatório")
    private String name;
    
    @NotNull(message = "Tipo de jogo é obrigatório")
    private GameType gameType; // CUP ou CHAMPIONSHIP
    
    private String description;
    
    @NotBlank(message = "Local é obrigatório")
    private String venue;
    
    private LocalDateTime startDate;
    
    @Min(value = 2, message = "Número máximo de times deve ser no mínimo 2")
    private Integer maxTeams; // Será ajustado para próxima potência de 2
}
