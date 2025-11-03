package com.fiap.projects.apipassabola.entity;

public enum Division {
    BRONZE(0, 29, "Bronze"),
    PRATA(30, 59, "Prata"),
    OURO(60, 99, "Ouro"),
    PLATINA(100, 149, "Platina"),
    DIAMANTE(150, 199, "Diamante"),
    MESTRE(200, 299, "Mestre"),
    LENDARIA(300, Integer.MAX_VALUE, "Lendária");
    
    private final int minPoints;
    private final int maxPoints;
    private final String displayName;
    
    Division(int minPoints, int maxPoints, String displayName) {
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.displayName = displayName;
    }
    
    public int getMinPoints() {
        return minPoints;
    }
    
    public int getMaxPoints() {
        return maxPoints;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Determina a divisão baseada nos pontos
     */
    public static Division fromPoints(int points) {
        for (Division division : Division.values()) {
            if (points >= division.minPoints && points <= division.maxPoints) {
                return division;
            }
        }
        return BRONZE; // Default
    }
    
    /**
     * Calcula pontos necessários para próxima divisão
     */
    public int getPointsToNextDivision(int currentPoints) {
        if (this == LENDARIA) {
            return 0; // Já está na divisão máxima
        }
        Division nextDivision = Division.values()[this.ordinal() + 1];
        return nextDivision.minPoints - currentPoints;
    }
}
