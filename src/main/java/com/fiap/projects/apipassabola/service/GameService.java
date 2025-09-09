package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.dto.request.GameRequest;
import com.fiap.projects.apipassabola.dto.response.GameResponse;
import com.fiap.projects.apipassabola.dto.response.OrganizationSummaryResponse;
import com.fiap.projects.apipassabola.entity.Game;
import com.fiap.projects.apipassabola.entity.Organization;
import com.fiap.projects.apipassabola.exception.BusinessException;
import com.fiap.projects.apipassabola.exception.ResourceNotFoundException;
import com.fiap.projects.apipassabola.repository.GameRepository;
import com.fiap.projects.apipassabola.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class GameService {
    
    private final GameRepository gameRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationService organizationService;
    
    public Page<GameResponse> findAll(Pageable pageable) {
        return gameRepository.findAll(pageable).map(this::convertToResponse);
    }
    
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        return convertToResponse(game);
    }
    
    public Page<GameResponse> findByOrganization(Long organizationId, Pageable pageable) {
        return gameRepository.findByHomeTeamIdOrAwayTeamId(organizationId, organizationId, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByStatus(Game.GameStatus status, Pageable pageable) {
        return gameRepository.findByStatus(status, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByChampionship(String championship, Pageable pageable) {
        return gameRepository.findByChampionshipContainingIgnoreCase(championship, pageable)
                .map(this::convertToResponse);
    }
    
    public Page<GameResponse> findByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return gameRepository.findByGameDateBetween(startDate, endDate, pageable)
                .map(this::convertToResponse);
    }
    
    public GameResponse create(GameRequest request) {
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new BusinessException("Home team and away team cannot be the same");
        }
        
        Organization homeTeam = organizationRepository.findById(request.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getHomeTeamId()));
        Organization awayTeam = organizationRepository.findById(request.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getAwayTeamId()));
        
        Game game = new Game();
        game.setHomeTeam(homeTeam);
        game.setAwayTeam(awayTeam);
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setChampionship(request.getChampionship());
        game.setRound(request.getRound());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public GameResponse update(Long id, GameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        if (request.getHomeTeamId() != null && !request.getHomeTeamId().equals(game.getHomeTeam().getId())) {
            Organization homeTeam = organizationRepository.findById(request.getHomeTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getHomeTeamId()));
            game.setHomeTeam(homeTeam);
        }
        
        if (request.getAwayTeamId() != null && !request.getAwayTeamId().equals(game.getAwayTeam().getId())) {
            Organization awayTeam = organizationRepository.findById(request.getAwayTeamId())
                    .orElseThrow(() -> new ResourceNotFoundException("Organization", "id", request.getAwayTeamId()));
            game.setAwayTeam(awayTeam);
        }
        
        if (game.getHomeTeam().getId().equals(game.getAwayTeam().getId())) {
            throw new BusinessException("Home team and away team cannot be the same");
        }
        
        game.setGameDate(request.getGameDate());
        game.setVenue(request.getVenue());
        game.setHomeGoals(request.getHomeGoals());
        game.setAwayGoals(request.getAwayGoals());
        game.setStatus(request.getStatus());
        game.setChampionship(request.getChampionship());
        game.setRound(request.getRound());
        game.setNotes(request.getNotes());
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new ResourceNotFoundException("Game", "id", id);
        }
        gameRepository.deleteById(id);
    }
    
    public GameResponse updateScore(Long id, Integer homeGoals, Integer awayGoals) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Game", "id", id));
        
        if (homeGoals != null && homeGoals < 0) {
            throw new BusinessException("Home goals cannot be negative");
        }
        if (awayGoals != null && awayGoals < 0) {
            throw new BusinessException("Away goals cannot be negative");
        }
        
        game.setHomeGoals(homeGoals);
        game.setAwayGoals(awayGoals);
        
        // Auto-update status based on current time and score
        if (game.getGameDate().isBefore(LocalDateTime.now()) && 
            homeGoals != null && awayGoals != null) {
            game.setStatus(Game.GameStatus.FINISHED);
        }
        
        Game savedGame = gameRepository.save(game);
        return convertToResponse(savedGame);
    }
    
    private GameResponse convertToResponse(Game game) {
        GameResponse response = new GameResponse();
        response.setId(game.getId());
        response.setHomeTeam(organizationService.convertToSummaryResponse(game.getHomeTeam()));
        response.setAwayTeam(organizationService.convertToSummaryResponse(game.getAwayTeam()));
        response.setGameDate(game.getGameDate());
        response.setVenue(game.getVenue());
        response.setHomeGoals(game.getHomeGoals());
        response.setAwayGoals(game.getAwayGoals());
        response.setStatus(game.getStatus());
        response.setChampionship(game.getChampionship());
        response.setRound(game.getRound());
        response.setNotes(game.getNotes());
        response.setResult(game.getResult());
        response.setDraw(game.isDraw());
        response.setCreatedAt(game.getCreatedAt());
        response.setUpdatedAt(game.getUpdatedAt());
        
        if (game.getWinner() != null) {
            response.setWinner(organizationService.convertToSummaryResponse(game.getWinner()));
        }
        
        return response;
    }
}
