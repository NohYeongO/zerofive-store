-- KEYS[1]: queue (ZSET), KEYS[2]: admitted (HASH)
-- ARGV[1]: accountId
-- Returns: {status, position}
-- Status: WAITING, ADMITTED, NOT_IN_QUEUE

-- 1. ZSET에서 순번 확인
local rank = redis.call('ZRANK', KEYS[1], ARGV[1])
if rank then
    return {'WAITING', rank + 1}
end

-- 2. admitted 확인 → 있으면 삭제하고 ADMITTED return
if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then
    redis.call('HDEL', KEYS[2], ARGV[1])
    return {'ADMITTED', 0}
end

-- 3. 둘 다 없음
return {'NOT_IN_QUEUE', 0}
