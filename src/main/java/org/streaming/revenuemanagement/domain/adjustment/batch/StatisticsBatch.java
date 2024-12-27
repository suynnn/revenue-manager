package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.streaming.revenuemanagement.domain.adjustment.batch.dto.VideoStatisticsUpdateDto;
import org.streaming.revenuemanagement.domain.adjustment.batch.processor.*;
import org.streaming.revenuemanagement.domain.adjustment.batch.reader.*;
import org.streaming.revenuemanagement.domain.adjustment.batch.writer.*;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;
import org.streaming.revenuemanagement.domain.videodailystatistics.repository.VideoDailyStatisticsRepository;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;
import org.streaming.revenuemanagement.domain.videolog.entity.VideoLog;
import org.streaming.revenuemanagement.domain.videostatistics.entity.VideoStatistics;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsBatch {

    private final JobRepository jobRepository;
    private final VideoDailyStatisticsRepository videoDailyStatisticsRepository;
    private final PlatformTransactionManager platformTransactionManager;

    private final RedisVideoLogReader redisVideoLogReader;
    private final VideoStatisticsReader videoStatisticsReader;
    private final VideoLogReader videoLogReader;

    @Autowired
    @Qualifier("videoDailyStatisticsPartitionReader")
    private JpaCursorItemReader<VideoDailyStatistics> videoDailyStatisticsPartitionReader;

    private final VideoDailyStatisticsReader videoDailyStatisticsReader;
    private final AdjustmentVideoDailyStatisticsReader adjustmentVideoDailyStatisticsReader;

    private final RedisVideoLogProcessor redisVideoLogProcessor;
    private final VideoLogStatisticsProcessor videoLogStatisticsProcessor;
    private final VideoStatisticsProcessor videoStatisticsProcessor;
    private final VideoLogStatisticsPartitionProcessor videoLogStatisticsPartitionProcessor;
    private final VideoDailyStatisticsProcessor videoDailyStatisticsProcessor;
    private final AdjustmentVideoDailyStatisticsProcessor adjustmentVideoDailyStatisticsProcessor;

    private final RedisVideoLogWriter redisVideoLogWriter;
    private final Step1VideoDailyStatisticsWriter step1VideoDailyStatisticsWriter;
    private final Step2VideoDailyStatisticsWriter step2VideoDailyStatisticsWriter;
    private final VideoDailyStatisticsPartitionWriter videoDailyStatisticsPartitionWriter;
    private final VideoStatisticsWriter videoStatisticsWriter;
    private final AdjustmentVideoDailyStatisticsWriter adjustmentVideoDailyStatisticsWriter;

    private final RedisTemplate<String, Object> redisTemplate;

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
        return new VideoStatisticsPartitioner(videoDailyStatisticsRepository, startDate, endDate);
    }

    @Bean
    public Job statisticsJob() {
        return new JobBuilder("statisticsJob", jobRepository)
                .start(storeRedisLogsToDatabaseStep())
                .next(deleteRedisLogsStep())
                .next(statisticsStep1())
//                .next(statisticsStep2())
                .next(partitionMasterStep())
                .next(statisticsStep3())
                .next(adjustmentStep1())
                .build();
    }

    @Bean
    public Step storeRedisLogsToDatabaseStep() {
        return new StepBuilder("storeRedisLogsToDatabaseStep", jobRepository)
                .<VideoLogReqDto, VideoLog>chunk(chunkSize, platformTransactionManager)
                .reader(redisVideoLogReader)
                .processor(redisVideoLogProcessor)
                .writer(redisVideoLogWriter)
                .build();
    }

    @Bean
    public Step deleteRedisLogsStep() {
        return new StepBuilder("deleteRedisLogsStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                            .getConnection()
                            .scan(ScanOptions.scanOptions()
                                    .match("log:video:*:date:" +
                                            LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "*")
                                    .count(1000)
                                    .build());

                    while (cursor.hasNext()) {
                        String key = new String(cursor.next());
                        redisTemplate.delete(key);
                    }

                    return RepeatStatus.FINISHED;

                }, platformTransactionManager)
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
    public Step statisticsStep2() {
        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLog, VideoStatisticsUpdateDto>chunk(chunkSize, platformTransactionManager)
                .reader(videoLogReader.reader())
                .processor(videoLogStatisticsProcessor)
                .writer(step2VideoDailyStatisticsWriter)
                .build();
    }

    @Bean
    public TaskExecutorPartitionHandler partitionHandler(Step workerStep) {
        TaskExecutorPartitionHandler partitionHandler = new TaskExecutorPartitionHandler();
        partitionHandler.setStep(workerStep);
        partitionHandler.setTaskExecutor(executor());
        partitionHandler.setGridSize(pool);
        return partitionHandler;
    }

    @Bean
    public Step partitionMasterStep() {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("workerStep", partitioner(null, null))
                .partitionHandler(partitionHandler(workerStep()))
                .build();
    }

    @Bean
    public Step workerStep() {
        return new StepBuilder("workerStep", jobRepository)
                .<VideoDailyStatistics, VideoStatisticsUpdateDto>chunk(chunkSize, platformTransactionManager)
                .reader(videoDailyStatisticsPartitionReader)
                .processor(videoLogStatisticsPartitionProcessor)
                .writer(videoDailyStatisticsPartitionWriter)
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