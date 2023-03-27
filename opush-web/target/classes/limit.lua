--利用zset集合实现滑动窗口限流
--KEYS[1]: 限流key
--ARGV[1]: 限流窗口（毫秒，当前为300000ms）
--ARGV[2]: 当前时间戳（作为score）
--ARGV[3]: 阈值（当前为1）
--ARGV[4]: score对应的分布式全局唯一ID（雪花算法生成，随时间递增）
-- 1. zset集合【限流key】中元素按score排序后，获取指定score范围【0，当前时间 - 300000ms】内的元素并移除范围外的元素
redis.call('zremrangeByScore', KEYS[1], 0, ARGV[2] - ARGV[1])
-- 2. 获取zset集合【限流key】中元素个数【一个用户可能有】
local res = redis.call('zcard', KEYS[1])
-- 3. 判断该限流key是否存在【是否在限流窗口内给用户发过信息】，是否超过阈值【当前为1】
if (res == nil) or (res < tonumber(ARGV[3])) then
    -- 添加一个元素到zset集合中【当前时间戳作为score，分布式全局唯一ID作为member】
    redis.call('zadd', KEYS[1], ARGV[2], ARGV[4])
    -- 设置整个zset集合的过期时间【单位s，当前为300s，即5分钟】
    redis.call('expire', KEYS[1], ARGV[1]/1000)
    return 0
else
    return 1
end
