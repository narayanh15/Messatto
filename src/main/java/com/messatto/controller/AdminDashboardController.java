package com.messatto.controller;

import com.messatto.domain.enums.MealType;
import com.messatto.dto.admin.DailyReportResponse;
import com.messatto.dto.admin.DashboardMealStatsResponse;
import com.messatto.dto.admin.WeeklyReportResponse;
import com.messatto.dto.feedback.FeedbackSummaryResponse;
import com.messatto.service.DashboardService;
import com.messatto.service.FeedbackService;
import com.messatto.service.ReportService;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminDashboardController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv");

    private final DashboardService dashboardService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

    public AdminDashboardController(DashboardService dashboardService, FeedbackService feedbackService, ReportService reportService) {
        this.dashboardService = dashboardService;
        this.feedbackService = feedbackService;
        this.reportService = reportService;
    }

    @GetMapping("/dashboard/live")
    public ResponseEntity<List<DashboardMealStatsResponse>> liveDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) MealType mealType
    ) {
        return ResponseEntity.ok(dashboardService.liveDashboard(date, mealType));
    }

    @GetMapping("/feedback/summary")
    public ResponseEntity<List<FeedbackSummaryResponse>> feedbackSummary(
            @RequestParam(required = false) UUID mealId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(feedbackService.getSummary(mealId, date));
    }

    @GetMapping("/reports/daily")
    public ResponseEntity<?> dailyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "json") @Pattern(regexp = "json|csv") String format
    ) {
        DailyReportResponse report = reportService.dailyReport(date);
        if ("csv".equalsIgnoreCase(format)) {
            return ResponseEntity.ok()
                    .contentType(TEXT_CSV)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=messatto-daily-" + report.date() + ".csv")
                    .body(reportService.dailyCsv(report));
        }
        return ResponseEntity.ok(report);
    }

    @GetMapping("/reports/weekly")
    public ResponseEntity<WeeklyReportResponse> weeklyReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate
    ) {
        return ResponseEntity.ok(reportService.weeklyReport(startDate));
    }
}
