package org.streaming.revenuemanagement.domain.adjustment.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

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

    @Scheduled(cron = "00 43 19 * * *")
    public void runStatisticsJob() {
        try {
            // 어제의 시작 날짜와 종료 날짜 정의
            LocalDateTime startDate = LocalDateTime.now().minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endDate = LocalDateTime.now().minusDays(1).withHour(23).withMinute(59).withSecond(59).withNano(0);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("currentTime", System.currentTimeMillis()) // 매 실행 시 유니크한 파라미터 추가 (중복 방지용)
                    .addString("startDate", startDate.toString()) // 어제의 시작 날짜 파라미터 추가
                    .addString("endDate", endDate.toString()) // 어제의 종료 날짜 파라미터 추가
                    .toJobParameters();

            jobLauncher.run(statisticsJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
