package com.fiap.projects.apipassabola.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for the @ValidCnpj annotation
 */
public class CnpjValidator implements ConstraintValidator<ValidCnpj, String> {
    
    @Override
    public void initialize(ValidCnpj constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String cnpj, ConstraintValidatorContext context) {
        if (cnpj == null || cnpj.trim().isEmpty()) {
            return false;
        }
        
        return com.fiap.projects.apipassabola.util.CnpjValidator.isValid(cnpj);
    }
}
