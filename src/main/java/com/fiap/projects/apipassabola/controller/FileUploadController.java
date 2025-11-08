    package com.fiap.projects.apipassabola.controller;

import com.fiap.projects.apipassabola.service.AzureBlobStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller para gerenciar upload de arquivos para Azure Blob Storage
 * Endpoints para avatares, banners, imagens de posts/jogos, etc.
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private AzureBlobStorageService blobService;

    /**
     * Upload de avatar de usuário
     * POST /api/files/users/{userId}/avatar?userType=PLAYER
     */
    @PostMapping("/users/{userId}/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadUserAvatar(
            @PathVariable Long userId,
            @RequestParam String userType,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de avatar para usuário: {} tipo: {}", userId, userType);

            // Validações
            if (!blobService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tipo de arquivo inválido. Use: JPG, PNG, GIF ou WebP"));
            }

            if (!blobService.isValidFileSize(file, 5.0)) { // 5MB máximo para avatar
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Avatar muito grande. Máximo: 5MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            // Upload
            String avatarUrl = blobService.uploadUserAvatar(file, userId, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Avatar atualizado com sucesso");
            response.put("url", avatarUrl);
            response.put("userId", userId);
            response.put("userType", userType);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao atualizar avatar: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao atualizar avatar: " + e.getMessage()));
        }
    }

    /**
     * Upload de banner de usuário
     * POST /api/files/users/{userId}/banner?userType=PLAYER
     */
    @PostMapping("/users/{userId}/banner")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadUserBanner(
            @PathVariable Long userId,
            @RequestParam String userType,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de banner para usuário: {} tipo: {}", userId, userType);

            // Validações
            if (!blobService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tipo de arquivo inválido. Use: JPG, PNG, GIF ou WebP"));
            }

            if (!blobService.isValidFileSize(file, 10.0)) { // 10MB máximo para banner
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Banner muito grande. Máximo: 10MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            // Upload
            String bannerUrl = blobService.uploadUserBanner(file, userId, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Banner atualizado com sucesso");
            response.put("url", bannerUrl);
            response.put("userId", userId);
            response.put("userType", userType);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao atualizar banner: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao atualizar banner: " + e.getMessage()));
        }
    }

    /**
     * Upload de imagem de post
     * POST /api/files/posts/{postId}/image
     */
    @PostMapping("/posts/{postId}/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadPostImage(
            @PathVariable Long postId,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de imagem para post: {}", postId);

            // Validações
            if (!blobService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tipo de arquivo inválido. Use: JPG, PNG, GIF ou WebP"));
            }

            if (!blobService.isValidFileSize(file, 10.0)) { // 10MB máximo
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Imagem muito grande. Máximo: 10MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            // Upload
            String imageUrl = blobService.uploadPostImage(file, postId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem do post enviada com sucesso");
            response.put("url", imageUrl);
            response.put("postId", postId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao enviar imagem do post: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao enviar imagem: " + e.getMessage()));
        }
    }

    /**
     * Upload de imagem de jogo
     * POST /api/files/games/{gameId}/image
     */
    @PostMapping("/games/{gameId}/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadGameImage(
            @PathVariable Long gameId,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de imagem para jogo: {}", gameId);

            // Validações
            if (!blobService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tipo de arquivo inválido. Use: JPG, PNG, GIF ou WebP"));
            }

            if (!blobService.isValidFileSize(file, 10.0)) { // 10MB máximo
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Imagem muito grande. Máximo: 10MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            // Upload
            String imageUrl = blobService.uploadGameImage(file, gameId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Imagem do jogo enviada com sucesso");
            response.put("url", imageUrl);
            response.put("gameId", gameId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao enviar imagem do jogo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao enviar imagem: " + e.getMessage()));
        }
    }

    /**
     * Upload de logo de time
     * POST /api/files/teams/{teamId}/logo
     */
    @PostMapping("/teams/{teamId}/logo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadTeamLogo(
            @PathVariable Long teamId,
            @RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de logo para time: {}", teamId);

            // Validações
            if (!blobService.isValidImageFile(file)) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Tipo de arquivo inválido. Use: JPG, PNG, GIF ou WebP"));
            }

            if (!blobService.isValidFileSize(file, 5.0)) { // 5MB máximo
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Logo muito grande. Máximo: 5MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            // Upload
            String logoUrl = blobService.uploadTeamLogo(file, teamId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Logo do time enviado com sucesso");
            response.put("url", logoUrl);
            response.put("teamId", teamId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao enviar logo do time: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao enviar logo: " + e.getMessage()));
        }
    }

    /**
     * Upload de documento
     * POST /api/files/documents?category=contratos
     */
    @PostMapping("/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {

        try {
            log.info("Upload de documento categoria: {}", category);

            if (!blobService.isValidFileSize(file, 50.0)) { // 50MB máximo para documentos
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Documento muito grande. Máximo: 50MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            String documentUrl = blobService.uploadDocument(file, category);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Documento enviado com sucesso");
            response.put("url", documentUrl);
            response.put("category", category);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao enviar documento: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao enviar documento: " + e.getMessage()));
        }
    }

    /**
     * Upload temporário
     * POST /api/files/temp
     */
    @PostMapping("/temp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadTempFile(@RequestParam("file") MultipartFile file) {

        try {
            log.info("Upload de arquivo temporário");

            if (!blobService.isValidFileSize(file, 20.0)) { // 20MB máximo para temp
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Arquivo muito grande. Máximo: 20MB. Tamanho atual: " 
                                + String.format("%.2f MB", blobService.getFileSizeMB(file))));
            }

            String tempUrl = blobService.uploadTempFile(file);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Arquivo temporário criado");
            response.put("url", tempUrl);
            response.put("expires", "7 dias");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro no upload temporário: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro no upload temporário: " + e.getMessage()));
        }
    }

    /**
     * Listar imagens de um post
     * GET /api/files/posts/{postId}/images
     */
    @GetMapping("/posts/{postId}/images")
    public ResponseEntity<?> listPostImages(@PathVariable Long postId) {
        try {
            log.info("Listando imagens do post: {}", postId);

            String folder = "posts/" + postId;
            log.info("Buscando na pasta: {}", folder);
            
            List<String> images = blobService.listFiles("imagens", folder);
            log.info("Imagens encontradas: {}", images.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("postId", postId);
            response.put("folder", folder); // Para debug
            response.put("images", images);
            response.put("count", images.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar imagens: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao listar imagens: " + e.getMessage()));
        }
    }

    /**
     * Listar imagens de um jogo
     * GET /api/files/games/{gameId}/images
     */
    @GetMapping("/games/{gameId}/images")
    public ResponseEntity<?> listGameImages(@PathVariable Long gameId) {
        try {
            log.info("Listando imagens do jogo: {}", gameId);

            String folder = "games/" + gameId;
            log.info("Buscando na pasta: {}", folder);
            
            List<String> images = blobService.listFiles("imagens", folder);
            log.info("Imagens encontradas: {}", images.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("gameId", gameId);
            response.put("folder", folder); // Para debug
            response.put("images", images);
            response.put("count", images.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar imagens: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao listar imagens: " + e.getMessage()));
        }
    }

    /**
     * Listar avatares de um usuário
     * GET /api/files/users/{userId}/avatars?userType=PLAYER
     */
    @GetMapping("/users/{userId}/avatars")
    public ResponseEntity<?> listUserAvatars(
            @PathVariable Long userId,
            @RequestParam String userType) {

        try {
            log.info("Listando avatares do usuário: {} tipo: {}", userId, userType);

            String folder = "users/" + userType.toLowerCase() + "/" + userId;
            log.info("Buscando na pasta: {}", folder);
            
            List<String> avatars = blobService.listFiles("avatars", folder);
            log.info("Avatares encontrados: {}", avatars.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("userType", userType);
            response.put("folder", folder); // Para debug
            response.put("avatars", avatars);
            response.put("count", avatars.size());
            response.put("currentAvatar", avatars.isEmpty() ? null : avatars.get(avatars.size() - 1)); // Último upload

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar avatares: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao listar avatares: " + e.getMessage()));
        }
    }

    /**
     * Listar banners de um usuário
     * GET /api/files/users/{userId}/banners?userType=PLAYER
     */
    @GetMapping("/users/{userId}/banners")
    public ResponseEntity<?> listUserBanners(
            @PathVariable Long userId,
            @RequestParam String userType) {

        try {
            log.info("Listando banners do usuário: {} tipo: {}", userId, userType);

            String folder = "banners/" + userType.toLowerCase() + "/" + userId;
            log.info("Buscando na pasta: {}", folder);
            
            List<String> banners = blobService.listFiles("avatars", folder);
            log.info("Banners encontrados: {}", banners.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("userType", userType);
            response.put("folder", folder); // Para debug
            response.put("banners", banners);
            response.put("count", banners.size());
            response.put("currentBanner", banners.isEmpty() ? null : banners.get(banners.size() - 1)); // Último upload

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar banners: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao listar banners: " + e.getMessage()));
        }
    }

    /**
     * Listar logos de um time
     * GET /api/files/teams/{teamId}/logos
     */
    @GetMapping("/teams/{teamId}/logos")
    public ResponseEntity<?> listTeamLogos(@PathVariable Long teamId) {
        try {
            log.info("Listando logos do time: {}", teamId);

            String folder = "teams/" + teamId;
            log.info("Buscando na pasta: {}", folder);
            
            List<String> logos = blobService.listFiles("imagens", folder);
            log.info("Logos encontrados: {}", logos.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("teamId", teamId);
            response.put("folder", folder); // Para debug
            response.put("logos", logos);
            response.put("count", logos.size());
            response.put("currentLogo", logos.isEmpty() ? null : logos.get(logos.size() - 1)); // Último upload

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao listar logos: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao listar logos: " + e.getMessage()));
        }
    }

    /**
     * Deletar arquivo pela URL
     * DELETE /api/files/delete
     */
    @DeleteMapping("/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteFile(@RequestParam("url") String fileUrl) {

        try {
            log.info("Deletando arquivo: {}", fileUrl);

            boolean deleted = blobService.deleteFileByUrl(fileUrl);

            Map<String, Object> response = new HashMap<>();
            response.put("success", deleted);
            response.put("message", deleted ? "Arquivo deletado com sucesso" : "Arquivo não encontrado");
            response.put("url", fileUrl);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erro ao deletar arquivo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Erro ao deletar arquivo: " + e.getMessage()));
        }
    }

    /**
     * Criar resposta de erro padronizada
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}
