package org.streaming.revenuemanagement.domain.adjustment.batch.processor;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Component
@RequiredArgsConstructor
public class AdjustmentVideoDailyStatisticsProcessor implements ItemProcessor<VideoDailyStatistics, VideoDailyStatistics> {

    @Override
    public VideoDailyStatistics process(VideoDailyStatistics videoDailyStatistics) throws Exception {
        VideoStatistics videoStatistics = videoDailyStatistics.getVideoStatistics();
        long totalViews = videoStatistics.getTotalViews() + videoDailyStatistics.getDailyViews();
        long dailyViews = videoDailyStatistics.getDailyViews();
        long dailyAdViews = videoDailyStatistics.getDailyAdViews();

        long videoAdjustment = calculateViewAdjustment(totalViews, dailyViews);
        long adAdjustment = calculateAdAdjustment(totalViews, dailyAdViews);

        videoDailyStatistics.updateAdjustment(videoAdjustment);
        videoDailyStatistics.updateAdAdjustment(adAdjustment);
        videoStatistics.updateAdjustment(videoAdjustment);
        videoStatistics.updateAdAdjustment(adAdjustment);

        return videoDailyStatistics;
    }

    private long calculateViewAdjustment(long totalViews, long dailyViews) {
        long adjustment = 0;
        if (totalViews < 100_000) {
            adjustment += Math.min(100_000 - totalViews, dailyViews) * 1;
            dailyViews -= Math.min(100_000 - totalViews, dailyViews);
            totalViews = Math.min(100_000, totalViews + dailyViews);
        }
        if (totalViews >= 100_000 && totalViews < 500_000 && dailyViews > 0) {
            adjustment += Math.min(500_000 - totalViews, dailyViews) * 1.1;
            dailyViews -= Math.min(500_000 - totalViews, dailyViews);
            totalViews = Math.min(500_000, totalViews + dailyViews);
        }
        if (totalViews >= 500_000 && totalViews < 1_000_000 && dailyViews > 0) {
            adjustment += Math.min(1_000_000 - totalViews, dailyViews) * 1.3;
            dailyViews -= Math.min(1_000_000 - totalViews, dailyViews);
            totalViews = Math.min(1_000_000, totalViews + dailyViews);
        }
        if (totalViews >= 1_000_000 && dailyViews > 0) {
            adjustment += dailyViews * 1.5;
        }
        return (long) Math.floor(adjustment);
    }

    private long calculateAdAdjustment(long totalViews, long dailyAdViews) {
        long adjustment = 0;
        if (totalViews < 100_000) {
            adjustment += Math.min(100_000 - totalViews, dailyAdViews) * 10;
            dailyAdViews -= Math.min(100_000 - totalViews, dailyAdViews);
            totalViews = Math.min(100_000, totalViews + dailyAdViews);
        }
        if (totalViews >= 100_000 && totalViews < 500_000 && dailyAdViews > 0) {
            adjustment += Math.min(500_000 - totalViews, dailyAdViews) * 12;
            dailyAdViews -= Math.min(500_000 - totalViews, dailyAdViews);
            totalViews = Math.min(500_000, totalViews + dailyAdViews);
        }
        if (totalViews >= 500_000 && totalViews < 1_000_000 && dailyAdViews > 0) {
            adjustment += Math.min(1_000_000 - totalViews, dailyAdViews) * 15;
            dailyAdViews -= Math.min(1_000_000 - totalViews, dailyAdViews);
            totalViews = Math.min(1_000_000, totalViews + dailyAdViews);
        }
        if (totalViews >= 1_000_000 && dailyAdViews > 0) {
            adjustment += dailyAdViews * 20;
        }
        return (long) Math.floor(adjustment);
    }
}
