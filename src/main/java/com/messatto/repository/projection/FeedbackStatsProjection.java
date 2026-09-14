package com.messatto.repository.projection;

public interface FeedbackStatsProjection {

    Long getTotalFeedback();

    Double getAverageTasteRating();

    Double getAverageHygieneRating();

    Double getAverageQuantityRating();

    Double getAverageServiceRating();
}
