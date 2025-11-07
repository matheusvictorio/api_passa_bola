package com.fiap.projects.apipassabola.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do Azure Storage
 * Mapeia as configurações do application.properties
 */
@Data
@Component
@ConfigurationProperties(prefix = "azure.storage")
public class AzureStorageProperties {

    private String accountName;
    private String accountKey;
    private String blobEndpoint;
    private String connectionString;

    private Container container = new Container();

    /**
     * Nomes dos containers no Azure Blob Storage
     */
    @Data
    public static class Container {
        private String imagens = "imagens";
        private String avatars = "avatars";
        private String documentos = "documentos";
        private String temp = "temp";
    }
}
