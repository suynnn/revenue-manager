package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.AdjustmentVideoDailyStatisticsProcessor;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.VideoDailyStatisticsProcessor;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.VideoLogStatisticsProcessor;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.VideoStatisticsProcessor;
import org.streaming.revenuemanagement.domain.adjustment.batch.reader.AdjustmentVideoDailyStatisticsReader;
import org.streaming.revenuemanagement.domain.adjustment.batch.reader.VideoDailyStatisticsReader;
import org.streaming.revenuemanagement.domain.adjustment.batch.reader.VideoStatisticsReader;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.AdjustmentVideoDailyStatisticsWriter;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.VideoDailyStatisticsWriter;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.VideoStatisticsWriter;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

@Configuration
@RequiredArgsConstructor
public class StatisticsBatch {

    private final JobRepository jobRepository;
    private final VideoStatisticsRepository videoStatisticsRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final VideoStatisticsReader videoStatisticsReader;

    private final RepositoryItemReader<VideoLog> videoLogPartitionReaderMethod;
    private final VideoDailyStatisticsReader videoDailyStatisticsReader;
    private final AdjustmentVideoDailyStatisticsReader adjustmentVideoDailyStatisticsReader;

    private final VideoStatisticsProcessor videoStatisticsProcessor;
    private final VideoLogStatisticsProcessor videoLogStatisticsProcessor;
    private final VideoDailyStatisticsProcessor videoDailyStatisticsProcessor;
    private final AdjustmentVideoDailyStatisticsProcessor adjustmentVideoDailyStatisticsProcessor;

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

    @Bean
    public Step statisticsStep1() {
        return new StepBuilder("statisticsStep1", jobRepository)
                .<VideoStatistics, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(videoStatisticsReader.reader(chunkSize))
                .processor(videoStatisticsProcessor)
                .writer(videoDailyStatisticsWriter)
                .build();
    }

    @Bean
    public Step step2Manager() {
        return new StepBuilder("statisticsStep2.manager", jobRepository)
                .partitioner("statisticsStep2", partitioner())
                .gridSize(pool)
                .step(statisticsStep2())
                .taskExecutor(executor())
                .build();
    }

    @Bean
    public Step statisticsStep2() {
        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLog, VideoDailyStatistics>chunk(chunkSize, platformTransactionManager)
                .reader(videoLogPartitionReaderMethod)
                .processor(videoLogStatisticsProcessor)
                .writer(videoDailyStatisticsWriter)
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