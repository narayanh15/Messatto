package com.messatto.dto.admin;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReportResponse(
        LocalDate startDate,
        LocalDate endDate,
        long totalAttendance,
        long vegCount,
        long nonVegCount,
        long feedbackCount,
        List<MealReportRow> meals
) {
}
