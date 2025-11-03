package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.response.TeamRankingResponse;
import com.fiap.projects.apipassabola.entity.Division;
import com.fiap.projects.apipassabola.entity.Team;
import com.fiap.projects.apipassabola.entity.TeamRanking;
import com.fiap.projects.apipassabola.repository.TeamRankingRepository;
import com.fiap.projects.apipassabola.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamRankingService {
    
    private final TeamRankingRepository teamRankingRepository;
    private final TeamRepository teamRepository;
    
    /**
     * Obtém ou cria ranking de um time
     */
    @Transactional
    public TeamRanking getOrCreateRanking(Long teamId) {
        return teamRankingRepository.findByTeamId(teamId)
                .orElseGet(() -> {
                    Team team = teamRepository.findById(teamId)
                            .orElseThrow(() -> new RuntimeException("Team not found: " + teamId));
                    
                    TeamRanking ranking = new TeamRanking();
                    ranking.setTeam(team);
                    ranking.setTotalPoints(0);
                    ranking.setDivision(Division.BRONZE);
                    ranking.setGamesWon(0);
                    ranking.setGamesDrawn(0);
                    ranking.setGamesLost(0);
                    ranking.setTotalGames(0);
                    ranking.setWinRate(0.0);
                    ranking.setCurrentStreak(0);
                    ranking.setBestStreak(0);
                    
                    return teamRankingRepository.save(ranking);
                });
    }
    
    /**
     * Adiciona vitória para um time
     */
    @Transactional
    public TeamRanking addWin(Long teamId) {
        TeamRanking ranking = getOrCreateRanking(teamId);
        ranking.addWin();
        return teamRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona empate para um time
     */
    @Transactional
    public TeamRanking addDraw(Long teamId) {
        TeamRanking ranking = getOrCreateRanking(teamId);
        ranking.addDraw();
        return teamRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona derrota para um time
     */
    @Transactional
    public TeamRanking addLoss(Long teamId) {
        TeamRanking ranking = getOrCreateRanking(teamId);
        ranking.addLoss();
        return teamRankingRepository.save(ranking);
    }
    
    /**
     * Adiciona pontos bônus (por gols marcados)
     */
    @Transactional
    public TeamRanking addBonusPoints(Long teamId, int points) {
        TeamRanking ranking = getOrCreateRanking(teamId);
        ranking.addBonusPoints(points);
        return teamRankingRepository.save(ranking);
    }
    
    /**
     * Busca ranking de um time
     */
    @Transactional(readOnly = true)
    public TeamRankingResponse getTeamRanking(Long teamId) {
        TeamRanking ranking = getOrCreateRanking(teamId);
        return convertToResponse(ranking);
    }
    
    /**
     * Busca ranking global com paginação
     */
    @Transactional(readOnly = true)
    public Page<TeamRankingResponse> getGlobalRanking(Pageable pageable) {
        Page<TeamRanking> rankings = teamRankingRepository.findAllByOrderByTotalPointsDesc(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca ranking por divisão
     */
    @Transactional(readOnly = true)
    public Page<TeamRankingResponse> getRankingByDivision(Division division, Pageable pageable) {
        Page<TeamRanking> rankings = teamRankingRepository.findByDivisionOrderByTotalPointsDesc(division, pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca top times
     */
    @Transactional(readOnly = true)
    public Page<TeamRankingResponse> getTopTeams(Pageable pageable) {
        Page<TeamRanking> rankings = teamRankingRepository.findTopTeams(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca times com melhor sequência de vitórias
     */
    @Transactional(readOnly = true)
    public Page<TeamRankingResponse> getTeamsWithWinStreak(Pageable pageable) {
        Page<TeamRanking> rankings = teamRankingRepository.findTeamsWithWinStreak(pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Busca times com maior taxa de vitória
     */
    @Transactional(readOnly = true)
    public Page<TeamRankingResponse> getTeamsByWinRate(Integer minGames, Pageable pageable) {
        Page<TeamRanking> rankings = teamRankingRepository.findTeamsByWinRate(minGames, pageable);
        return rankings.map(this::convertToResponse);
    }
    
    /**
     * Converte entidade para response
     */
    private TeamRankingResponse convertToResponse(TeamRanking ranking) {
        TeamRankingResponse response = new TeamRankingResponse();
        response.setId(ranking.getId());
        response.setTeamId(ranking.getTeam().getId());
        response.setTeamName(ranking.getTeam().getNameTeam());
        response.setLeaderName(ranking.getTeam().getLeader().getName());
        response.setLeaderId(ranking.getTeam().getLeader().getId());
        response.setPlayersCount(ranking.getTeam().getPlayersCount());
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
        Long globalPosition = teamRankingRepository.findTeamPosition(
                ranking.getTotalPoints(), ranking.getId());
        response.setGlobalPosition(globalPosition);
        
        Long divisionPosition = teamRankingRepository.findTeamPositionInDivision(
                ranking.getDivision(), ranking.getTotalPoints(), ranking.getId());
        response.setDivisionPosition(divisionPosition);
        
        return response;
    }
}
