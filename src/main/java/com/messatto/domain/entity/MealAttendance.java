package com.messatto.domain.entity;

import com.messatto.domain.enums.MealChoice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "meal_attendance")
public class MealAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_choice", nullable = false, length = 32)
    private MealChoice mealChoice;

    @Column(name = "scanned_at", nullable = false)
    private Instant scannedAt;

    protected MealAttendance() {
    }

    public MealAttendance(AppUser student, Meal meal, MealChoice mealChoice, Instant scannedAt) {
        this.student = student;
        this.meal = meal;
        this.mealChoice = mealChoice;
        this.scannedAt = scannedAt;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getStudent() {
        return student;
    }

    public Meal getMeal() {
        return meal;
    }

    public MealChoice getMealChoice() {
        return mealChoice;
    }

    public Instant getScannedAt() {
        return scannedAt;
    }
}
