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

    // Step1: VideoStatistics 별로 전날 조회수, 광고 조회수, 재생시간, 정산 데이터를 집계할 VideoDailyStatistics 객체 생성
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


    // Step2: VideoLog 데이터를 가지고 VideoDailyStatistics에 쌓는 단계
    @Bean
    public Step statisticsStep2() {

        return new StepBuilder("statisticsStep2", jobRepository)
                .<VideoLogStatisticsRespDto, VideoDailyStatistics> chunk(chunkSize, platformTransactionManager)
                .reader(videoLogStatisticsReader())
                .processor(videoLogStatisticsProcessor())
                .writer(videoLogVideoDailyStatisticsWriter())
                .build();
    }

    // 청크 사이즈 별로 VideoLog 데이터를 읽어와서 같은 영상에 대한 조회수, 광고 조회수, 재생시간 합계해 DTO로 반환
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

    // 각 비디오의 VideoDailyStatistics에 조회수, 광고 조회수, 재생시간 업데이트
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

                // 기존 데이터에 새로운 통계 정보 업데이트
                videoDailyStatistics.updateStatistics(views, adViews, playTime);
                return videoDailyStatistics;
            }
        };
    }

    // 그리고 VideoDailyStatistics들을 saveAll
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

    // Step3: VideoDailyStatistics 데이터를 통해 누적 합산 엔티티인 VideoStatistics 업데이트
    @Bean
    public Step statisticsStep3() {

        return new StepBuilder("statisticsStep3", jobRepository)
                .<VideoDailyStatistics, VideoStatistics> chunk(chunkSize, platformTransactionManager)
                .reader(videoDailyStatisticsReader())
                .processor(videoDailyStatisticsProcessor())
                .writer(videoStatisticsWriter())
                .build();
    }

    // 당일 생성된 VideoDailyStatistics를 찾아옴
    @Bean
    public RepositoryItemReader<VideoDailyStatistics> videoDailyStatisticsReader() {

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        return new RepositoryItemReaderBuilder<VideoDailyStatistics>()
                .name("videoDailyStatisticsReader")
                .arguments(List.of(startOfDay, endOfDay)) // 날짜 범위와 페이징 정보 전달
                .pageSize(chunkSize)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoDailyStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    // videoStatistics에 조회수, 광고 조회수, 재생시간 누적 합산 업데이트
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

    // Step4: 정산 작업을 수행하는 단계
    @Bean
    public Step adjustmentStep1() {

        return new StepBuilder("adjustmentStep1", jobRepository)
                .<VideoDailyStatistics, VideoDailyStatistics> chunk(chunkSize, platformTransactionManager)
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
                .pageSize(chunkSize)
                .methodName("findAllByCreatedAtBetween")
                .repository(videoDailyStatisticsRepository)
                .sorts(Map.of("id", Sort.Direction.ASC))
                .build();
    }

    // 당일 생성된 VideoDailyStatistics의 데이터들을 기반으로 단가를 계산하고 업데이트
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

                // 단가 계산 후 조회수 정산 및 광고 조회수 정산 비용 업데이트
                videoDailyStatistics.updateAdjustment(videoAdjustment);
                videoDailyStatistics.updateAdAdjustment(adAdjustment);

                videoStatistics.updateAdjustment(videoAdjustment);
                videoStatistics.updateAdAdjustment(adAdjustment);

                return videoDailyStatistics;
            }

            // 요구사항대로 단가 계산하는 메서드
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

    // VideoDailyStatistics 및 VideoStatistics를 모두 저장
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

    // 두 개의 Repository를 사용하여 데이터를 한 번에 저장
    @Transactional
    public void saveAllVideoData(List<VideoDailyStatistics> videoDailyStatisticsList, List<VideoStatistics> videoStatisticsList) {
        // VideoDailyStatistics와 VideoStatistics를 모두 한 번에 저장
        videoDailyStatisticsRepository.saveAll(videoDailyStatisticsList);
        videoStatisticsRepository.saveAll(videoStatisticsList);
    }
}
