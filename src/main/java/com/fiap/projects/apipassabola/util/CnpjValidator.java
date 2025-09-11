package com.fiap.projects.apipassabola.util;

import java.util.regex.Pattern;

/**
 * Utility class for CNPJ (Cadastro Nacional da Pessoa Jurídica) validation
 * Implements the Brazilian CNPJ validation algorithm
 */
public class CnpjValidator {
    
    private static final Pattern CNPJ_PATTERN = Pattern.compile("\\d{14}");
    
    /**
     * Validates a CNPJ string
     * @param cnpj The CNPJ string to validate (should contain only digits)
     * @return true if the CNPJ is valid, false otherwise
     */
    public static boolean isValid(String cnpj) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        
        // Remove any non-digit characters
        cnpj = cnpj.replaceAll("\\D", "");
        
        // Check if it has exactly 14 digits
        if (!CNPJ_PATTERN.matcher(cnpj).matches()) {
            return false;
        }
        
        // Check for known invalid CNPJs (all same digits)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }
        
        // Calculate first check digit
        int sum = 0;
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        
        int remainder = sum % 11;
        int firstCheckDigit = remainder < 2 ? 0 : 11 - remainder;
        
        if (Character.getNumericValue(cnpj.charAt(12)) != firstCheckDigit) {
            return false;
        }
        
        // Calculate second check digit
        sum = 0;
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        
        remainder = sum % 11;
        int secondCheckDigit = remainder < 2 ? 0 : 11 - remainder;
        
        return Character.getNumericValue(cnpj.charAt(13)) == secondCheckDigit;
    }
    
    /**
     * Formats a CNPJ string with dots, slash and dash
     * @param cnpj The CNPJ string (only digits)
     * @return Formatted CNPJ (XX.XXX.XXX/XXXX-XX) or the original string if invalid
     */
    public static String format(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        
        cnpj = cnpj.replaceAll("\\D", "");
        
        if (cnpj.length() != 14) {
            return cnpj;
        }
        
        return cnpj.substring(0, 2) + "." +
               cnpj.substring(2, 5) + "." +
               cnpj.substring(5, 8) + "/" +
               cnpj.substring(8, 12) + "-" +
               cnpj.substring(12, 14);
    }
    
    /**
     * Removes formatting from CNPJ string, keeping only digits
     * @param cnpj The formatted or unformatted CNPJ string
     * @return CNPJ with only digits
     */
    public static String unformat(String cnpj) {
        if (cnpj == null) {
            return null;
        }
        return cnpj.replaceAll("\\D", "");
    }
}
