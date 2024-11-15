package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.adjustment.batch.VideoDailyStatisticsMap;
import org.streaming.revenuemanagement.domain.videodailystatistics.entity.VideoDailyStatistics;

import java.util.Collection;
import java.util.Iterator;

@Slf4j
@Component
@RequiredArgsConstructor
public class DummyReader implements ItemReader<VideoDailyStatistics> {

    private final VideoDailyStatisticsMap videoDailyStatisticsMap;
    private Iterator<VideoDailyStatistics> iterator;

    @Override
    public VideoDailyStatistics read() {
        // 매번 read() 메서드가 호출될 때 맵의 최신 데이터를 가져옵니다.
        if (iterator == null || !iterator.hasNext()) {
            Collection<VideoDailyStatistics> values = videoDailyStatisticsMap.getStatisticsMap().values();
            iterator = values.iterator();
        }

        if (iterator.hasNext()) {
            return iterator.next();
        }

        return null; // 데이터가 더 이상 없을 경우 null 반환
    }
}