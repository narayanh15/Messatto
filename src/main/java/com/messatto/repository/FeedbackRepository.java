package com.messatto.repository;

import com.messatto.domain.entity.Feedback;
import com.messatto.repository.projection.FeedbackStatsProjection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Optional<Feedback> findByStudent_IdAndMeal_Id(UUID studentId, UUID mealId);

    boolean existsByStudent_IdAndMeal_Id(UUID studentId, UUID mealId);

    List<Feedback> findByMeal_IdOrderByCreatedAtDesc(UUID mealId);

    List<Feedback> findByMeal_MealDate(LocalDate mealDate);

    @Query("""
            select count(f) as totalFeedback,
                   avg(f.tasteRating) as averageTasteRating,
                   avg(f.hygieneRating) as averageHygieneRating,
                   avg(f.quantityRating) as averageQuantityRating,
                   avg(f.serviceRating) as averageServiceRating
            from Feedback f
            where f.meal.id = :mealId
            """)
    FeedbackStatsProjection aggregateForMeal(@Param("mealId") UUID mealId);
}
