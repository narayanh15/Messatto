package com.messatto.domain.entity;

import com.messatto.domain.enums.MealType;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "meals")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 32)
    private MealType mealType;

    @Column(name = "veg_menu", nullable = false, columnDefinition = "TEXT")
    private String vegMenu;

    @Column(name = "non_veg_menu", nullable = false, columnDefinition = "TEXT")
    private String nonVegMenu;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Meal() {
    }

    public Meal(LocalDate mealDate, MealType mealType, String vegMenu, String nonVegMenu, LocalTime startTime, LocalTime endTime, AppUser createdBy) {
        this.mealDate = mealDate;
        this.mealType = mealType;
        this.vegMenu = vegMenu;
        this.nonVegMenu = nonVegMenu;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdBy = createdBy;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getMealDate() {
        return mealDate;
    }

    public void setMealDate(LocalDate mealDate) {
        this.mealDate = mealDate;
    }

    public MealType getMealType() {
        return mealType;
    }

    public void setMealType(MealType mealType) {
        this.mealType = mealType;
    }

    public String getVegMenu() {
        return vegMenu;
    }

    public void setVegMenu(String vegMenu) {
        this.vegMenu = vegMenu;
    }

    public String getNonVegMenu() {
        return nonVegMenu;
    }

    public void setNonVegMenu(String nonVegMenu) {
        this.nonVegMenu = nonVegMenu;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public AppUser getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
