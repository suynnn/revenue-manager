package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogStatisticsRespDto;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
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

    @Value("${spring.batch.pool.size}")
    private int pool;

    @Bean(name = "statisticsJob_taskPool")
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(pool);
        executor.setMaxPoolSize(pool);
        executor.setThreadNamePrefix("partition-thread");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean(name = "statisticsJob_partitioner")
    @JobScope
    public VideoStatisticsPartitioner partitioner() {
        return new VideoStatisticsPartitioner(videoStatisticsRepository);
    }

    @Bean
    public Job statisticsJob() {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep1())
                .next(step2Manager())
                .next(statisticsStep3())
                .next(adjustmentStep1())
                .build();
    }

    // Step1: VideoStatistics 별로 전날 조회수, 광고 조회수, 재생시간, 정산 데이터를 집계할 VideoDailyStatistics 객체 생성
    @Bean
    public Step statisticsStep1() {
        return createStep("statisticsStep1", videoStatisticsReader(), videoStatisticsProcessor(), videoDailyStatisticsWriter());
    }

    @Bean
    public Step step2Manager() {
        return new StepBuilder("statisticsStep2.manager", jobRepository)
                .partitioner("statisticsStep2", partitioner())
                .step(statisticsStep2())
                .taskExecutor(executor())
                .build();
    }

    @Bean
    public Step statisticsStep2() {
        return createStep("statisticsStep2", videoStatisticsPartitionReader(null, null), videoLogStatisticsProcessor(), videoLogVideoDailyStatisticsWriter());
    }

    @Bean
    public Step statisticsStep3() {
        return createStep("statisticsStep3", videoDailyStatisticsReader(), videoDailyStatisticsProcessor(), videoStatisticsWriter());
    }

    @Bean
    public Step adjustmentStep1() {
        return createStep("adjustmentStep1", adjustmentVideoDailyStatisticsReader(), adjustmentVideoDailyStatisticsProcessor(), adjustmentVideoDailyStatisticsWriter());
    }

    private <I, O> Step createStep(String stepName, RepositoryItemReader<I> reader, ItemProcessor<I, O> processor, ItemWriter<O> writer) {
        return new StepBuilder(stepName, jobRepository)
                .<I, O>chunk(chunkSize, platformTransactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public RepositoryItemReader<VideoStatistics> videoStatisticsReader() {
        return createReader("videoStatisticsReader", List.of(), "findAll", videoStatisticsRepository);
    }

    @Bean
    @StepScope
    public RepositoryItemReader<VideoStatistics> videoStatisticsPartitionReader(@Value("#{stepExecutionContext[minId]}") Long minId,
                                                                                @Value("#{stepExecutionContext[maxId]}") Long maxId) {
        log.info("Partition Reader initialized with minId: {} and maxId: {}", minId, maxId);
        return createReader("videoStatisticsPartitionReader", List.of(minId, maxId), "findByIdBetween", videoStatisticsRepository);
    }

    @Bean
    public RepositoryItemReader<VideoDailyStatistics> videoDailyStatisticsReader() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return createReader("videoDailyStatisticsReader", List.of(startOfDay, endOfDay), "findAllByCreatedAtBetween", videoDailyStatisticsRepository);
    }

    @Bean
    public RepositoryItemReader<VideoDailyStatistics> adjustmentVideoDailyStatisticsReader() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        return createReader("adjustmentVideoDailyStatisticsReader", List.of(startOfDay, endOfDay), "findAllByCreatedAtBetween", videoDailyStatisticsRepository);
    }

    private <T, R extends PagingAndSortingRepository<T, ?>> RepositoryItemReader<T> createReader(String readerName, List<Object> arguments, String methodName, R repository) {
        return new RepositoryItemReaderBuilder<T>()
                .name(readerName)
                .arguments(arguments)
                .pageSize(chunkSize)
                .methodName(methodName)
                .repository(repository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<VideoStatistics, VideoDailyStatistics> videoStatisticsProcessor() {
        return videoStatistics -> VideoDailyStatistics.builder()
                .videoStatistics(videoStatistics)
                .videoId(videoStatistics.getVideo().getId())
                .build();
    }

    @Bean
    public ItemProcessor<VideoStatistics, VideoDailyStatistics> videoLogStatisticsProcessor() {
        return videoStatistics -> {
            Long videoId = videoStatistics.getVideo().getId();
            LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfYesterday = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59);

            Optional<VideoLogStatisticsRespDto> videoLogStats = videoLogRepository.findVideoStatisticsByVideoIdAndDateRange(videoId, startOfYesterday, endOfYesterday);

            if (videoLogStats.isPresent()) {
                VideoLogStatisticsRespDto stats = videoLogStats.get();
                log.info("Found log stats for Video ID {}: Views {}, Ad Views {}, Play Time {}", videoId, stats.getViews(), stats.getAdViews(), stats.getPlayTime());
                VideoDailyStatistics videoDailyStatistics = videoDailyStatisticsRepository.findByVideoId(videoId)
                        .orElseGet(() -> VideoDailyStatistics.builder().videoId(videoId).build());
                videoDailyStatistics.updateStatistics(stats.getViews(), stats.getAdViews(), stats.getPlayTime());
                return videoDailyStatistics;
            } else {
                log.info("No log stats found for Video ID: {}", videoId);
            }
            return null;
        };
    }

    @Bean
    public ItemProcessor<VideoDailyStatistics, VideoStatistics> videoDailyStatisticsProcessor() {
        return videoDailyStatistics -> {
            VideoStatistics videoStatistics = videoStatisticsRepository.findById(videoDailyStatistics.getVideoStatistics().getId()).orElseThrow();
            videoStatistics.updateStatistics(videoDailyStatistics.getDailyViews(), videoDailyStatistics.getDailyAdViews(), videoDailyStatistics.getDailyPlayTime());
            return videoStatistics;
        };
    }

    @Bean
    public ItemProcessor<VideoDailyStatistics, VideoDailyStatistics> adjustmentVideoDailyStatisticsProcessor() {
        return videoDailyStatistics -> {
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
        };
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

    @Bean
    public RepositoryItemWriter<VideoDailyStatistics> videoDailyStatisticsWriter() {
        return createWriter(videoDailyStatisticsRepository);
    }

    @Bean
    public RepositoryItemWriter<VideoStatistics> videoStatisticsWriter() {
        return createWriter(videoStatisticsRepository);
    }

    @Bean
    public ItemWriter<VideoDailyStatistics> videoLogVideoDailyStatisticsWriter() {
        return chunk -> videoDailyStatisticsRepository.saveAll(chunk.getItems());
    }

    @Bean
    public ItemWriter<VideoDailyStatistics> adjustmentVideoDailyStatisticsWriter() {
        return items -> {
            List<VideoDailyStatistics> updatedVideoDailyStatistics = new ArrayList<>();
            List<VideoStatistics> updatedVideoStatistics = new ArrayList<>();

            for (VideoDailyStatistics dailyStatistics : items) {
                updatedVideoDailyStatistics.add(dailyStatistics);
                VideoStatistics videoStatistics = dailyStatistics.getVideoStatistics();
                updatedVideoStatistics.add(videoStatistics);
            }
            saveAllVideoData(updatedVideoDailyStatistics, updatedVideoStatistics);
        };
    }


    private <T, R extends CrudRepository<T, ?>> RepositoryItemWriter<T> createWriter(R repository) {
        return new RepositoryItemWriterBuilder<T>()
                .repository(repository)
                .methodName("save")
                .build();
    }

    @Transactional
    public void saveAllVideoData(List<VideoDailyStatistics> videoDailyStatisticsList, List<VideoStatistics> videoStatisticsList) {
        videoDailyStatisticsRepository.saveAll(videoDailyStatisticsList);
        videoStatisticsRepository.saveAll(videoStatisticsList);
    }
}
