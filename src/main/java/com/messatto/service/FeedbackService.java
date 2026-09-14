package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.entity.Feedback;
import com.messatto.domain.entity.Meal;
import com.messatto.domain.enums.Role;
import com.messatto.dto.feedback.FeedbackCommentResponse;
import com.messatto.dto.feedback.FeedbackRequest;
import com.messatto.dto.feedback.FeedbackResponse;
import com.messatto.dto.feedback.FeedbackSummaryResponse;
import com.messatto.exception.ConflictException;
import com.messatto.exception.ForbiddenException;
import com.messatto.exception.NotFoundException;
import com.messatto.repository.FeedbackRepository;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.MealRepository;
import com.messatto.repository.UserRepository;
import com.messatto.repository.projection.FeedbackStatsProjection;
import com.messatto.security.UserPrincipal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private static final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final FeedbackRepository feedbackRepository;
    private final MealAttendanceRepository attendanceRepository;
    private final MealRepository mealRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            MealAttendanceRepository attendanceRepository,
            MealRepository mealRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.feedbackRepository = feedbackRepository;
        this.attendanceRepository = attendanceRepository;
        this.mealRepository = mealRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public FeedbackResponse submitFeedback(UserPrincipal principal, FeedbackRequest request) {
        ensureStudent(principal);
        AppUser student = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        Meal meal = mealRepository.findById(request.mealId())
                .orElseThrow(() -> new NotFoundException("Meal not found"));

        if (!attendanceRepository.existsByStudent_IdAndMeal_Id(student.getId(), meal.getId())) {
            throw new ForbiddenException("Feedback is allowed only after attending the meal");
        }
        if (feedbackRepository.existsByStudent_IdAndMeal_Id(student.getId(), meal.getId())) {
            throw new ConflictException("Feedback already submitted for this meal");
        }

        Feedback saved = feedbackRepository.save(new Feedback(
                student,
                meal,
                request.tasteRating(),
                request.hygieneRating(),
                request.quantityRating(),
                request.serviceRating(),
                normalizeComment(request.comment()),
                request.anonymous()
        ));
        log.info("Recorded feedback: feedbackId={}, studentId={}, mealId={}, anonymous={}", saved.getId(), student.getId(), meal.getId(), saved.isAnonymous());
        return new FeedbackResponse(saved.getId(), meal.getId(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<FeedbackSummaryResponse> getSummary(UUID mealId, LocalDate date) {
        if (mealId != null) {
            Meal meal = mealRepository.findById(mealId)
                    .orElseThrow(() -> new NotFoundException("Meal not found"));
            return List.of(toSummary(meal));
        }

        LocalDate summaryDate = date == null ? LocalDate.now(clock) : date;
        return mealRepository.findByMealDateOrderByStartTimeAsc(summaryDate)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    private FeedbackSummaryResponse toSummary(Meal meal) {
        FeedbackStatsProjection stats = feedbackRepository.aggregateForMeal(meal.getId());
        List<FeedbackCommentResponse> comments = feedbackRepository.findByMeal_IdOrderByCreatedAtDesc(meal.getId())
                .stream()
                .map(this::toComment)
                .toList();

        long total = stats.getTotalFeedback() == null ? 0 : stats.getTotalFeedback();
        return new FeedbackSummaryResponse(
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                total,
                round(stats.getAverageTasteRating()),
                round(stats.getAverageHygieneRating()),
                round(stats.getAverageQuantityRating()),
                round(stats.getAverageServiceRating()),
                comments
        );
    }

    private FeedbackCommentResponse toComment(Feedback feedback) {
        return new FeedbackCommentResponse(
                feedback.getTasteRating(),
                feedback.getHygieneRating(),
                feedback.getQuantityRating(),
                feedback.getServiceRating(),
                feedback.getComment(),
                feedback.isAnonymous(),
                feedback.isAnonymous() ? null : feedback.getStudent().getName(),
                feedback.getCreatedAt()
        );
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }

    private void ensureStudent(UserPrincipal principal) {
        if (principal.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can submit feedback");
        }
    }

    private double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
