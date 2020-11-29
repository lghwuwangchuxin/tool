package com.lgh.tool.lock;

public enum LockType {

    default_lock("edc.default", 120, "edc默认锁"),
    crfsave_lock("edc.crfsave", 120, "crf保存锁");

    private String code;
    private int    timeout;// 单位：秒
    private String desc;

    private LockType(String code, int timeout, String desc) {
        this.code = code;
        this.timeout = timeout;
        this.desc = desc;
    }

    public String getCode() {
        return this.code;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getDesc() {
        return this.desc;
    }

    public static LockType toLockType(String code) {
        LockType[] items = LockType.values();
        for (LockType item : items) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }
}
