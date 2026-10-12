package dev.miniredis;
import java.util.HashMap;
import java.util.Map;
import java.time.Clock;

public class MiniRedisStore {
    private final Map<String,String> redisStorage = new HashMap<>();
    private final Clock clock;
    private final Map<String, Long> expirations = new HashMap<>();

    public MiniRedisStore(){
        this(Clock.systemUTC());
    }

    public MiniRedisStore(Clock clock){
        this.clock=clock;
    }

    public void set(String key, String value) {
        expirations.remove(key);
        redisStorage.put(key, value);
    }

    public String get(String key) {
        expireIfNeeded(key);
        return redisStorage.get(key);
    }

    public boolean exists(String key) {
        expireIfNeeded(key);
        return redisStorage.containsKey(key);
    }

    public int delete(String key){
        if (exists(key)){
            redisStorage.remove(key);
            expirations.remove(key);
            return 1;
        }
        return 0;
    }

    public long incr(String key){
        long increment;
        String value;

        if (!exists(key)){
            long keyDoesNotExist=0;
            increment = keyDoesNotExist+1;
            value = String.valueOf(increment);
            set(key,value);
            return increment;
        }
        long numberValue = Long.parseLong(get(key));
        increment=Math.incrementExact(numberValue);
        value = String.valueOf(increment);
        set(key,value);
        return increment;

    }

    private void expireIfNeeded(String key){
        if (expirations.containsKey(key)){
            long expireAt = expirations.get(key);
            if (expireAt <= clock.instant().toEpochMilli()){
                redisStorage.remove(key);
                expirations.remove(key);
            }
        }
    }

    public long expire(String key, long ttl){
        if (exists(key)){
            if (ttl<=0){
                redisStorage.remove(key);
                expirations.remove(key);
                return 1L;
            }
            long ttlMillis = ttl * 1000L;
            long expiresAt = clock.instant().toEpochMilli() + ttlMillis;
            expirations.put(key, expiresAt);
            return 1L;
        }
        return 0L;
    }

}
