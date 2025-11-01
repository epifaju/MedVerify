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
        if (dayOfWeek == null) {
            return null;
        }
        
        switch (dayOfWeek) {
            case MONDAY:
                return monday;
            case TUESDAY:
                return tuesday;
            case WEDNESDAY:
                return wednesday;
            case THURSDAY:
                return thursday;
            case FRIDAY:
                return friday;
            case SATURDAY:
                return saturday;
            case SUNDAY:
                return sunday;
            default:
                return null;
        }
    }
}

