package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.GameSpectator;
import com.fiap.projects.apipassabola.entity.UserType;
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
    
    // Universal watcher fields (Player or Spectator)
    private Long watcherId;
    private String watcherUsername;
    private String watcherName;
    private UserType watcherType;
    
    // Legacy fields for backward compatibility
    private Long spectatorId;
    private String spectatorUsername;
    private String spectatorName;
    
    private GameSpectator.SpectatorStatus status;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
}
