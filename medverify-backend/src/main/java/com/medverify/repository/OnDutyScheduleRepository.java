package com.medverify.repository;

import com.medverify.entity.OnDutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour gérer les plannings de garde
 */
@Repository
public interface OnDutyScheduleRepository extends JpaRepository<OnDutySchedule, UUID> {

    /**
     * Trouver gardes actives pour une date
     */
    @Query("""
                SELECT s FROM OnDutySchedule s
                WHERE s.isActive = true
                AND :date BETWEEN s.startDate AND s.endDate
                ORDER BY s.startDate DESC
            """)
    List<OnDutySchedule> findActiveSchedulesForDate(@Param("date") LocalDate date);

    /**
     * Trouver gardes par région et date
     */
    @Query("""
                SELECT s FROM OnDutySchedule s
                WHERE s.isActive = true
                AND s.region = :region
                AND :date BETWEEN s.startDate AND s.endDate
                ORDER BY s.dutyType, s.startDate
            """)
    List<OnDutySchedule> findActiveSchedulesByRegionAndDate(
            @Param("region") String region,
            @Param("date") LocalDate date);

    /**
     * Trouver pharmacies de garde actuelles dans un rayon (requête native avec
     * PostGIS)
     */
    @Query(value = """
                SELECT p.id, p.name, p.address, p.city, p.region, p.district, p.phone_number,
                       p.email, p.is_24h, p.is_night_pharmacy, p.is_active, p.photo_url, p.rating,
                       ST_X(p.location\\:\\:geometry) as longitude,
                       ST_Y(p.location\\:\\:geometry) as latitude,
                       ST_Distance(p.location\\:\\:geography, ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography) as distance,
                       s.duty_type, s.start_date, s.end_date
                FROM on_duty_schedules s
                JOIN pharmacies p ON s.pharmacy_id = p.id
                WHERE s.is_active = true
                AND p.is_active = true
                AND :date BETWEEN s.start_date AND s.end_date
                AND ST_DWithin(
                    p.location\\:\\:geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)\\:\\:geography,
                    :radiusMeters
                )
                ORDER BY distance
                LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findOnDutyPharmaciesNearby(
            @Param("date") LocalDate date,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("radiusMeters") double radiusMeters,
            @Param("limit") int limit);

    /**
     * Trouver toutes les gardes actives d'une pharmacie
     */
    @Query("""
                SELECT s FROM OnDutySchedule s
                WHERE s.pharmacy.id = :pharmacyId
                AND s.isActive = true
                ORDER BY s.startDate DESC
            """)
    List<OnDutySchedule> findActiveSchedulesByPharmacy(@Param("pharmacyId") UUID pharmacyId);
}
