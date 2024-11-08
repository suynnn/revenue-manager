package org.streaming.revenuemanagement.domain.adjustment.batch;

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

    public BatchScheduler(JobLauncher jobLauncher,
                          @Qualifier("statisticsJob") Job statisticsJob) {
        this.jobLauncher = jobLauncher;
        this.statisticsJob = statisticsJob;
    }

    @Scheduled(cron = "0 39 00 * * *")
    public void runStatisticsJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("currentTime", System.currentTimeMillis()) // 매 실행 시 유니크한 파라미터 추가 (중복 방지용)
                    .toJobParameters();
            jobLauncher.run(statisticsJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
