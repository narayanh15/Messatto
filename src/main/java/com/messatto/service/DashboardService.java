package com.messatto.service;

import com.messatto.domain.entity.Meal;
import com.messatto.domain.enums.MealChoice;
import com.messatto.domain.enums.MealType;
import com.messatto.dto.admin.DashboardMealStatsResponse;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.MealRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final MealRepository mealRepository;
    private final MealAttendanceRepository attendanceRepository;
    private final Clock clock;

    public DashboardService(MealRepository mealRepository, MealAttendanceRepository attendanceRepository, Clock clock) {
        this.mealRepository = mealRepository;
        this.attendanceRepository = attendanceRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<DashboardMealStatsResponse> liveDashboard(LocalDate date, MealType mealType) {
        LocalDate dashboardDate = date == null ? LocalDate.now(clock) : date;
        List<Meal> meals = mealType == null
                ? mealRepository.findByMealDateOrderByStartTimeAsc(dashboardDate)
                : mealRepository.findByMealDateAndMealType(dashboardDate, mealType).stream().toList();

        return meals.stream()
                .map(this::toStats)
                .toList();
    }

    private DashboardMealStatsResponse toStats(Meal meal) {
        long vegCount = attendanceRepository.countByMeal_IdAndMealChoice(meal.getId(), MealChoice.VEG);
        long nonVegCount = attendanceRepository.countByMeal_IdAndMealChoice(meal.getId(), MealChoice.NON_VEG);
        return new DashboardMealStatsResponse(
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                attendanceRepository.countByMeal_Id(meal.getId()),
                vegCount,
                nonVegCount
        );
    }
}
