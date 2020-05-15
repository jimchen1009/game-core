package com.game.common.lock;

import com.game.cache.exception.CacheException;

import java.util.Objects;

public class LockKey {

    public static final String SYSTEM_PREFIX =  "@";

    private final String primary;
    private final String secondary;

    /**
     *
     * @param primary 业务名字
     * @param secondary 业务键值
     */
    private LockKey(String primary, String secondary) {
        this.primary = primary;
        this.secondary = secondary;
    }

    public LockKey(String primary) {
        this(primary, null);
    }

    public String getPrimary() {
        return primary;
    }

    public String getSecondary() {
        return secondary;
    }

    public LockKey createLockKey(String secondary){
        return new LockKey(primary, secondary);
    }

    public String toLockName(){
        return secondary == null ? primary : String.format("%s.%s", primary, secondary);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LockKey lockKey = (LockKey) o;
        return Objects.equals(primary, lockKey.primary) &&
                Objects.equals(secondary, lockKey.secondary);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primary, secondary);
    }

    @Override
    public String toString() {
        return "{" +
                "primary='" + primary + '\'' +
                ", secondary='" + secondary + '\'' +
                '}';
    }

    public static LockKey systemLockKey(String name){
        return new LockKey("@" + name);
    }

    public static LockKey extLockKey(String name){
        if (name.indexOf(SYSTEM_PREFIX) == 0){
            throw new CacheException("don't support name:%s", name);
        }
        return new LockKey("@" + name);
    }
}
