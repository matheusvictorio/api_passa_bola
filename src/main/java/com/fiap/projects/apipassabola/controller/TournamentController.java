package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.dto.request.MatchResultRequest;
import com.fiap.projects.apipassabola.dto.request.TournamentRequest;
import com.fiap.projects.apipassabola.dto.response.TournamentMatchResponse;
import com.fiap.projects.apipassabola.dto.response.TournamentResponse;
import com.fiap.projects.apipassabola.dto.response.TournamentTeamResponse;
import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.service.TournamentService;
import com.fiap.projects.apipassabola.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    
    private final TournamentService tournamentService;
    private final UserContextService userContextService;
    
    /**
     * Criar novo torneio
     * ORGANIZATION: pode criar torneios de COPA (obrigatório chaveamento)
     * PLAYER: pode criar torneios de CAMPEONATO (chaveamento opcional)
     */
    @PostMapping
    @PreAuthorize("hasRole('ORGANIZATION') or hasRole('PLAYER')")
    public ResponseEntity<TournamentResponse> createTournament(@Valid @RequestBody TournamentRequest request) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        
        // Validação: apenas ORGANIZATION pode criar torneios de COPA
        if (request.getGameType() == GameType.CUP && currentUser.getUserType() != UserType.ORGANIZATION) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(null);
        }
        
        Tournament tournament = new Tournament();
        tournament.setName(request.getName());
        tournament.setGameType(request.getGameType());
        tournament.setDescription(request.getDescription());
        tournament.setVenue(request.getVenue());
        tournament.setStartDate(request.getStartDate());
        tournament.setMaxTeams(request.getMaxTeams());
        tournament.setCreatorId(currentUser.getUserId());
        tournament.setCreatorUsername(userContextService.getCurrentUsername());
        
        Tournament created = tournamentService.createTournament(tournament);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertToResponse(created));
    }
    
    /**
     * Buscar torneio por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TournamentResponse> getTournament(@PathVariable Long id) {
        Tournament tournament = tournamentService.findById(id);
        return ResponseEntity.ok(convertToDetailedResponse(tournament));
    }
    
    /**
     * Listar todos os torneios
     */
    @GetMapping
    public ResponseEntity<Page<TournamentResponse>> getAllTournaments(Pageable pageable) {
        Page<Tournament> tournaments = tournamentService.findAll(pageable);
        return ResponseEntity.ok(tournaments.map(this::convertToResponse));
    }
    
    /**
     * Listar torneios por tipo
     */
    @GetMapping("/type/{gameType}")
    public ResponseEntity<List<TournamentResponse>> getTournamentsByType(@PathVariable GameType gameType) {
        List<Tournament> tournaments = tournamentService.findByGameType(gameType);
        return ResponseEntity.ok(tournaments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Listar torneios abertos para inscrição
     */
    @GetMapping("/open")
    public ResponseEntity<List<TournamentResponse>> getOpenTournaments() {
        List<Tournament> tournaments = tournamentService.findOpenForRegistration();
        return ResponseEntity.ok(tournaments.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Inscrever time no torneio
     * Apenas líderes de times podem inscrever seus times
     */
    @PostMapping("/{tournamentId}/register/{teamId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<TournamentTeamResponse> registerTeam(
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {
        
        TournamentTeam tournamentTeam = tournamentService.registerTeam(tournamentId, teamId);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(convertToTeamResponse(tournamentTeam));
    }
    
    /**
     * Gerar chaveamento do torneio
     * Apenas o criador do torneio pode gerar o chaveamento
     */
    @PostMapping("/{tournamentId}/generate-bracket")
    @PreAuthorize("hasRole('ORGANIZATION') or hasRole('PLAYER')")
    public ResponseEntity<TournamentResponse> generateBracket(@PathVariable Long tournamentId) {
        UserContextService.UserIdAndType currentUser = userContextService.getCurrentUserIdAndType();
        Tournament tournament = tournamentService.findById(tournamentId);
        
        // Verifica se é o criador
        if (!tournament.getCreatorId().equals(currentUser.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        tournamentService.generateBracket(tournamentId);
        Tournament updated = tournamentService.findById(tournamentId);
        
        return ResponseEntity.ok(convertToDetailedResponse(updated));
    }
    
    /**
     * Listar times inscritos no torneio
     */
    @GetMapping("/{tournamentId}/teams")
    public ResponseEntity<List<TournamentTeamResponse>> getTeams(@PathVariable Long tournamentId) {
        List<TournamentTeam> teams = tournamentService.getTeams(tournamentId);
        return ResponseEntity.ok(teams.stream()
            .map(this::convertToTeamResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Listar todas as partidas do torneio
     */
    @GetMapping("/{tournamentId}/matches")
    public ResponseEntity<List<TournamentMatchResponse>> getMatches(@PathVariable Long tournamentId) {
        List<TournamentMatch> matches = tournamentService.getMatches(tournamentId);
        return ResponseEntity.ok(matches.stream()
            .map(this::convertToMatchResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Listar partidas de uma rodada específica
     */
    @GetMapping("/{tournamentId}/matches/round/{round}")
    public ResponseEntity<List<TournamentMatchResponse>> getMatchesByRound(
            @PathVariable Long tournamentId,
            @PathVariable String round) {
        
        List<TournamentMatch> matches = tournamentService.getMatchesByRound(tournamentId, round);
        return ResponseEntity.ok(matches.stream()
            .map(this::convertToMatchResponse)
            .collect(Collectors.toList()));
    }
    
    /**
     * Atualizar resultado de uma partida
     * Apenas o criador do torneio pode atualizar resultados
     */
    @PatchMapping("/matches/{matchId}/result")
    @PreAuthorize("hasRole('ORGANIZATION') or hasRole('PLAYER')")
    public ResponseEntity<TournamentMatchResponse> updateMatchResult(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchResultRequest request) {
        
        TournamentMatch match = tournamentService.updateMatchResult(
            matchId,
            request.getTeam1Score(),
            request.getTeam2Score()
        );
        
        return ResponseEntity.ok(convertToMatchResponse(match));
    }
    
    // Métodos de conversão
    
    private TournamentResponse convertToResponse(Tournament tournament) {
        TournamentResponse response = new TournamentResponse();
        response.setId(tournament.getId());
        response.setName(tournament.getName());
        response.setGameType(tournament.getGameType());
        response.setCreatorId(tournament.getCreatorId());
        response.setCreatorUsername(tournament.getCreatorUsername());
        response.setStatus(tournament.getStatus());
        response.setDescription(tournament.getDescription());
        response.setVenue(tournament.getVenue());
        response.setStartDate(tournament.getStartDate());
        response.setEndDate(tournament.getEndDate());
        response.setTotalTeams(tournament.getTotalTeams());
        response.setMaxTeams(tournament.getMaxTeams());
        response.setCurrentRound(tournament.getCurrentRound());
        response.setBracketGenerated(tournament.getBracketGenerated());
        response.setCreatedAt(tournament.getCreatedAt());
        response.setUpdatedAt(tournament.getUpdatedAt());
        return response;
    }
    
    private TournamentResponse convertToDetailedResponse(Tournament tournament) {
        TournamentResponse response = convertToResponse(tournament);
        
        // Adiciona times e partidas
        List<TournamentTeam> teams = tournamentService.getTeams(tournament.getId());
        response.setTeams(teams.stream()
            .map(this::convertToTeamResponse)
            .collect(Collectors.toList()));
        
        List<TournamentMatch> matches = tournamentService.getMatches(tournament.getId());
        response.setMatches(matches.stream()
            .map(this::convertToMatchResponse)
            .collect(Collectors.toList()));
        
        return response;
    }
    
    private TournamentTeamResponse convertToTeamResponse(TournamentTeam tournamentTeam) {
        TournamentTeamResponse response = new TournamentTeamResponse();
        response.setId(tournamentTeam.getId());
        response.setTournamentId(tournamentTeam.getTournament().getId());
        response.setTeamId(tournamentTeam.getTeam().getId());
        response.setTeamName(tournamentTeam.getTeam().getNameTeam());
        response.setSeedPosition(tournamentTeam.getSeedPosition());
        response.setStatus(tournamentTeam.getStatus());
        response.setRegisteredAt(tournamentTeam.getRegisteredAt());
        return response;
    }
    
    private TournamentMatchResponse convertToMatchResponse(TournamentMatch match) {
        TournamentMatchResponse response = new TournamentMatchResponse();
        response.setId(match.getId());
        response.setTournamentId(match.getTournament().getId());
        response.setRound(match.getRound());
        response.setMatchNumber(match.getMatchNumber());
        
        if (match.getTeam1() != null) {
            response.setTeam1Id(match.getTeam1().getId());
            response.setTeam1Name(match.getTeam1().getNameTeam());
        }
        
        if (match.getTeam2() != null) {
            response.setTeam2Id(match.getTeam2().getId());
            response.setTeam2Name(match.getTeam2().getNameTeam());
        }
        
        response.setTeam1Score(match.getTeam1Score());
        response.setTeam2Score(match.getTeam2Score());
        
        if (match.getWinner() != null) {
            response.setWinnerId(match.getWinner().getId());
            response.setWinnerName(match.getWinner().getNameTeam());
        }
        
        response.setStatus(match.getStatus());
        response.setScheduledDate(match.getScheduledDate());
        response.setBracketPosition(match.getBracketPosition());
        response.setNextMatchId(match.getNextMatchId());
        
        if (match.getGame() != null) {
            response.setGameId(match.getGame().getId());
        }
        
        response.setCreatedAt(match.getCreatedAt());
        response.setUpdatedAt(match.getUpdatedAt());
        
        return response;
    }
}
