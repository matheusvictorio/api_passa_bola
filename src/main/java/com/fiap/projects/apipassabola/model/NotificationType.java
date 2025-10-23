package com.fiap.projects.apipassabola.model;

public enum NotificationType {
    // Convites de equipe
    TEAM_INVITE_RECEIVED,      // Você recebeu um convite para entrar em um time
    TEAM_INVITE_ACCEPTED,      // Seu convite foi aceito
    TEAM_INVITE_REJECTED,      // Seu convite foi rejeitado
    
    // Seguimento
    NEW_FOLLOWER,              // Alguém começou a seguir você
    
    // Posts
    POST_LIKED,                // Alguém curtiu seu post
    
    // Jogos
    GAME_INVITE_RECEIVED,      // Você recebeu um convite para um jogo
    GAME_INVITE_ACCEPTED,      // Seu convite de jogo foi aceito
    GAME_INVITE_REJECTED,      // Seu convite de jogo foi rejeitado
    
    // Time
    PLAYER_JOINED_TEAM,        // Uma jogadora entrou no seu time
    PLAYER_LEFT_TEAM,          // Uma jogadora saiu do seu time
    PLAYER_REMOVED_FROM_TEAM,  // Você foi removida do time
    
    // Sistema
    SYSTEM_ANNOUNCEMENT        // Anúncio do sistema
}
