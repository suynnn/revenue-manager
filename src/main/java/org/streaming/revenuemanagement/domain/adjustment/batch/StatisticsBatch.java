package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.*;
import org.streaming.revenuemanagement.domain.adjustment.batch.reader.*;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.AdjustmentVideoDailyStatisticsWriter;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.Step1VideoDailyStatisticsWriter;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.VideoDailyStatisticsWriter;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.VideoStatisticsWriter;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videolog.repository.VideoLogRepository;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsBatch {

    private final JobRepository jobRepository;
    private final VideoLogRepository videoLogRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final VideoStatisticsReader videoStatisticsReader;

    private final VideoLogReader videoLogReader;
    private final DummyReader dummyReader;

    @Autowired
    @Qualifier("videoLogPartitionReaderMethod")
    private RepositoryItemReader<VideoLog> videoLogPartitionReaderMethod;

    private final VideoDailyStatisticsReader videoDailyStatisticsReader;
    private final AdjustmentVideoDailyStatisticsReader adjustmentVideoDailyStatisticsReader;

    private final VideoLogStatisticsProcessor videoLogStatisticsProcessor;
    private final VideoStatisticsProcessor videoStatisticsProcessor;
    private final VideoLogStatisticsPartitionProcessor videoLogStatisticsPartitionProcessor;
    private final VideoDailyStatisticsProcessor videoDailyStatisticsProcessor;
    private final AdjustmentVideoDailyStatisticsProcessor adjustmentVideoDailyStatisticsProcessor;

    private final Step1VideoDailyStatisticsWriter step1VideoDailyStatisticsWriter;
    private final VideoDailyStatisticsWriter videoDailyStatisticsWriter;
    private final VideoStatisticsWriter videoStatisticsWriter;
    private final AdjustmentVideoDailyStatisticsWriter adjustmentVideoDailyStatisticsWriter;

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
    public VideoStatisticsPartitioner partitioner(
            @Value("#{jobParameters['startDate']}") String startDate,
            @Value("#{jobParameters['endDate']}") String endDate
    ) {
        return new VideoStatisticsPartitioner(videoLogRepository, startDate, endDate);
    }

    @Bean
    public Job statisticsJob() {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(statisticsStep1())
                .next(step2Manager())
                .next(statisticsStep2Final())
//                .next(statisticsStep2NotUsedPartitioner())
                .next(statisticsStep3())
                .next(adjustmentStep1())
                .build();
    }

    @Bean
    public Step statisticsStep1() {
        return new StepBuilder("statisticsStep1", jobRepository)
                .<VideoStatistics, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(videoStatisticsReader.reader(chunkSize))
                .processor(videoStatisticsProcessor)
                .writer(step1VideoDailyStatisticsWriter)
                .build();
    }

    @Bean
    public Step statisticsStep2NotUsedPartitioner() {
        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLog, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(videoLogReader.reader())
                .processor(videoLogStatisticsProcessor)
                .writer(step1VideoDailyStatisticsWriter)
                .build();
    }

    @Bean
    public Step step2Manager() {
        return new StepBuilder("statisticsStep2.manager", jobRepository)
                .partitioner("statisticsStep2", partitioner(null, null))
                .gridSize(pool)
                .step(statisticsStep2())
                .taskExecutor(executor())
                .build();
    }

    @Bean
    public Step statisticsStep2() {
        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLog, Void>chunk(chunkSize, platformTransactionManager)
                .reader(videoLogPartitionReaderMethod)
                .processor(videoLogStatisticsPartitionProcessor)
                .writer(items -> {})
                .build();
    }

    @Bean
    public Step statisticsStep2Final() {
        return new StepBuilder("statisticsStep2Final", jobRepository)
                .<VideoDailyStatistics, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(dummyReader)
                .writer(videoDailyStatisticsWriter) // Map에 저장된 데이터를 한 번에 저장
                .build();
    }

    @Bean
    public Step statisticsStep3() {
        return new StepBuilder("statisticsStep3", jobRepository)
                .<VideoDailyStatistics, VideoStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(videoDailyStatisticsReader.reader(chunkSize))
                .processor(videoDailyStatisticsProcessor)
                .writer(videoStatisticsWriter)
                .build();
    }

    @Bean
    public Step adjustmentStep1() {
        return new StepBuilder("adjustmentStep1", jobRepository)
                .<VideoDailyStatistics, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(adjustmentVideoDailyStatisticsReader.reader(chunkSize))
                .processor(adjustmentVideoDailyStatisticsProcessor)
                .writer(adjustmentVideoDailyStatisticsWriter)
                .build();
    }
}