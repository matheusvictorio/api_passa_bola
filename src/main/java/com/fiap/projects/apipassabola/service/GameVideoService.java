package com.fiap.projects.apipassabola.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service para buscar vídeos do Azure Blob relacionados a jogos
 * Busca sob demanda, sem salvar no banco de dados
 */
@Slf4j
@Service
public class GameVideoService {
    
    @Autowired
    private AzureBlobStorageService blobService;
    
    // Pattern para extrair timestamp do nome do arquivo: clip_2025-11-07_16-31-36.mp4
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("clip_(\\d{4}-\\d{2}-\\d{2})_(\\d{2}-\\d{2}-\\d{2})");
    
    /**
     * Busca vídeos do Azure Blob próximos ao horário do jogo (±3 horas)
     * 
     * @param gameDate Data e hora do jogo
     * @return Lista de vídeos com informações (URL, filename, timestamp, diferença de tempo)
     */
    public List<Map<String, Object>> findVideosByGameTimestamp(LocalDateTime gameDate) {
        log.info("🎬 Buscando vídeos para jogo em: {}", gameDate);
        
        try {
            // 1. Listar todos os vídeos do container videos/videos/
            List<Map<String, Object>> allVideos = blobService.listVideosWithDetails("videos", "videos");
            log.info("📹 Total de vídeos no blob: {}", allVideos.size());
            
            // 2. Definir janela de tempo (±3 horas)
            LocalDateTime startTime = gameDate.minusHours(3);
            LocalDateTime endTime = gameDate.plusHours(3);
            
            log.info("⏰ Janela de busca: {} até {}", startTime, endTime);
            
            List<Map<String, Object>> matchingVideos = new ArrayList<>();
            
            // 3. Filtrar vídeos dentro da janela de tempo
            for (Map<String, Object> videoInfo : allVideos) {
                String filename = (String) videoInfo.get("filename");
                
                // Extrair timestamp do nome do arquivo
                LocalDateTime videoTimestamp = extractTimestampFromFilename(filename);
                
                if (videoTimestamp != null) {
                    // Verificar se está dentro da janela de tempo
                    if (!videoTimestamp.isBefore(startTime) && !videoTimestamp.isAfter(endTime)) {
                        log.info("✅ Vídeo encontrado: {} - Timestamp: {}", filename, videoTimestamp);
                        
                        // Criar resposta com informações do vídeo
                        Map<String, Object> videoResponse = new HashMap<>();
                        videoResponse.put("url", videoInfo.get("url"));
                        videoResponse.put("filename", filename);
                        videoResponse.put("videoTimestamp", videoTimestamp.toString());
                        videoResponse.put("size", videoInfo.get("size"));
                        videoResponse.put("lastModified", videoInfo.get("lastModified"));
                        
                        // Calcular diferença em minutos do horário do jogo
                        long minutesDiff = java.time.Duration.between(gameDate, videoTimestamp).toMinutes();
                        videoResponse.put("minutesFromGameStart", minutesDiff);
                        
                        // Descrição amigável da diferença de tempo
                        String timeDiffDescription;
                        if (minutesDiff == 0) {
                            timeDiffDescription = "No início do jogo";
                        } else if (minutesDiff > 0) {
                            timeDiffDescription = minutesDiff + " minutos após o início";
                        } else {
                            timeDiffDescription = Math.abs(minutesDiff) + " minutos antes do início";
                        }
                        videoResponse.put("timeDiffDescription", timeDiffDescription);
                        
                        matchingVideos.add(videoResponse);
                    }
                }
            }
            
            // 4. Ordenar por timestamp (mais antigos primeiro)
            matchingVideos.sort((v1, v2) -> {
                String ts1 = (String) v1.get("videoTimestamp");
                String ts2 = (String) v2.get("videoTimestamp");
                return ts1.compareTo(ts2);
            });
            
            log.info("✅ Total de vídeos encontrados: {}", matchingVideos.size());
            return matchingVideos;
            
        } catch (Exception e) {
            log.error("❌ Erro ao buscar vídeos: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao buscar vídeos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extrai timestamp do nome do arquivo
     * Formato esperado: clip_2025-11-07_16-31-36.mp4
     * 
     * @param filename Nome do arquivo
     * @return LocalDateTime ou null se não conseguir extrair
     */
    private LocalDateTime extractTimestampFromFilename(String filename) {
        try {
            Matcher matcher = TIMESTAMP_PATTERN.matcher(filename);
            
            if (matcher.find()) {
                String datePart = matcher.group(1); // 2025-11-07
                String timePart = matcher.group(2); // 16-31-36
                
                // Converter para formato LocalDateTime
                String dateTimeString = datePart + " " + timePart.replace("-", ":");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                
                return LocalDateTime.parse(dateTimeString, formatter);
            }
            
            return null;
            
        } catch (Exception e) {
            log.warn("⚠️ Não foi possível extrair timestamp de: {} - {}", filename, e.getMessage());
            return null;
        }
    }
}
