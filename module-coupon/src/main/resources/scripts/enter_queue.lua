-- KEYS[1]: queue (ZSET), KEYS[2]: admitted (HASH)
-- ARGV[1]: accountId, ARGV[2]: now (timestamp)
-- Returns: {status, position}
-- Status: QUEUED, ADMITTED

-- 1. 이미 대기 중?
local existingRank = redis.call('ZRANK', KEYS[1], ARGV[1])
if existingRank then
    return {'QUEUED', existingRank + 1}
end

-- 2. 이미 입장됨?
if redis.call('HEXISTS', KEYS[2], ARGV[1]) == 1 then
    return {'ADMITTED', 0}
end

-- 3. 대기열 적재
redis.call('ZADD', KEYS[1], ARGV[2], ARGV[1])
local rank = redis.call('ZRANK', KEYS[1], ARGV[1])
return {'QUEUED', rank + 1}
