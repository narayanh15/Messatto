package com.messatto.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private AppUser student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(name = "taste_rating", nullable = false)
    private short tasteRating;

    @Column(name = "hygiene_rating", nullable = false)
    private short hygieneRating;

    @Column(name = "quantity_rating", nullable = false)
    private short quantityRating;

    @Column(name = "service_rating", nullable = false)
    private short serviceRating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private boolean anonymous;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Feedback() {
    }

    public Feedback(AppUser student, Meal meal, int tasteRating, int hygieneRating, int quantityRating, int serviceRating, String comment, boolean anonymous) {
        this.student = student;
        this.meal = meal;
        this.tasteRating = (short) tasteRating;
        this.hygieneRating = (short) hygieneRating;
        this.quantityRating = (short) quantityRating;
        this.serviceRating = (short) serviceRating;
        this.comment = comment;
        this.anonymous = anonymous;
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

    public int getTasteRating() {
        return tasteRating;
    }

    public int getHygieneRating() {
        return hygieneRating;
    }

    public int getQuantityRating() {
        return quantityRating;
    }

    public int getServiceRating() {
        return serviceRating;
    }

    public String getComment() {
        return comment;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
