package org.streaming.revenuemanagement.domain.adjustment.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.streaming.revenuemanagement.domain.videostatistics.repository.VideoStatisticsRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class VideoStatisticsPartitioner implements Partitioner {

    private final VideoStatisticsRepository videoStatisticsRepository;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        long min = videoStatisticsRepository.findMinId(); // videoStatistics에서 최소 ID 조회
        long max = videoStatisticsRepository.findMaxId(); // videoStatistics에서 최대 ID 조회

        log.info("VideoStatisticsPartitioner min = {}, max = {}", min, max);

        long targetSize = (max - min) / gridSize + 1;

        Map<String, ExecutionContext> result = new HashMap<>();
        long number = 0;
        long start = min;
        long end = start + targetSize - 1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);

            if (end >= max) {
                end = max;
            }

            value.putLong("minId", start); // 각 파티션마다 사용될 minId
            value.putLong("maxId", end); // 각 파티션마다 사용될 maxId
            start += targetSize;
            end += targetSize;
            number++;
        }

        return result;
    }
}
