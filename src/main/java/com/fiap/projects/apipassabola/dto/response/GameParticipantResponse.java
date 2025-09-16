package com.fiap.projects.apipassabola.dto.response;

import com.fiap.projects.apipassabola.entity.GameParticipant;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for GameParticipant responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameParticipantResponse {
    
    private Long id;
    private Long gameId;
    private PlayerSummaryResponse player;
    private GameParticipant.ParticipationType participationType;
    private GameParticipant.ParticipationStatus status;
    private Integer teamSide;
    private LocalDateTime joinedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
