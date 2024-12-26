package org.streaming.revenuemanagement.domain.adjustment.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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

    @Scheduled(cron = "00 00 03 * * *")
    public void runStatisticsJob() {
        try {
            LocalDateTime startDate = LocalDate.now().atStartOfDay();
            LocalDateTime endDate = LocalDate.now().atTime(LocalTime.MAX);

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
