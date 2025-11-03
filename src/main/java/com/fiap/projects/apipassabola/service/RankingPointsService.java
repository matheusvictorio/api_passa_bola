package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.GameParticipant;
import com.fiap.projects.apipassabola.entity.GameType;
import com.fiap.projects.apipassabola.entity.Goal;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.repository.GameParticipantRepository;
import com.fiap.projects.apipassabola.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service responsável por calcular e distribuir pontos de ranking após jogos
 * Apenas jogos de CHAMPIONSHIP e CUP contam para o ranking
 * 
 * Sistema de Pontuação:
 * - Vitória: 3 pontos base + 1 ponto por gol marcado
 * - Empate: 1 ponto base + 1 ponto por gol marcado
 * - Derrota: 0 pontos base + 1 ponto por gol marcado
 * 
 * Exemplos:
 * - Ganhou e fez 3 gols: 3 + 3 = 6 pontos
 * - Empatou e fez 2 gols: 1 + 2 = 3 pontos
 * - Perdeu e fez 1 gol: 0 + 1 = 1 ponto
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingPointsService {
    
    private final PlayerRankingService playerRankingService;
    private final TeamRankingService teamRankingService;
    private final GameParticipantRepository gameParticipantRepository;
    private final GoalRepository goalRepository;
    
    /**
     * Distribui pontos após finalização de um jogo
     * Apenas CHAMPIONSHIP e CUP contam para ranking
     */
    @Transactional
    public void distributePointsAfterGame(Game game) {
        // Apenas jogos de campeonato e copa contam para ranking
        if (game.getGameType() != GameType.CHAMPIONSHIP && game.getGameType() != GameType.CUP) {
            log.info("Game {} is type {}, skipping ranking points distribution", 
                    game.getId(), game.getGameType());
            return;
        }
        
        // Verifica se o jogo está finalizado
        if (game.getStatus() != Game.GameStatus.FINISHED) {
            log.warn("Game {} is not finished, cannot distribute points", game.getId());
            return;
        }
        
        log.info("Distributing ranking points for game {} (type: {})", game.getId(), game.getGameType());
        
        // Determina o resultado
        Integer winningTeamSide = game.getWinningTeamSide();
        boolean isDraw = game.isDraw();
        
        if (isDraw) {
            distributeDrawPoints(game);
        } else if (winningTeamSide != null) {
            distributeWinLossPoints(game, winningTeamSide);
        } else {
            log.warn("Could not determine winner for game {}", game.getId());
        }
    }
    
    /**
     * Distribui pontos em caso de empate (1 ponto base + 1 por gol)
     */
    private void distributeDrawPoints(Game game) {
        log.info("Game {} ended in a draw, distributing points to all participants", game.getId());
        
        List<GameParticipant> participants = gameParticipantRepository.findByGameIdAndStatus(
                game.getId(), GameParticipant.ParticipationStatus.CONFIRMED);
        
        // Busca todos os gols do jogo (exceto gols contra)
        List<Goal> goals = goalRepository.findByGameIdAndIsOwnGoal(game.getId(), false);
        
        for (GameParticipant participant : participants) {
            // Apenas jogadoras ganham pontos (espectadores não)
            if (participant.isPlayer() && participant.getPlayer() != null) {
                Long playerId = participant.getPlayer().getId();
                
                // Conta quantos gols a jogadora fez
                long goalsScored = goals.stream()
                        .filter(g -> g.getPlayer().getId().equals(playerId))
                        .count();
                
                // Empate: 1 ponto base + 1 por gol
                int totalPoints = 1 + (int) goalsScored;
                
                playerRankingService.addDraw(playerId);
                
                // Adiciona pontos extras por gols
                if (goalsScored > 0) {
                    playerRankingService.addBonusPoints(playerId, (int) goalsScored);
                    log.debug("Added draw (1 point) + {} goal bonus to player {} = {} total points", 
                            goalsScored, playerId, totalPoints);
                } else {
                    log.debug("Added draw (1 point) to player {}", playerId);
                }
                
                // Se participou com time, adiciona pontos para o time também (apenas pontos base, sem bônus de gols)
                if (participant.isTeamParticipation()) {
                    Player player = participant.getPlayer();
                    if (player.getTeams() != null && !player.getTeams().isEmpty()) {
                        for (Team team : player.getTeams()) {
                            teamRankingService.addDraw(team.getId());
                            log.debug("Added draw to team {}", team.getId());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Distribui pontos em caso de vitória/derrota
     * Vitória: 3 pontos base + 1 por gol
     * Derrota: 0 pontos base + 1 por gol
     */
    private void distributeWinLossPoints(Game game, Integer winningTeamSide) {
        log.info("Game {} won by team side {}, distributing points", game.getId(), winningTeamSide);
        
        List<GameParticipant> participants = gameParticipantRepository.findByGameIdAndStatus(
                game.getId(), GameParticipant.ParticipationStatus.CONFIRMED);
        
        // Busca todos os gols do jogo (exceto gols contra)
        List<Goal> goals = goalRepository.findByGameIdAndIsOwnGoal(game.getId(), false);
        
        for (GameParticipant participant : participants) {
            // Apenas jogadoras ganham pontos (espectadores não)
            if (participant.isPlayer() && participant.getPlayer() != null) {
                Long playerId = participant.getPlayer().getId();
                boolean isWinner = participant.getTeamSide().equals(winningTeamSide);
                
                // Conta quantos gols a jogadora fez
                long goalsScored = goals.stream()
                        .filter(g -> g.getPlayer().getId().equals(playerId))
                        .count();
                
                if (isWinner) {
                    // Vitória: 3 pontos base + 1 por gol
                    int totalPoints = 3 + (int) goalsScored;
                    
                    playerRankingService.addWin(playerId);
                    
                    // Adiciona pontos extras por gols
                    if (goalsScored > 0) {
                        playerRankingService.addBonusPoints(playerId, (int) goalsScored);
                        log.debug("Added win (3 points) + {} goal bonus to player {} = {} total points", 
                                goalsScored, playerId, totalPoints);
                    } else {
                        log.debug("Added win (3 points) to player {}", playerId);
                    }
                    
                    // Se participou com time, adiciona pontos para o time também (apenas pontos base, sem bônus de gols)
                    if (participant.isTeamParticipation()) {
                        Player player = participant.getPlayer();
                        if (player.getTeams() != null && !player.getTeams().isEmpty()) {
                            for (Team team : player.getTeams()) {
                                teamRankingService.addWin(team.getId());
                                log.debug("Added win to team {}", team.getId());
                            }
                        }
                    }
                } else {
                    // Derrota: 0 pontos base + 1 por gol
                    int totalPoints = (int) goalsScored;
                    
                    playerRankingService.addLoss(playerId);
                    
                    // Adiciona pontos extras por gols mesmo na derrota
                    if (goalsScored > 0) {
                        playerRankingService.addBonusPoints(playerId, (int) goalsScored);
                        log.debug("Added loss (0 points) + {} goal bonus to player {} = {} total points", 
                                goalsScored, playerId, totalPoints);
                    } else {
                        log.debug("Added loss (0 points) to player {}", playerId);
                    }
                    
                    // Se participou com time, adiciona pontos para o time também (apenas pontos base, sem bônus de gols)
                    if (participant.isTeamParticipation()) {
                        Player player = participant.getPlayer();
                        if (player.getTeams() != null && !player.getTeams().isEmpty()) {
                            for (Team team : player.getTeams()) {
                                teamRankingService.addLoss(team.getId());
                                log.debug("Added loss to team {}", team.getId());
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Recalcula pontos de um jogo (útil para correções)
     */
    @Transactional
    public void recalculateGamePoints(Long gameId) {
        // Este método pode ser implementado futuramente se necessário
        // para recalcular pontos em caso de correção de placar
        log.warn("Recalculate points not implemented yet for game {}", gameId);
    }
}
