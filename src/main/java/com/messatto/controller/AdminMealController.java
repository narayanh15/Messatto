package com.messatto.controller;

import com.messatto.dto.meal.CreateMealRequest;
import com.messatto.dto.meal.MealResponse;
import com.messatto.dto.qr.GenerateQrRequest;
import com.messatto.dto.qr.QrTokenResponse;
import com.messatto.security.UserPrincipal;
import com.messatto.service.MealService;
import com.messatto.service.QrTokenService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/meals")
public class AdminMealController {

    private final MealService mealService;
    private final QrTokenService qrTokenService;

    public AdminMealController(MealService mealService, QrTokenService qrTokenService) {
        this.mealService = mealService;
        this.qrTokenService = qrTokenService;
    }

    @PostMapping
    public ResponseEntity<MealResponse> createMeal(
            @Valid @RequestBody CreateMealRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        MealResponse response = mealService.createMeal(request, principal.getId());
        return ResponseEntity.created(URI.create("/admin/meals/" + response.id())).body(response);
    }

    @PostMapping("/{mealId}/qr")
    public ResponseEntity<QrTokenResponse> generateQr(
            @PathVariable UUID mealId,
            @Valid @RequestBody GenerateQrRequest request
    ) {
        return ResponseEntity.ok(qrTokenService.generateToken(mealId, request));
    }
}
