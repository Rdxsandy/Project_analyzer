package com.codeguardian.analyzerservice.model;

public enum Language {
    JAVA,
    PYTHON,
    JAVASCRIPT;

    public static Language fromString(String value) {
        if (value == null || value.isBlank()) {
            return JAVA;
        }
        try {
            return Language.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return JAVA;
        }
    }
}
