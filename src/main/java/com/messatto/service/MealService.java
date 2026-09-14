package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.entity.Meal;
import com.messatto.dto.meal.CreateMealRequest;
import com.messatto.dto.meal.MealResponse;
import com.messatto.exception.BadRequestException;
import com.messatto.exception.NotFoundException;
import com.messatto.repository.MealRepository;
import com.messatto.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MealService {

    private static final Logger log = LoggerFactory.getLogger(MealService.class);

    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public MealService(MealRepository mealRepository, UserRepository userRepository, Clock clock) {
        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public MealResponse createMeal(CreateMealRequest request, UUID createdByUserId) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("endTime must be after startTime");
        }

        AppUser creator = userRepository.findById(createdByUserId)
                .orElseThrow(() -> new NotFoundException("Creator user not found"));

        Meal meal = new Meal(
                request.mealDate(),
                request.mealType(),
                request.vegMenu().trim(),
                request.nonVegMenu().trim(),
                request.startTime(),
                request.endTime(),
                creator
        );
        Meal saved = mealRepository.save(meal);
        log.info("Created meal: id={}, date={}, type={}, createdBy={}", saved.getId(), saved.getMealDate(), saved.getMealType(), creator.getEmail());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<MealResponse> getTodayMeals() {
        return mealRepository.findByMealDateOrderByStartTimeAsc(LocalDate.now(clock))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    MealResponse toResponse(Meal meal) {
        return new MealResponse(
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                meal.getVegMenu(),
                meal.getNonVegMenu(),
                meal.getStartTime(),
                meal.getEndTime(),
                meal.getCreatedBy().getName(),
                meal.getCreatedAt()
        );
    }
}
