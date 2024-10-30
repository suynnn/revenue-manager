package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class AdjustmentBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final VideoStatisticsRepository videoStatisticsRepository;
    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Bean
    public Job adjustmentJob() {

        return new JobBuilder("adjustmentJob", jobRepository)
                .start(adjustmentStep1())
                .build();
    }

    @Bean
    public Step adjustmentStep1() {

        return new StepBuilder("adjustmentStep1", jobRepository)
                .<VideoDailyStatistics, VideoDailyStatistics> chunk(10, platformTransactionManager)
                .reader(adjustmentVideoDailyStatisticsReader())
                .processor(adjustmentVideoDailyStatisticsProcessor())
                .writer(adjustmentVideoDailyStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoDailyStatistics> adjustmentVideoDailyStatisticsReader() {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 오늘 00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 오늘 23:59:59

        return new RepositoryItemReaderBuilder<VideoDailyStatistics>()
                .name("adjustmentVideoDailyStatisticsReader")
                .arguments(List.of(startOfDay, endOfDay))
                .pageSize(10)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoDailyStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<VideoDailyStatistics, VideoDailyStatistics> adjustmentVideoDailyStatisticsProcessor() {

        return new ItemProcessor<VideoDailyStatistics, VideoDailyStatistics>() {

            @Override
            public VideoDailyStatistics process(VideoDailyStatistics videoDailyStatistics) throws Exception {

                VideoStatistics videoStatistics = videoDailyStatistics.getVideoStatistics();
                long totalViews = videoStatistics.getTotalViews() + videoDailyStatistics.getDailyViews();
                long dailyViews = videoDailyStatistics.getDailyViews();
                long dailyAdViews = videoDailyStatistics.getDailyAdViews();

                // 영상별 단가 계산
                long videoAdjustment = calculateViewAdjustment(totalViews, dailyViews);

                // 광고별 단가 계산
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

                return (long) Math.floor(adjustment); // 1원 단위 이하 절사
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

                return (long) Math.floor(adjustment); // 1원 단위 이하 절사
            }
        };
    }

    @Bean
    public ItemWriter<VideoDailyStatistics> adjustmentVideoDailyStatisticsWriter() {
        return items -> {
            for (VideoDailyStatistics dailyStatistics : items) {
                // VideoDailyStatistics 저장
                videoDailyStatisticsRepository.save(dailyStatistics);

                // 연관된 VideoStatistics 저장
                VideoStatistics videoStatistics = dailyStatistics.getVideoStatistics();
                videoStatisticsRepository.save(videoStatistics);
            }
        };
    }
}
