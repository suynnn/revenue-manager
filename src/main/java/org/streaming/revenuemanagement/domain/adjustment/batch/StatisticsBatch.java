package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class StatisticsBatch {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final VideoLogRepository videoLogRepository;
    private final VideoStatisticsRepository videoStatisticsRepository;
    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;

    @Value("${spring.batch.chunk.size}")
    private int chunkSize;

    @Bean
    public Job statisticsJob() {

        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep1())
                .next(statisticsStep2())
                .next(statisticsStep3())
                .next(adjustmentStep1())
                .build();
    }

    @Bean
    public Step statisticsStep1() {

        return new StepBuilder("statisticsStep1", jobRepository)
                .<VideoStatistics, VideoDailyStatistics> chunk(chunkSize, platformTransactionManager)
                .reader(videoStatisticsReader())
                .processor(videoStatisticsProcessor())
                .writer(videoDailyStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoStatistics> videoStatisticsReader() {

        return new RepositoryItemReaderBuilder<VideoStatistics>()
                .name("videoStatisticsReader")
                .pageSize(chunkSize)
                .methodName("findAll")
                .repository(videoStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<VideoStatistics, VideoDailyStatistics> videoStatisticsProcessor() {

        return new ItemProcessor<VideoStatistics, VideoDailyStatistics>() {

            @Override
            public VideoDailyStatistics process(VideoStatistics videoStatistics) throws Exception {

                return VideoDailyStatistics.builder()
                        .videoStatistics(videoStatistics)
                        .videoId(videoStatistics.getVideo().getId())
                        .build();
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoDailyStatistics> videoDailyStatisticsWriter() {

        return new RepositoryItemWriterBuilder<VideoDailyStatistics>()
                .repository(videoDailyStatisticsRepository)
                .methodName("save")
                .build();
    }


    @Bean
    public Step statisticsStep2() {

        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLogStatisticsRespDto, VideoDailyStatistics> chunk(chunkSize, platformTransactionManager)
                .reader(videoLogStatisticsReader())
                .processor(videoLogStatisticsProcessor())
                .writer(videoLogVideoDailyStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoLogStatisticsRespDto> videoLogStatisticsReader() {

        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

        return new RepositoryItemReaderBuilder<VideoLogStatisticsRespDto>()
                .name("videoLogStatisticsReader")
                .arguments(List.of(startOfYesterday, endOfYesterday))
                .pageSize(chunkSize)
                .methodName("findVideoStatisticsBetweenDates")
                .repository(videoLogRepository)
                .sorts(Map.of())  // 정렬 필요 없음
                .build();
    }

    @Bean
    public ItemProcessor<VideoLogStatisticsRespDto, VideoDailyStatistics> videoLogStatisticsProcessor() {

        return new ItemProcessor<VideoLogStatisticsRespDto, VideoDailyStatistics>() {
            @Override
            public VideoDailyStatistics process(VideoLogStatisticsRespDto item) throws Exception {
                Long videoId = item.getVideoId();
                Long views = item.getViews();
                Long adViews = item.getAdViews();
                Long playTime = item.getPlayTime();

                VideoDailyStatistics videoDailyStatistics = videoDailyStatisticsRepository.findByVideoId(videoId)
                        .orElseGet(() -> VideoDailyStatistics.builder()
                                .videoId(videoId)
                                .build());

                videoDailyStatistics.updateStatistics(views, adViews, playTime);
                return videoDailyStatistics;
            }
        };
    }

    @Bean
    public ItemWriter<VideoDailyStatistics> videoLogVideoDailyStatisticsWriter() {

        return new ItemWriter<VideoDailyStatistics>() {
            @Override
            public void write(Chunk<? extends VideoDailyStatistics> chunk) throws Exception {
                // 데이터베이스에 일괄 저장
                videoDailyStatisticsRepository.saveAll(chunk.getItems());
            }
        };
    }

    @Bean
    public Step statisticsStep3() {

        return new StepBuilder("statisticsStep3", jobRepository)
                .<VideoDailyStatistics, VideoStatistics> chunk(chunkSize, platformTransactionManager)
                .reader(videoDailyStatisticsReader())
                .processor(videoDailyStatisticsProcessor())
                .writer(videoStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoDailyStatistics> videoDailyStatisticsReader() {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay(); // 오늘 00:00:00
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX); // 오늘 23:59:59

        return new RepositoryItemReaderBuilder<VideoDailyStatistics>()
                .name("videoDailyStatisticsReader")
                .arguments(List.of(startOfDay, endOfDay)) // 날짜 범위와 페이징 정보 전달
                .pageSize(chunkSize)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoDailyStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<VideoDailyStatistics, VideoStatistics> videoDailyStatisticsProcessor() {

        return new ItemProcessor<VideoDailyStatistics, VideoStatistics>() {

            @Override
            public VideoStatistics process(VideoDailyStatistics videoDailyStatistics) throws Exception {

                VideoStatistics videoStatistics = videoStatisticsRepository.findById(videoDailyStatistics.getVideoStatistics().getId()).orElseThrow();
                videoStatistics.updateStatistics(videoDailyStatistics.getDailyViews(), videoDailyStatistics.getDailyAdViews(), videoDailyStatistics.getDailyPlayTime());

                return videoStatistics;
            }
        };
    }

    @Bean
    public RepositoryItemWriter<VideoStatistics> videoStatisticsWriter() {

        return new RepositoryItemWriterBuilder<VideoStatistics>()
                .repository(videoStatisticsRepository)
                .methodName("save")
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
