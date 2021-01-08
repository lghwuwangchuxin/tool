package com.lgh.tool.lock;

import com.snowalker.lock.redisson.RedissonLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * @ClassName ExecutorRedissonNormal
 * @Description:
 * @Author lgh
 * @Date 2021/1/8 17:22
 **/
public class ExecutorRedissonNormal {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRedissonNormal.class);

    @Autowired
    RedissonLock redissonLock;
    public void execute() throws InterruptedException {
        if (redissonLock.lock("redisson", 10)) {
            LOGGER.info("[ExecutorRedisson]--执行定时任务开始，休眠三秒");
            Thread.sleep(3000);
            System.out.println("=======================业务逻辑=============================");
            LOGGER.info("[ExecutorRedisson]--执行定时任务结束，休眠三秒");
            redissonLock.release("redisson");
        } else {
            LOGGER.info("[ExecutorRedisson]获取锁失败");
        }

    }
}
