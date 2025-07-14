package kr.hhplus.be.server.infrastructure.persistence.rank;

import java.util.List;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ConcertSoldOutRankRepository {

	private static final String SOLD_OUT_RANK_KEY = "concert:ranking:soldout";

	// 수정된 최종 Lua 스크립트
	private static final String UPDATE_RANK_SCRIPT = """
          local key = KEYS[1]
          local member = ARGV[1]
          local new_score = tonumber(ARGV[2])
          
          -- 현재 점수 조회
          local current_score = redis.call('ZSCORE', key, member)
          
          -- 현재 점수가 없거나(nil), 새 점수가 더 좋을 경우(더 작을 경우)에만 랭킹을 갱신
          if not current_score or new_score < tonumber(current_score) then
             redis.call('ZADD', key, new_score, member)
          end
          
          -- 상위 100위까지만 유지 (0-99위 유지, 100위부터 삭제)
          redis.call('ZREMRANGEBYRANK', key, 100, -1)
          
          -- 최종 랭킹 조회 (0부터 시작)
          local rank = redis.call('ZRANK', key, member)
          
          -- 랭킹에 있다면 1을 더해서 반환 (1위부터 시작하도록), 없다면 nil 반환
          if rank ~= nil then
             return rank + 1
          else
             return nil
          end
          """;

	private final RedisTemplate<String, Object> redisTemplate;

	/**
	 * 콘서트 매진 랭킹 업데이트
	 * @param concertId 매진 콘서트 ID
	 * @param score 점수 (매진까지 걸린 시간, 낮을수록 좋음)
	 * @return 랭킹 (100위 안에 들지 못하면 null)
	 */
	public Long updateRank(UUID concertId, long score) {
		DefaultRedisScript<Long> script = new DefaultRedisScript<>();
		script.setScriptText(UPDATE_RANK_SCRIPT);
		script.setResultType(Long.class);

		List<String> keys = List.of(SOLD_OUT_RANK_KEY);
		// 스크립트에서 필요한 인수만 전달하도록 수정
		return redisTemplate.execute(script, keys, concertId.toString(), score);
	}
}