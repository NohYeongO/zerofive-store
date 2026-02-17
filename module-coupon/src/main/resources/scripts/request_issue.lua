-- KEYS[1]: stock (STRING)
-- Returns: SUCCESS, SOLD_OUT

-- 1. 재고 확인
local stock = tonumber(redis.call('GET', KEYS[1]) or '0')
if stock <= 0 then
    return 'SOLD_OUT'
end

-- 2. 재고 차감 (원자적)
local remaining = redis.call('DECR', KEYS[1])
if remaining < 0 then
    redis.call('INCR', KEYS[1])
    return 'SOLD_OUT'
end

return 'SUCCESS'
