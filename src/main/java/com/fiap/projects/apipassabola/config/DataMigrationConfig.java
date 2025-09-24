package com.fiap.projects.apipassabola.config;

import com.fiap.projects.apipassabola.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

/**
 * Configuration class for data migration tasks.
 * This class handles data migrations that need to be run during application startup.
 */
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataMigrationConfig {
    
    private final FollowService followService;
    
    /**
     * Executes data migrations when the application is ready.
     * This ensures that all beans are initialized before migrations run.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void migrateData() {
        // Migrate favorite organizations to following organizations
        followService.migratePlayerFavoriteOrganizationsToFollowing();
    }
}
