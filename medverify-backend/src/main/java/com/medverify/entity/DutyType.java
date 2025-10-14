package com.medverify.entity;

/**
 * Types de gardes pour pharmacies
 */
public enum DutyType {
    WEEKEND("Garde weekend (vendredi soir - lundi matin)"),
    HOLIDAY("Garde jour férié"),
    NIGHT("Garde de nuit (20h-8h)"),
    SPECIAL("Garde exceptionnelle");

    private final String description;

    DutyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
