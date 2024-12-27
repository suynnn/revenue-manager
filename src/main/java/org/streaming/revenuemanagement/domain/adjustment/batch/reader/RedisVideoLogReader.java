package org.streaming.revenuemanagement.domain.adjustment.batch.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import org.streaming.revenuemanagement.domain.videolog.dto.VideoLogReqDto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisVideoLogReader implements ItemReader<VideoLogReqDto> {

    private final RedisTemplate<String, Object> redisTemplate;
    private Cursor<byte[]> cursor;
    private static final String dateKeyPattern = "log:video:*:date:" +
            LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "*";

    @Override
    public VideoLogReqDto read() throws Exception {
        if (cursor == null) {
            cursor = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(ScanOptions.scanOptions().match(dateKeyPattern).count(1000).build());
        }

        if (cursor.hasNext()) {
            String key = new String(cursor.next()); // byte[] -> String 변환
            try {
                DataType keyType = redisTemplate.type(key); // 키 타입 확인

                if (keyType == DataType.STRING) { // String 타입 처리
                    String value = (String) redisTemplate.opsForValue().get(key);
                    return convertToDtoFromString(value); // String 데이터를 DTO로 변환
                }

            } catch (Exception e) {
                log.error("Error processing key {}: {}", key, e.getMessage());
            }
        }
        return null; // 더 이상 데이터가 없으면 null 반환
    }

    private VideoLogReqDto convertToDtoFromString(String rawLog) {
        // JSON 포맷으로 저장된 경우 Jackson을 사용해 파싱
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(rawLog, VideoLogReqDto.class);
        } catch (Exception e) {
            log.error("Error converting raw log to DTO: {}", rawLog, e);
            return null;
        }
    }
}
