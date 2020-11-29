package com.lgh.tool.lock;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisLockTemplate {

    private LockType lockType;
    private Object   key;

    public RedisLockTemplate(LockType lockType, Object key) {
        super();
        this.lockType = lockType;
        this.key = key;
    }

    public void getLockAndExecute(LockCallback lockCallback) {
        // 1.尝试加锁
        boolean isTryLock = false;
        try {
            isTryLock = RedisLockSupport.getInstance().tryLock(lockType.getCode(), key, lockType.getTimeout());
        } catch (Exception ex) {
            log.error("从redis获取锁失败,流程暂时不受锁控制。 key=" + key, ex);
            throw new RedisLockException("从redis获取锁失败,流程暂时不受锁控制");
        }
        if(!isTryLock){
            // log.debug("Redis锁模板，加锁失败。检测到此数据存在并发执行，本次执行将中断。key={}", key);
            throw new RedisLockException("检测到此数据存在并发执行，本次执行将中断");
        }
        boolean bizFlag = true;
        // 2.执行回调
        try {
            lockCallback.execute();
        } catch (Exception ex) {
            bizFlag = false;
            log.error("Redis锁模板，lockCallback执行出错。key=" + key + "，错误消息：" + ex.getMessage(), ex);
        }
        // 3.释放锁
        try {
            RedisLockSupport.getInstance().unlock(lockType.getCode(), key);
        } catch (Exception ex) {
            // redis出错，返回true，暂时不用lock控制
            log.error("关闭redis锁失败。流程暂时不收锁控制。 key=" + key, ex);
        }
        if(!bizFlag){
            throw new BizException("业务方法执行出错");
        }
    }

    public static interface LockCallback {

        void execute() throws Exception;

    }
}
