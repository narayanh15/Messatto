package com.messatto.repository;

import com.messatto.domain.entity.Meal;
import com.messatto.domain.enums.MealType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealRepository extends JpaRepository<Meal, UUID> {

    List<Meal> findByMealDateOrderByStartTimeAsc(LocalDate mealDate);

    List<Meal> findByMealDateBetweenOrderByMealDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    Optional<Meal> findByMealDateAndMealType(LocalDate mealDate, MealType mealType);
}
