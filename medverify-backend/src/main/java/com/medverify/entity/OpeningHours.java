package com.medverify.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.DayOfWeek;

/**
 * Représente les horaires d'ouverture hebdomadaires d'une pharmacie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHours implements Serializable {

    private OpeningHoursDay monday;
    private OpeningHoursDay tuesday;
    private OpeningHoursDay wednesday;
    private OpeningHoursDay thursday;
    private OpeningHoursDay friday;
    private OpeningHoursDay saturday;
    private OpeningHoursDay sunday;

    /**
     * Obtenir les horaires pour un jour de la semaine
     */
    public OpeningHoursDay getDay(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> monday;
            case TUESDAY -> tuesday;
            case WEDNESDAY -> wednesday;
            case THURSDAY -> thursday;
            case FRIDAY -> friday;
            case SATURDAY -> saturday;
            case SUNDAY -> sunday;
        };
    }
}

