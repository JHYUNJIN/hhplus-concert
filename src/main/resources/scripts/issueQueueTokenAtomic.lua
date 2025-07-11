-- KEYS[1]: tokenIdKey (e.g., "token:id:{userId}:{concertId}")
-- KEYS[2]: tokenInfoKey (e.g., "token:info:{tokenId}")
-- ARGV[1]: new_token_id_string (UUID.toString())
-- ARGV[2]: serialized_queue_token_object_string (JSON string of QueueToken)
-- ARGV[3]: expiration_seconds_tokenIdKey
-- ARGV[4]: expiration_seconds_tokenInfoKey

local existing_token_id = redis.call('GET', KEYS[1])

if existing_token_id then
    -- 토큰이 이미 존재함, 기존 토큰 ID 반환
    return existing_token_id
else
    -- 토큰이 존재하지 않음, 원자적으로 설정 시도
    local setnx_result = redis.call('SETNX', KEYS[1], ARGV[1])
    if setnx_result == 1 then
        -- SETNX 성공 (경쟁에서 이김)
        redis.call('EXPIRE', KEYS[1], ARGV[3]) -- tokenIdKey 만료 시간 설정

        redis.call('SET', KEYS[2], ARGV[2]) -- QueueToken 객체 저장
        redis.call('EXPIRE', KEYS[2], ARGV[4]) -- tokenInfoKey 만료 시간 설정

        return ARGV[1] -- 새로 발급된 토큰 ID 반환
    else
        -- SETNX 실패 (다른 동시 호출이 락을 획득)
        -- 승자가 설정한 토큰 ID를 다시 읽어서 반환
        return redis.call('GET', KEYS[1])
    end
end