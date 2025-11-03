package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.PlayerRankingResponse;
import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.entity.Player;
import com.fiap.projects.apipassabola.entity.PlayerRanking;
import com.fiap.projects.apipassabola.repository.PlayerRankingRepository;
import com.fiap.projects.apipassabola.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerRankingService {
    
    private final PlayerRankingRepository playerRankingRepository;
    private final PlayerRepository playerRepository;
    
    /**
     * Obtém ou cria ranking de uma jogadora
     */
    @Transactional
    public PlayerRanking getOrCreateRanking(Long playerId) {
        return playerRankingRepository.findByPlayerId(playerId)
                .orElseGet(() -> {
                    Player player = playerRepository.findById(playerId)
                            .orElseThrow(() -> new RuntimeException("Player not found: " + playerId));
                    
                    PlayerRanking ranking = new PlayerRanking();
                    ranking.setPlayer(player);
                    ranking.setTotalPoints(0);
                    ranking.setDivision(Division.BRONZE);
                    ranking.setGamesWon(0);
                    ranking.setGamesDrawn(0);
                    ranking.setGamesLost(0);
                    ranking.setTotalGames(0);
                    ranking.setWinRate(0.0);
                    ranking.setCurrentStreak(0);
                    ranking.setBestStreak(0);
                    
                    return playerRankingRepository.save(ranking);
                });
    }
    
    /**
     * Adiciona vitória para uma jogadora
     */
    @Transactional
    public PlayerRanking addWin(Long playerId) {
        PlayerRanking ranking = getOrCreateRanking(playerId);
        ranking.addWin();
        return playerRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona empate para uma jogadora
     */
    @Transactional
    public PlayerRanking addDraw(Long playerId) {
        PlayerRanking ranking = getOrCreateRanking(playerId);
        ranking.addDraw();
        return playerRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona derrota para uma jogadora
     */
    @Transactional
    public PlayerRanking addLoss(Long playerId) {
        PlayerRanking ranking = getOrCreateRanking(playerId);
        ranking.addLoss();
        return playerRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona pontos bônus (por gols marcados)
     */
    @Transactional
    public PlayerRanking addBonusPoints(Long playerId, int points) {
        PlayerRanking ranking = getOrCreateRanking(playerId);
        ranking.addBonusPoints(points);
        return playerRankingRepository.save(ranking);
    }
    
    /**
     * Busca ranking de uma jogadora
     */
    @Transactional(readOnly = true)
    public PlayerRankingResponse getPlayerRanking(Long playerId) {
        PlayerRanking ranking = getOrCreateRanking(playerId);
        return convertToResponse(ranking);
    }
    
    /**
     * Busca ranking global com paginação
     */
    @Transactional(readOnly = true)
    public Page<PlayerRankingResponse> getGlobalRanking(Pageable pageable) {
        Page<PlayerRanking> rankings = playerRankingRepository.findAllByOrderByTotalPointsDesc(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca ranking por divisão
     */
    @Transactional(readOnly = true)
    public Page<PlayerRankingResponse> getRankingByDivision(Division division, Pageable pageable) {
        Page<PlayerRanking> rankings = playerRankingRepository.findByDivisionOrderByTotalPointsDesc(division, pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca top jogadoras
     */
    @Transactional(readOnly = true)
    public Page<PlayerRankingResponse> getTopPlayers(Pageable pageable) {
        Page<PlayerRanking> rankings = playerRankingRepository.findTopPlayers(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca jogadoras com melhor sequência de vitórias
     */
    @Transactional(readOnly = true)
    public Page<PlayerRankingResponse> getPlayersWithWinStreak(Pageable pageable) {
        Page<PlayerRanking> rankings = playerRankingRepository.findPlayersWithWinStreak(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca jogadoras com maior taxa de vitória
     */
    @Transactional(readOnly = true)
    public Page<PlayerRankingResponse> getPlayersByWinRate(Integer minGames, Pageable pageable) {
        Page<PlayerRanking> rankings = playerRankingRepository.findPlayersByWinRate(minGames, pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Converte entidade para response
     */
    private PlayerRankingResponse convertToResponse(PlayerRanking ranking) {
        PlayerRankingResponse response = new PlayerRankingResponse();
        response.setId(ranking.getId());
        response.setPlayerId(ranking.getPlayer().getId());
        response.setPlayerName(ranking.getPlayer().getName());
        response.setPlayerUsername(ranking.getPlayer().getRealUsername());
        response.setProfilePhotoUrl(ranking.getPlayer().getProfilePhotoUrl());
        response.setTotalPoints(ranking.getTotalPoints());
        response.setDivision(ranking.getDivision());
        response.setDivisionName(ranking.getDivision().getDisplayName());
        response.setGamesWon(ranking.getGamesWon());
        response.setGamesDrawn(ranking.getGamesDrawn());
        response.setGamesLost(ranking.getGamesLost());
        response.setTotalGames(ranking.getTotalGames());
        response.setWinRate(ranking.getWinRate());
        response.setCurrentStreak(ranking.getCurrentStreak());
        response.setBestStreak(ranking.getBestStreak());
        response.setPointsToNextDivision(ranking.getPointsToNextDivision());
        response.setLastGameDate(ranking.getLastGameDate());
        response.setCreatedAt(ranking.getCreatedAt());
        response.setUpdatedAt(ranking.getUpdatedAt());
        
        // Calcula posições
        Long globalPosition = playerRankingRepository.findPlayerPosition(
                ranking.getTotalPoints(), ranking.getId());
        response.setGlobalPosition(globalPosition);
        
        Long divisionPosition = playerRankingRepository.findPlayerPositionInDivision(
                ranking.getDivision(), ranking.getTotalPoints(), ranking.getId());
        response.setDivisionPosition(divisionPosition);
        
        return response;
    }
}
