package com.messatto.controller;

import com.messatto.dto.meal.MealResponse;
import com.messatto.service.MealService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<MealResponse>> today() {
        return ResponseEntity.ok(mealService.getTodayMeals());
    }
}
