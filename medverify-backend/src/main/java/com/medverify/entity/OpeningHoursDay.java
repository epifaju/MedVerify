package com.medverify.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Représente les horaires d'ouverture pour un jour de la semaine
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpeningHoursDay implements Serializable {

    @Builder.Default
    private Boolean closed = false;

    // Horaires matin
    private LocalTime morningOpen; // Ex: 08:00
    private LocalTime morningClose; // Ex: 12:30

    // Horaires après-midi
    private LocalTime afternoonOpen; // Ex: 14:00
    private LocalTime afternoonClose; // Ex: 19:00

    // Horaires soir/nuit (pour pharmacies de nuit)
    private LocalTime eveningOpen; // Ex: 20:00
    private LocalTime eveningClose; // Ex: 23:59 ou 02:00

    /**
     * Vérifie si le jour est fermé
     */
    public boolean isClosed() {
        return closed != null && closed;
    }

    /**
     * Vérifie si la pharmacie est ouverte à une heure donnée
     */
    public boolean isOpenAt(LocalTime time) {
        if (isClosed())
            return false;

        // Vérifier horaires matin
        if (morningOpen != null && morningClose != null) {
            if (time.isAfter(morningOpen) && time.isBefore(morningClose)) {
                return true;
            }
        }

        // Vérifier horaires après-midi
        if (afternoonOpen != null && afternoonClose != null) {
            if (time.isAfter(afternoonOpen) && time.isBefore(afternoonClose)) {
                return true;
            }
        }

        // Vérifier horaires soir/nuit
        if (eveningOpen != null && eveningClose != null) {
            // Cas normal (ex: 20:00-23:59)
            if (eveningClose.isAfter(eveningOpen)) {
                if (time.isAfter(eveningOpen) && time.isBefore(eveningClose)) {
                    return true;
                }
            } else {
                // Cas horaire qui traverse minuit (ex: 22:00-02:00)
                if (time.isAfter(eveningOpen) || time.isBefore(eveningClose)) {
                    return true;
                }
            }
        }

        return false;
    }
}
