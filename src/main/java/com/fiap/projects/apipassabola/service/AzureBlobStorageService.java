package com.fiap.projects.apipassabola.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.ListBlobsOptions;
import com.fiap.projects.apipassabola.config.AzureStorageProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service para gerenciar operações com Azure Blob Storage
 * Responsável por upload, download, delete e listagem de arquivos
 */
@Slf4j
@Service
public class AzureBlobStorageService {

    @Autowired
    private BlobServiceClient blobServiceClient;

    @Autowired
    private AzureStorageProperties azureProperties;

    /**
     * Upload de arquivo para o Azure Blob Storage
     *
     * @param file Arquivo a ser enviado
     * @param containerName Nome do container
     * @param folder Pasta dentro do container (opcional)
     * @return URL pública do arquivo
     */
    public String uploadFile(MultipartFile file, String containerName, String folder) {
        try {
            // Gerar nome único para o arquivo
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String blobName = folder != null ? folder + "/" + fileName : fileName;

            log.info("Iniciando upload do arquivo: {} para container: {}", blobName, containerName);

            // Obter cliente do blob
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            // Configurar headers HTTP
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType());

            // Upload do arquivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            String blobUrl = blobClient.getBlobUrl();
            log.info("Upload concluído com sucesso: {}", blobUrl);

            return blobUrl;

        } catch (IOException e) {
            log.error("Erro ao fazer upload do arquivo: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer upload do arquivo: " + e.getMessage(), e);
        } catch (BlobStorageException e) {
            log.error("Erro no Azure Blob Storage: {}", e.getMessage());
            throw new RuntimeException("Erro no Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Upload de foto de perfil (avatar) de usuário
     */
    public String uploadUserAvatar(MultipartFile file, Long userId, String userType) {
        String folder = "users/" + userType.toLowerCase() + "/" + userId;
        return uploadFile(file, azureProperties.getContainer().getAvatars(), folder);
    }

    /**
     * Upload de banner de usuário
     */
    public String uploadUserBanner(MultipartFile file, Long userId, String userType) {
        String folder = "banners/" + userType.toLowerCase() + "/" + userId;
        return uploadFile(file, azureProperties.getContainer().getAvatars(), folder);
    }

    /**
     * Upload de imagem de post
     */
    public String uploadPostImage(MultipartFile file, Long postId) {
        return uploadFile(file, azureProperties.getContainer().getImagens(), "posts/" + postId);
    }

    /**
     * Upload de imagem de jogo
     */
    public String uploadGameImage(MultipartFile file, Long gameId) {
        return uploadFile(file, azureProperties.getContainer().getImagens(), "games/" + gameId);
    }

    /**
     * Upload de logo de time
     */
    public String uploadTeamLogo(MultipartFile file, Long teamId) {
        return uploadFile(file, azureProperties.getContainer().getImagens(), "teams/" + teamId);
    }

    /**
     * Upload de documento (privado)
     */
    public String uploadDocument(MultipartFile file, String category) {
        return uploadFile(file, azureProperties.getContainer().getDocumentos(), category);
    }

    /**
     * Upload temporário (para preview, etc.)
     */
    public String uploadTempFile(MultipartFile file) {
        String folder = "temp/" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return uploadFile(file, azureProperties.getContainer().getTemp(), folder);
    }

    /**
     * Upload de vídeo de jogo
     * Mantém o nome original do arquivo para preservar o timestamp
     * @param file Arquivo de vídeo
     * @return URL do vídeo no Azure Blob
     */
    public String uploadGameVideo(MultipartFile file) {
        try {
            // Usar nome original do arquivo para preservar timestamp
            String originalFilename = file.getOriginalFilename();
            String blobName = "videos/" + originalFilename;
            
            log.info("Iniciando upload de vídeo: {} para container: videos", blobName);
            
            // Obter cliente do blob
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient("videos")
                    .getBlobClient(blobName);
            
            // Configurar headers HTTP para vídeo
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType(file.getContentType() != null ? file.getContentType() : "video/mp4");
            
            // Upload do arquivo
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            blobClient.setHttpHeaders(headers);
            
            String blobUrl = blobClient.getBlobUrl();
            log.info("Upload de vídeo concluído com sucesso: {}", blobUrl);
            
            return blobUrl;
            
        } catch (IOException e) {
            log.error("Erro ao fazer upload do vídeo: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer upload do vídeo: " + e.getMessage(), e);
        } catch (BlobStorageException e) {
            log.error("Erro no Azure Blob Storage: {}", e.getMessage());
            throw new RuntimeException("Erro no Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Download de arquivo
     */
    public byte[] downloadFile(String containerName, String blobName) {
        try {
            log.info("Iniciando download do arquivo: {} do container: {}", blobName, containerName);

            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);

            log.info("Download concluído com sucesso");
            return outputStream.toByteArray();

        } catch (BlobStorageException e) {
            log.error("Erro ao fazer download do arquivo: {}", e.getMessage());
            throw new RuntimeException("Erro ao fazer download do arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Deletar arquivo
     */
    public boolean deleteFile(String containerName, String blobName) {
        try {
            log.info("Deletando arquivo: {} do container: {}", blobName, containerName);

            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            boolean deleted = blobClient.deleteIfExists();
            
            if (deleted) {
                log.info("Arquivo deletado com sucesso");
            } else {
                log.warn("Arquivo não encontrado para deleção");
            }

            return deleted;

        } catch (BlobStorageException e) {
            log.error("Erro ao deletar arquivo: {}", e.getMessage());
            throw new RuntimeException("Erro ao deletar arquivo: " + e.getMessage(), e);
        }
    }

    /**
     * Deletar arquivo pela URL completa
     */
    public boolean deleteFileByUrl(String fileUrl) {
        try {
            // Extrair container e blob name da URL
            // URL format: https://stdev2495531.blob.core.windows.net/container/path/to/file.jpg
            String[] parts = fileUrl.replace(azureProperties.getBlobEndpoint() + "/", "").split("/", 2);
            
            if (parts.length < 2) {
                log.error("URL inválida: {}", fileUrl);
                return false;
            }

            String containerName = parts[0];
            String blobName = parts[1];

            return deleteFile(containerName, blobName);

        } catch (Exception e) {
            log.error("Erro ao processar URL para deleção: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Listar arquivos de um container
     */
    public List<String> listFiles(String containerName, String prefix) {
        List<String> fileUrls = new ArrayList<>();

        try {
            log.info("Listando arquivos do container: {} com prefix: {}", containerName, prefix);

            var containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Garantir que o prefix termine com / se não estiver vazio
            String normalizedPrefix = prefix;
            if (prefix != null && !prefix.isEmpty() && !prefix.endsWith("/")) {
                normalizedPrefix = prefix + "/";
            }

            // Listar todos os blobs com o prefixo
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(normalizedPrefix);
            for (BlobItem blobItem : containerClient.listBlobs(options, null)) {
                String blobUrl = containerClient.getBlobClient(blobItem.getName()).getBlobUrl();
                fileUrls.add(blobUrl);
                log.debug("Arquivo encontrado: {}", blobUrl);
            }

            log.info("Total de arquivos encontrados: {}", fileUrls.size());
            return fileUrls;

        } catch (BlobStorageException e) {
            log.error("Erro ao listar arquivos: {}", e.getMessage());
            throw new RuntimeException("Erro ao listar arquivos: " + e.getMessage(), e);
        }
    }

    /**
     * Listar vídeos do container com informações detalhadas
     * Retorna lista de mapas com URL e nome do arquivo
     */
    public List<Map<String, Object>> listVideosWithDetails(String containerName, String prefix) {
        List<Map<String, Object>> videos = new ArrayList<>();

        try {
            log.info("Listando vídeos do container: {} com prefix: {}", containerName, prefix);

            var containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Garantir que o prefix termine com / se não estiver vazio
            String normalizedPrefix = prefix;
            if (prefix != null && !prefix.isEmpty() && !prefix.endsWith("/")) {
                normalizedPrefix = prefix + "/";
            }

            // Listar todos os blobs com o prefixo
            ListBlobsOptions options = new ListBlobsOptions().setPrefix(normalizedPrefix);
            for (BlobItem blobItem : containerClient.listBlobs(options, null)) {
                Map<String, Object> videoInfo = new HashMap<>();
                
                String blobName = blobItem.getName();
                String blobUrl = containerClient.getBlobClient(blobName).getBlobUrl();
                
                // Extrair apenas o nome do arquivo (sem o path)
                String fileName = blobName.contains("/") ? 
                        blobName.substring(blobName.lastIndexOf("/") + 1) : blobName;
                
                videoInfo.put("url", blobUrl);
                videoInfo.put("filename", fileName);
                videoInfo.put("fullPath", blobName);
                videoInfo.put("size", blobItem.getProperties().getContentLength());
                videoInfo.put("lastModified", blobItem.getProperties().getLastModified());
                
                videos.add(videoInfo);
                log.debug("Vídeo encontrado: {}", fileName);
            }

            log.info("Total de vídeos encontrados: {}", videos.size());
            return videos;

        } catch (BlobStorageException e) {
            log.error("Erro ao listar vídeos: {}", e.getMessage());
            throw new RuntimeException("Erro ao listar vídeos: " + e.getMessage(), e);
        }
    }

    /**
     * Verificar se arquivo existe
     */
    public boolean fileExists(String containerName, String blobName) {
        try {
            BlobClient blobClient = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);

            return blobClient.exists();

        } catch (BlobStorageException e) {
            log.error("Erro ao verificar existência do arquivo: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obter URL pública do arquivo
     */
    public String getFileUrl(String containerName, String blobName) {
        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(blobName);

        return blobClient.getBlobUrl();
    }

    /**
     * Gerar nome único para arquivo
     */
    private String generateUniqueFileName(String originalFilename) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        return timestamp + "_" + uuid + extension;
    }

    /**
     * Validar tipo de arquivo de imagem
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")
        );
    }

    /**
     * Validar tamanho do arquivo (em MB)
     */
    public boolean isValidFileSize(MultipartFile file, double maxSizeMB) {
        if (file == null) {
            return false;
        }

        double fileSizeMB = (double) file.getSize() / (1024 * 1024);
        return fileSizeMB <= maxSizeMB;
    }

    /**
     * Obter tamanho do arquivo em MB
     */
    public double getFileSizeMB(MultipartFile file) {
        if (file == null) {
            return 0;
        }
        return (double) file.getSize() / (1024 * 1024);
    }

    /**
     * Validar tipo de arquivo de vídeo
     */
    public boolean isValidVideoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        return contentType != null && (
                contentType.equals("video/mp4") ||
                contentType.equals("video/mpeg") ||
                contentType.equals("video/quicktime") ||
                contentType.equals("video/x-msvideo") ||
                contentType.equals("video/webm")
        );
    }
}
