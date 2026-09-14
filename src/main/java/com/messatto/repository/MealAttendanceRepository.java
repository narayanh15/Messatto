package com.messatto.repository;

import com.messatto.domain.entity.MealAttendance;
import com.messatto.domain.enums.MealChoice;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealAttendanceRepository extends JpaRepository<MealAttendance, UUID> {

    Optional<MealAttendance> findByStudent_IdAndMeal_Id(UUID studentId, UUID mealId);

    boolean existsByStudent_IdAndMeal_Id(UUID studentId, UUID mealId);

    List<MealAttendance> findByStudent_IdOrderByScannedAtDesc(UUID studentId);

    List<MealAttendance> findByMeal_MealDate(LocalDate mealDate);

    long countByMeal_Id(UUID mealId);

    long countByMeal_IdAndMealChoice(UUID mealId, MealChoice mealChoice);
}
