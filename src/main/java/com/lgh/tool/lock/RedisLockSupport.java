package com.lgh.tool.lock;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisLockSupport {

    private RedisTemplate<String, Object> redisTemplate = (RedisTemplate) SpringContextUtils.getBean("redisTemplate");

    private volatile static RedisLockSupport instance;

    public static RedisLockSupport getInstance() {
        if (instance == null) {
            synchronized (RedisLockSupport.class) {
                if (instance == null) {
                    instance = new RedisLockSupport();
                }
            }
        }
        return instance;
    }

    public boolean tryLock(String lockType, Object key, int seconds) {
        boolean success = false;
        String cacheKey = createLockKey(lockType, key);
        try {
            // 构建value。包括：IP|创建时间
            String value = buildCacheValue();
            // 不存在才创建，且同时设置超时时间。超时时间单位为秒
            SessionCallback<Boolean> sessionCallback = new SessionCallback<Boolean>() {
                List<Object> exec = null;
                @Override
                @SuppressWarnings("unchecked")
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    redisTemplate.opsForValue().setIfAbsent(cacheKey,value);
                    redisTemplate.expire(cacheKey,seconds, TimeUnit.SECONDS);
                    exec = operations.exec();
                    if(exec.size() > 0) {
                        return (Boolean) exec.get(0);
                    }
                    return false;
                }
            };
            success = redisTemplate.execute(sessionCallback);
            if(success){
                // log.debug("redis锁加锁结果：{}，key={}", success, cacheKey);
            } else {
                String oldValue = redisTemplate.opsForValue().get(cacheKey).toString();
                log.info("redis锁加锁结果：{"+success+"}，key={"+cacheKey+"}, 已存在锁信息：{"+oldValue+"}");
            }
        } catch (Exception ex) {
            log.error("redis 加锁出错。出错原因：" + ex.getMessage(), ex);
            throw ex;
        }
        return success;
    }

    public void unlock(String type, Object key) {
        String cacheKey = createLockKey(type, key);
        try {
            redisTemplate.delete(cacheKey);
        } catch (Exception ex) {
            log.error("Redis访问出错！可能会影响并发检查。", ex);
            throw ex;
        }
    }

    public String createLockKey(String lockType, Object key) {
        return lockType + "_" + key;
    }

    private String buildCacheValue() {
        String value = "";
        try {
            String localIp = InetAddress.getLocalHost().getHostAddress();
            value = localIp + "|" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_FORMAT);
        } catch (Exception e) {

        }
        return value;
    }

}
