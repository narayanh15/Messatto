package com.messatto.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.messatto.domain.entity.AppUser;
import com.messatto.domain.entity.Meal;
import com.messatto.domain.entity.MealAttendance;
import com.messatto.domain.enums.MealChoice;
import com.messatto.domain.enums.MealType;
import com.messatto.domain.enums.Role;
import com.messatto.dto.attendance.ScanRequest;
import com.messatto.exception.ConflictException;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.UserRepository;
import com.messatto.security.UserPrincipal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-06-19T07:30:00Z"), ZoneId.of("UTC"));

    @Mock
    private MealAttendanceRepository attendanceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QrTokenService qrTokenService;

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(attendanceRepository, userRepository, qrTokenService, CLOCK);
    }

    @Test
    void scanReturnsExistingAttendanceForSameChoice() {
        UUID studentId = UUID.randomUUID();
        AppUser student = user(studentId, Role.STUDENT);
        Meal meal = meal(UUID.randomUUID());
        MealAttendance existing = attendance(student, meal, MealChoice.VEG);
        UserPrincipal principal = new UserPrincipal(studentId, "student@example.com", "hash", Role.STUDENT, true);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(qrTokenService.validateToken("qr-token")).thenReturn(new QrTokenService.ValidatedQrToken(meal, MealChoice.VEG));
        when(attendanceRepository.findByStudent_IdAndMeal_Id(studentId, meal.getId())).thenReturn(Optional.of(existing));

        var response = attendanceService.scan(principal, new ScanRequest("qr-token"));

        assertThat(response.alreadyRecorded()).isTrue();
        assertThat(response.mealChoice()).isEqualTo(MealChoice.VEG);
        verify(attendanceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void scanRejectsSecondChoiceForSameMeal() {
        UUID studentId = UUID.randomUUID();
        AppUser student = user(studentId, Role.STUDENT);
        Meal meal = meal(UUID.randomUUID());
        MealAttendance existing = attendance(student, meal, MealChoice.VEG);
        UserPrincipal principal = new UserPrincipal(studentId, "student@example.com", "hash", Role.STUDENT, true);

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(qrTokenService.validateToken("qr-token")).thenReturn(new QrTokenService.ValidatedQrToken(meal, MealChoice.NON_VEG));
        when(attendanceRepository.findByStudent_IdAndMeal_Id(studentId, meal.getId())).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> attendanceService.scan(principal, new ScanRequest("qr-token")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("different meal choice");
        verify(attendanceRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private AppUser user(UUID id, Role role) {
        AppUser user = new AppUser(
                role.name() + " User",
                role.name().toLowerCase() + "@example.com",
                role == Role.STUDENT ? "ROLL-1" : null,
                "hash",
                role,
                "Hostel A",
                true
        );
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Meal meal(UUID id) {
        Meal meal = new Meal(
                LocalDate.of(2026, 6, 19),
                MealType.BREAKFAST,
                "Idli, sambar",
                "Egg curry",
                LocalTime.of(7, 0),
                LocalTime.of(9, 0),
                user(UUID.randomUUID(), Role.MESS_ADMIN)
        );
        ReflectionTestUtils.setField(meal, "id", id);
        return meal;
    }

    private MealAttendance attendance(AppUser student, Meal meal, MealChoice choice) {
        MealAttendance attendance = new MealAttendance(student, meal, choice, Instant.now(CLOCK));
        ReflectionTestUtils.setField(attendance, "id", UUID.randomUUID());
        return attendance;
    }
}
