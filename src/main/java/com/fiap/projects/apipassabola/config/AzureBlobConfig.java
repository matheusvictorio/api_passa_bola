package com.fiap.projects.apipassabola.config;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do Azure Blob Storage
 * Cria o cliente principal para interagir com o Azure Blob Storage
 */
@Configuration
public class AzureBlobConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    /**
     * Bean do BlobServiceClient
     * Cliente principal para todas as operações com Azure Blob Storage
     */
    @Bean
    public BlobServiceClient blobServiceClient() {
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }
}
