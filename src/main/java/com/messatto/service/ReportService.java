package com.messatto.service;

import com.messatto.domain.entity.Meal;
import com.messatto.domain.enums.MealChoice;
import com.messatto.dto.admin.DailyReportResponse;
import com.messatto.dto.admin.MealReportRow;
import com.messatto.dto.admin.WeeklyReportResponse;
import com.messatto.repository.FeedbackRepository;
import com.messatto.repository.MealAttendanceRepository;
import com.messatto.repository.MealRepository;
import com.messatto.repository.projection.FeedbackStatsProjection;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final MealRepository mealRepository;
    private final MealAttendanceRepository attendanceRepository;
    private final FeedbackRepository feedbackRepository;
    private final Clock clock;

    public ReportService(
            MealRepository mealRepository,
            MealAttendanceRepository attendanceRepository,
            FeedbackRepository feedbackRepository,
            Clock clock
    ) {
        this.mealRepository = mealRepository;
        this.attendanceRepository = attendanceRepository;
        this.feedbackRepository = feedbackRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DailyReportResponse dailyReport(LocalDate date) {
        LocalDate reportDate = date == null ? LocalDate.now(clock) : date;
        List<MealReportRow> rows = mealRepository.findByMealDateOrderByStartTimeAsc(reportDate)
                .stream()
                .map(this::toReportRow)
                .toList();
        return new DailyReportResponse(
                reportDate,
                rows.stream().mapToLong(MealReportRow::totalAttendance).sum(),
                rows.stream().mapToLong(MealReportRow::vegCount).sum(),
                rows.stream().mapToLong(MealReportRow::nonVegCount).sum(),
                rows.stream().mapToLong(MealReportRow::feedbackCount).sum(),
                rows
        );
    }

    @Transactional(readOnly = true)
    public WeeklyReportResponse weeklyReport(LocalDate startDate) {
        LocalDate start = startDate == null
                ? LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                : startDate;
        LocalDate end = start.plusDays(6);
        List<MealReportRow> rows = mealRepository.findByMealDateBetweenOrderByMealDateAscStartTimeAsc(start, end)
                .stream()
                .map(this::toReportRow)
                .toList();

        return new WeeklyReportResponse(
                start,
                end,
                rows.stream().mapToLong(MealReportRow::totalAttendance).sum(),
                rows.stream().mapToLong(MealReportRow::vegCount).sum(),
                rows.stream().mapToLong(MealReportRow::nonVegCount).sum(),
                rows.stream().mapToLong(MealReportRow::feedbackCount).sum(),
                rows
        );
    }

    public String dailyCsv(DailyReportResponse report) {
        StringBuilder csv = new StringBuilder("meal_id,date,meal_type,total_attendance,veg_count,non_veg_count,feedback_count,avg_taste,avg_hygiene,avg_quantity,avg_service\n");
        for (MealReportRow row : report.meals()) {
            csv.append(row.mealId()).append(',')
                    .append(row.mealDate()).append(',')
                    .append(row.mealType()).append(',')
                    .append(row.totalAttendance()).append(',')
                    .append(row.vegCount()).append(',')
                    .append(row.nonVegCount()).append(',')
                    .append(row.feedbackCount()).append(',')
                    .append(row.averageTasteRating()).append(',')
                    .append(row.averageHygieneRating()).append(',')
                    .append(row.averageQuantityRating()).append(',')
                    .append(row.averageServiceRating()).append('\n');
        }
        return csv.toString();
    }

    private MealReportRow toReportRow(Meal meal) {
        long vegCount = attendanceRepository.countByMeal_IdAndMealChoice(meal.getId(), MealChoice.VEG);
        long nonVegCount = attendanceRepository.countByMeal_IdAndMealChoice(meal.getId(), MealChoice.NON_VEG);
        FeedbackStatsProjection stats = feedbackRepository.aggregateForMeal(meal.getId());
        long feedbackCount = stats.getTotalFeedback() == null ? 0 : stats.getTotalFeedback();

        return new MealReportRow(
                meal.getId(),
                meal.getMealDate(),
                meal.getMealType(),
                attendanceRepository.countByMeal_Id(meal.getId()),
                vegCount,
                nonVegCount,
                feedbackCount,
                round(stats.getAverageTasteRating()),
                round(stats.getAverageHygieneRating()),
                round(stats.getAverageQuantityRating()),
                round(stats.getAverageServiceRating())
        );
    }

    private double round(Double value) {
        if (value == null) {
            return 0.0;
        }
        return Math.round(value * 100.0) / 100.0;
    }
}
