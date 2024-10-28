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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
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

    @Bean
    public Job statisticsJob() {

        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep1())
                .next(statisticsStep2())
                .next(statisticsStep3())
                .build();
    }

    @Bean
    public Step statisticsStep1() {

        return new StepBuilder("statisticsStep1", jobRepository)
                .<VideoStatistics, VideoDailyStatistics> chunk(10, platformTransactionManager)
                .reader(videoStatisticsReader())
                .processor(videoStatisticsProcessor())
                .writer(videoDailyStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoStatistics> videoStatisticsReader() {

        return new RepositoryItemReaderBuilder<VideoStatistics>()
                .name("videoStatisticsReader")
                .pageSize(10)
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
                .<VideoLog, VideoLog> chunk(10, platformTransactionManager)
                .reader(videoLogReader())
                .writer(videoLogVideoDailyStatisticsWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoLog> videoLogReader() {

        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

        return new RepositoryItemReaderBuilder<VideoLog>()
                .name("videoLogReader")
                .arguments(List.of(startOfYesterday, endOfYesterday)) // 날짜 범위와 페이징 정보 전달
                .pageSize(10)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoLogRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemWriter<VideoLog> videoLogVideoDailyStatisticsWriter() {

        return new ItemWriter<VideoLog>() {
            @Override
            public void write(Chunk<? extends VideoLog> chunk) throws Exception {
                // In-Memory 집계 데이터 구조
                Map<Long, VideoDailyStatistics> statisticsMap = new HashMap<>();

                // VideoLog 데이터를 처리하여 VideoDailyStatistics에 누적
                for (VideoLog videoLog : chunk) {
                    Long videoId = videoLog.getVideo().getId();

                    // 기존에 statisticsMap에 있으면 가져오고 없으면 DB에서 조회 또는 생성
                    VideoDailyStatistics videoDailyStatistics = statisticsMap.computeIfAbsent(videoId, id ->
                            videoDailyStatisticsRepository.findByVideoId(videoId).orElseThrow()
                    );

                    // 누적 값 계산
                    videoDailyStatistics.updateStatistics(1L, Long.valueOf(videoLog.getAdCnt()), videoLog.getPlayTime());

                    // 맵에 저장하여 중복 조회 방지
                    statisticsMap.put(videoId, videoDailyStatistics);
                }

                // 데이터베이스에 일괄 저장
                videoDailyStatisticsRepository.saveAll(statisticsMap.values());
            }
        };
    }

    @Bean
    public Step statisticsStep3() {

        return new StepBuilder("statisticsStep3", jobRepository)
                .<VideoDailyStatistics, VideoStatistics> chunk(10, platformTransactionManager)
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
                .pageSize(10)
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
}
