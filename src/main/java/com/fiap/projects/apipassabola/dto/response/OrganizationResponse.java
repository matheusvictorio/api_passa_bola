package com.fiap.projects.apipassabola.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String name;
    private String description;
    private String city;
    private String state;
    private String logoUrl;
    private String primaryColors;
    private Integer foundedYear;
    private String websiteUrl;
    private String contactEmail;
    private String contactPhone;
    private String cnpj;
    private int playersCount;
    private int totalGames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
