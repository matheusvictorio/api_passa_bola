package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.GameSpectator;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSpectatorResponse {
    
    private Long id;
    private Long gameId;
    private String gameName;
    private Long spectatorId;
    private String spectatorUsername;
    private String spectatorName;
    private GameSpectator.SpectatorStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
}
