package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("statisticsJob")
    private final Job statisticsJob;

    @Qualifier("adjustmentJob")
    private final Job adjustmentJob;

    public BatchScheduler(JobLauncher jobLauncher,
                          @Qualifier("statisticsJob") Job statisticsJob,
                          @Qualifier("adjustmentJob") Job adjustmentJob) {
        this.jobLauncher = jobLauncher;
        this.statisticsJob = statisticsJob;
        this.adjustmentJob = adjustmentJob;
    }

    @Scheduled(cron = "0 04 16 * * *")
    public void runStatisticsJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("currentTime", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(statisticsJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 05 16 * * *")
    public void runAdjustmentJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("currentTime", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(adjustmentJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
