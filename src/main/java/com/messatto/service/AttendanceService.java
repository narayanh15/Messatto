package com.messatto.service;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.entity.Meal;
import com.messatto.domain.entity.MealAttendance;
import com.messatto.domain.enums.MealChoice;
import com.messatto.domain.enums.Role;
import com.messatto.dto.attendance.AttendanceHistoryResponse;
import com.messatto.dto.attendance.ScanRequest;
import com.messatto.dto.attendance.ScanResponse;
import com.messatto.exception.BadRequestException;
import com.messatto.exception.ConflictException;
import com.messatto.exception.ForbiddenException;
import com.messatto.exception.NotFoundException;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.UserRepository;
import com.messatto.security.UserPrincipal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final Logger log = LoggerFactory.getLogger(AttendanceService.class);

    private final MealAttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final QrTokenService qrTokenService;
    private final Clock clock;

    public AttendanceService(
            MealAttendanceRepository attendanceRepository,
            UserRepository userRepository,
            QrTokenService qrTokenService,
            Clock clock
    ) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
        this.qrTokenService = qrTokenService;
        this.clock = clock;
    }

    @Transactional
    public ScanResponse scan(UserPrincipal principal, ScanRequest request) {
        ensureStudent(principal);
        AppUser student = userRepository.findById(principal.getId())
                .orElseThrow(() -> new NotFoundException("Student not found"));

        QrTokenService.ValidatedQrToken validatedQrToken = qrTokenService.validateToken(request.token());
        Meal meal = validatedQrToken.meal();
        MealChoice choice = validatedQrToken.mealChoice();
        validateMealWindow(meal);

        return attendanceRepository.findByStudent_IdAndMeal_Id(student.getId(), meal.getId())
                .map(existing -> handleExistingAttendance(existing, choice))
                .orElseGet(() -> createAttendance(student, meal, choice));
    }

    @Transactional(readOnly = true)
    public List<AttendanceHistoryResponse> getHistory(UserPrincipal principal) {
        ensureStudent(principal);
        return attendanceRepository.findByStudent_IdOrderByScannedAtDesc(principal.getId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private ScanResponse createAttendance(AppUser student, Meal meal, MealChoice choice) {
        MealAttendance saved = attendanceRepository.save(new MealAttendance(student, meal, choice, Instant.now(clock)));
        log.info("Recorded attendance: attendanceId={}, studentId={}, mealId={}, choice={}", saved.getId(), student.getId(), meal.getId(), choice);
        return toScanResponse(saved, false);
    }

    private ScanResponse handleExistingAttendance(MealAttendance existing, MealChoice requestedChoice) {
        if (existing.getMealChoice() != requestedChoice) {
            throw new ConflictException("Attendance already recorded for this meal with a different meal choice");
        }
        log.info("Idempotent attendance scan: attendanceId={}, studentId={}, mealId={}", existing.getId(), existing.getStudent().getId(), existing.getMeal().getId());
        return toScanResponse(existing, true);
    }

    private void validateMealWindow(Meal meal) {
        LocalDate today = LocalDate.now(clock);
        LocalTime now = LocalTime.now(clock);
        if (!meal.getMealDate().equals(today) || now.isBefore(meal.getStartTime()) || now.isAfter(meal.getEndTime())) {
            throw new BadRequestException("Student can scan only during the valid meal window");
        }
    }

    private void ensureStudent(UserPrincipal principal) {
        if (principal.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Only students can scan meals or view student attendance");
        }
    }

    private ScanResponse toScanResponse(MealAttendance attendance, boolean alreadyRecorded) {
        Meal meal = attendance.getMeal();
        return new ScanResponse(
                attendance.getId(),
                meal.getId(),
                meal.getMealType(),
                meal.getMealDate(),
                attendance.getMealChoice(),
                attendance.getScannedAt(),
                alreadyRecorded
        );
    }

    private AttendanceHistoryResponse toHistoryResponse(MealAttendance attendance) {
        Meal meal = attendance.getMeal();
        return new AttendanceHistoryResponse(
                attendance.getId(),
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                attendance.getMealChoice(),
                attendance.getScannedAt()
        );
    }
}
