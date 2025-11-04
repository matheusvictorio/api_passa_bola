package com.fiap.projects.apipassabola.service;

import com.fiap.projects.apipassabola.entity.*;
import com.fiap.projects.apipassabola.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TournamentService {
    
    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamRepository;
    private final TournamentMatchRepository tournamentMatchRepository;
    private final TeamRepository teamRepository;
    private final GameService gameService;
    
    /**
     * Cria um novo torneio
     */
    @Transactional
    public Tournament createTournament(Tournament tournament) {
        // Validações
        if (tournament.getGameType() == GameType.CUP && tournament.getMaxTeams() == null) {
            throw new IllegalArgumentException("Torneios de Copa devem ter um número máximo de times definido");
        }
        
        // Define maxTeams como próxima potência de 2 se não definido
        if (tournament.getMaxTeams() == null) {
            tournament.setMaxTeams(8); // Padrão
        } else {
            // Garante que maxTeams é potência de 2
            tournament.setMaxTeams(getNextPowerOfTwo(tournament.getMaxTeams()));
        }
        
        tournament.setTotalTeams(0);
        tournament.setBracketGenerated(false);
        tournament.setStatus(Tournament.TournamentStatus.REGISTRATION);
        
        return tournamentRepository.save(tournament);
    }
    
    /**
     * Inscreve um time no torneio
     */
    @Transactional
    public TournamentTeam registerTeam(Long tournamentId, Long teamId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneio não encontrado"));
        
        Team team = teamRepository.findById(teamId)
            .orElseThrow(() -> new IllegalArgumentException("Time não encontrado"));
        
        // Validações
        if (!tournament.isRegistrationOpen()) {
            throw new IllegalStateException("Inscrições fechadas para este torneio");
        }
        
        if (tournament.getTotalTeams() >= tournament.getMaxTeams()) {
            throw new IllegalStateException("Torneio já atingiu o número máximo de times");
        }
        
        if (tournamentTeamRepository.existsByTournamentIdAndTeamId(tournamentId, teamId)) {
            throw new IllegalStateException("Time já está inscrito neste torneio");
        }
        
        // Cria inscrição
        TournamentTeam tournamentTeam = new TournamentTeam();
        tournamentTeam.setTournament(tournament);
        tournamentTeam.setTeam(team);
        tournamentTeam.setStatus(TournamentTeam.TeamStatus.REGISTERED);
        
        tournamentTeam = tournamentTeamRepository.save(tournamentTeam);
        
        // Atualiza contador de times
        tournament.setTotalTeams(tournament.getTotalTeams() + 1);
        tournamentRepository.save(tournament);
        
        log.info("Time {} inscrito no torneio {}", team.getNameTeam(), tournament.getName());
        
        return tournamentTeam;
    }
    
    /**
     * Gera o chaveamento do torneio de forma aleatória
     */
    @Transactional
    public void generateBracket(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneio não encontrado"));
        
        // Validações
        if (tournament.isBracketGenerated()) {
            throw new IllegalStateException("Chaveamento já foi gerado para este torneio");
        }
        
        if (!tournament.canGenerateBracket()) {
            throw new IllegalStateException(
                "Não é possível gerar chaveamento. Verifique se há times suficientes e se o número é potência de 2"
            );
        }
        
        // Busca times inscritos
        List<TournamentTeam> teams = tournamentTeamRepository.findByTournamentId(tournamentId);
        
        if (teams.isEmpty()) {
            throw new IllegalStateException("Nenhum time inscrito no torneio");
        }
        
        // Embaralha times aleatoriamente
        List<TournamentTeam> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams, new Random());
        
        // Atribui posições de seed
        for (int i = 0; i < shuffledTeams.size(); i++) {
            TournamentTeam team = shuffledTeams.get(i);
            team.setSeedPosition(i + 1);
            team.setStatus(TournamentTeam.TeamStatus.CONFIRMED);
            tournamentTeamRepository.save(team);
        }
        
        // Gera as partidas do chaveamento
        generateMatches(tournament, shuffledTeams);
        
        // Atualiza status do torneio
        tournament.setBracketGenerated(true);
        tournament.setStatus(Tournament.TournamentStatus.BRACKET_READY);
        tournament.setCurrentRound(tournament.getRoundName(shuffledTeams.size()));
        tournamentRepository.save(tournament);
        
        log.info("Chaveamento gerado para o torneio {} com {} times", tournament.getName(), shuffledTeams.size());
    }
    
    /**
     * Gera todas as partidas do chaveamento
     */
    private void generateMatches(Tournament tournament, List<TournamentTeam> teams) {
        int totalTeams = teams.size();
        int currentRoundTeams = totalTeams;
        int matchIdCounter = 0;
        
        // Mapa para rastrear partidas e suas conexões
        Map<String, List<TournamentMatch>> roundMatches = new HashMap<>();
        
        // Gera partidas de todas as rodadas
        while (currentRoundTeams >= 2) {
            String roundName = tournament.getRoundName(currentRoundTeams);
            int matchesInRound = currentRoundTeams / 2;
            List<TournamentMatch> matches = new ArrayList<>();
            
            for (int i = 0; i < matchesInRound; i++) {
                TournamentMatch match = new TournamentMatch();
                match.setTournament(tournament);
                match.setRound(roundName);
                match.setMatchNumber(i + 1);
                match.setBracketPosition(matchIdCounter++);
                
                // Primeira rodada: define os times e cria o jogo
                if (currentRoundTeams == totalTeams) {
                    Team team1 = teams.get(i * 2).getTeam();
                    Team team2 = teams.get(i * 2 + 1).getTeam();
                    
                    match.setTeam1(team1);
                    match.setTeam2(team2);
                    match.setStatus(TournamentMatch.MatchStatus.SCHEDULED);
                    
                    // Cria o jogo automaticamente para a primeira rodada
                    Game game = gameService.createTournamentGame(
                        team1,
                        team2,
                        tournament.getVenue(),
                        tournament.getStartDate(),
                        tournament.getName(),
                        roundName,
                        tournament.getCreatorId()
                    );
                    match.setGame(game);
                    
                    log.info("Jogo criado automaticamente para partida {} vs {} no torneio {}", 
                        team1.getNameTeam(), team2.getNameTeam(), tournament.getName());
                } else {
                    // Rodadas seguintes: times serão definidos pelos vencedores
                    match.setStatus(TournamentMatch.MatchStatus.PENDING);
                }
                
                matches.add(match);
            }
            
            roundMatches.put(roundName, matches);
            currentRoundTeams /= 2;
        }
        
        // Salva todas as partidas e conecta com próximas rodadas
        List<String> rounds = new ArrayList<>(roundMatches.keySet());
        
        for (int i = 0; i < rounds.size() - 1; i++) {
            String currentRound = rounds.get(i);
            String nextRound = rounds.get(i + 1);
            
            List<TournamentMatch> currentMatches = roundMatches.get(currentRound);
            List<TournamentMatch> nextMatches = roundMatches.get(nextRound);
            
            // Salva partidas da rodada atual
            currentMatches = currentMatches.stream()
                .map(tournamentMatchRepository::save)
                .collect(Collectors.toList());
            
            // Conecta com próxima rodada
            for (int j = 0; j < currentMatches.size(); j++) {
                TournamentMatch currentMatch = currentMatches.get(j);
                int nextMatchIndex = j / 2;
                
                if (nextMatchIndex < nextMatches.size()) {
                    TournamentMatch nextMatch = nextMatches.get(nextMatchIndex);
                    currentMatch.setNextMatchId(nextMatch.getId());
                    tournamentMatchRepository.save(currentMatch);
                }
            }
        }
        
        // Salva partidas da última rodada (final)
        String finalRound = rounds.get(rounds.size() - 1);
        roundMatches.get(finalRound).forEach(tournamentMatchRepository::save);
    }
    
    /**
     * Atualiza resultado de uma partida e avança vencedor
     */
    @Transactional
    public TournamentMatch updateMatchResult(Long matchId, Integer team1Score, Integer team2Score) {
        TournamentMatch match = tournamentMatchRepository.findById(matchId)
            .orElseThrow(() -> new IllegalArgumentException("Partida não encontrada"));
        
        if (!match.isReady()) {
            throw new IllegalStateException("Partida ainda não tem ambos os times definidos");
        }
        
        match.setTeam1Score(team1Score);
        match.setTeam2Score(team2Score);
        match.setWinnerFromScore();
        match.setStatus(TournamentMatch.MatchStatus.FINISHED);
        
        match = tournamentMatchRepository.save(match);
        
        // Avança vencedor para próxima rodada
        if (match.hasWinner() && match.getNextMatchId() != null) {
            advanceWinner(match);
        }
        
        // Verifica se o torneio terminou
        checkTournamentCompletion(match.getTournament().getId());
        
        return match;
    }
    
    /**
     * Sincroniza resultado do jogo com a partida do torneio
     * Chamado quando um jogo é finalizado pelo sistema de jogos
     */
    @Transactional
    public void syncGameResultToMatch(Long gameId, Integer homeGoals, Integer awayGoals) {
        // Busca a partida do torneio associada ao jogo
        TournamentMatch match = tournamentMatchRepository.findAll().stream()
            .filter(m -> m.getGame() != null && m.getGame().getId().equals(gameId))
            .findFirst()
            .orElse(null);
        
        if (match != null) {
            log.info("Sincronizando resultado do jogo {} com partida do torneio {}", 
                gameId, match.getId());
            
            // Atualiza resultado da partida do torneio
            updateMatchResult(match.getId(), homeGoals, awayGoals);
        }
    }
    
    /**
     * Avança o vencedor para a próxima partida
     */
    private void advanceWinner(TournamentMatch match) {
        TournamentMatch nextMatch = tournamentMatchRepository.findById(match.getNextMatchId())
            .orElseThrow(() -> new IllegalArgumentException("Próxima partida não encontrada"));
        
        Team winner = match.getWinner();
        Tournament tournament = match.getTournament();
        
        // Define o vencedor no slot correto da próxima partida
        if (nextMatch.getTeam1() == null) {
            nextMatch.setTeam1(winner);
        } else if (nextMatch.getTeam2() == null) {
            nextMatch.setTeam2(winner);
        }
        
        // Se ambos os times estão definidos, a partida está pronta e cria o jogo
        if (nextMatch.isReady()) {
            nextMatch.setStatus(TournamentMatch.MatchStatus.SCHEDULED);
            
            // Cria o jogo automaticamente para a próxima rodada
            Game game = gameService.createTournamentGame(
                nextMatch.getTeam1(),
                nextMatch.getTeam2(),
                tournament.getVenue(),
                tournament.getStartDate(), // Pode ser ajustado conforme necessário
                tournament.getName(),
                nextMatch.getRound(),
                tournament.getCreatorId()
            );
            nextMatch.setGame(game);
            
            log.info("Jogo criado automaticamente para {} vs {} na rodada {} do torneio {}", 
                nextMatch.getTeam1().getNameTeam(), 
                nextMatch.getTeam2().getNameTeam(),
                nextMatch.getRound(),
                tournament.getName());
        }
        
        tournamentMatchRepository.save(nextMatch);
        log.info("Time {} avançou para a próxima rodada", winner.getNameTeam());
    }
    
    /**
     * Verifica se o torneio foi concluído
     */
    private void checkTournamentCompletion(Long tournamentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
            .orElseThrow(() -> new IllegalArgumentException("Torneio não encontrado"));
        
        // Busca a final
        List<TournamentMatch> finalMatches = tournamentMatchRepository
            .findByTournamentIdAndRound(tournamentId, "FINAL");
        
        if (!finalMatches.isEmpty()) {
            TournamentMatch finalMatch = finalMatches.get(0);
            
            if (finalMatch.isFinished() && finalMatch.hasWinner()) {
                // Torneio concluído
                tournament.setStatus(Tournament.TournamentStatus.FINISHED);
                tournament.setEndDate(LocalDateTime.now());
                tournamentRepository.save(tournament);
                
                // Atualiza status dos times
                updateTeamStatuses(tournamentId, finalMatch);
                
                log.info("Torneio {} finalizado! Campeão: {}", 
                    tournament.getName(), 
                    finalMatch.getWinner().getNameTeam());
            }
        }
    }
    
    /**
     * Atualiza status dos times após conclusão do torneio
     */
    private void updateTeamStatuses(Long tournamentId, TournamentMatch finalMatch) {
        // Campeão
        TournamentTeam champion = tournamentTeamRepository
            .findByTournamentIdAndTeamId(tournamentId, finalMatch.getWinner().getId())
            .orElse(null);
        
        if (champion != null) {
            champion.setStatus(TournamentTeam.TeamStatus.CHAMPION);
            tournamentTeamRepository.save(champion);
        }
        
        // Vice-campeão
        Team runnerUp = finalMatch.getTeam1().equals(finalMatch.getWinner()) 
            ? finalMatch.getTeam2() 
            : finalMatch.getTeam1();
        
        TournamentTeam runnerUpTeam = tournamentTeamRepository
            .findByTournamentIdAndTeamId(tournamentId, runnerUp.getId())
            .orElse(null);
        
        if (runnerUpTeam != null) {
            runnerUpTeam.setStatus(TournamentTeam.TeamStatus.RUNNER_UP);
            tournamentTeamRepository.save(runnerUpTeam);
        }
        
        // Demais times eliminados
        List<TournamentTeam> allTeams = tournamentTeamRepository.findByTournamentId(tournamentId);
        for (TournamentTeam team : allTeams) {
            if (team.getStatus() == TournamentTeam.TeamStatus.CONFIRMED) {
                team.setStatus(TournamentTeam.TeamStatus.ELIMINATED);
                tournamentTeamRepository.save(team);
            }
        }
    }
    
    // Métodos de consulta
    
    public Tournament findById(Long id) {
        return tournamentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Torneio não encontrado"));
    }
    
    public List<Tournament> findAll() {
        return tournamentRepository.findAll();
    }
    
    public Page<Tournament> findAll(Pageable pageable) {
        return tournamentRepository.findAll(pageable);
    }
    
    public List<Tournament> findByGameType(GameType gameType) {
        return tournamentRepository.findByGameType(gameType);
    }
    
    public List<Tournament> findOpenForRegistration() {
        return tournamentRepository.findOpenForRegistration();
    }
    
    public List<TournamentTeam> getTeams(Long tournamentId) {
        return tournamentTeamRepository.findByTournamentIdOrderBySeedPosition(tournamentId);
    }
    
    public List<TournamentMatch> getMatches(Long tournamentId) {
        return tournamentMatchRepository.findByTournamentIdOrderByRound(tournamentId);
    }
    
    public List<TournamentMatch> getMatchesByRound(Long tournamentId, String round) {
        return tournamentMatchRepository.findByTournamentIdAndRound(tournamentId, round);
    }
    
    // Métodos auxiliares
    
    private int getNextPowerOfTwo(int n) {
        int power = 2;
        while (power < n) {
            power *= 2;
        }
        return power;
    }
}
