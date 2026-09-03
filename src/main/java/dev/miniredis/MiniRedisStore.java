package dev.miniredis;
import java.util.HashMap;
import java.util.Map;

public class MiniRedisStore {
    private Map<String,String> redisStorage = new HashMap<>();

    public void set(String key, String value) {
        redisStorage.put(key, value);
    }

    public String get(String key) {
        return redisStorage.get(key);
    }

    public boolean exists(String key) {
        return redisStorage.containsKey(key);
    }

    public int delete(String key){
        if (exists(key)){
            redisStorage.remove(key);
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
        increment=numberValue+1;
        value = String.valueOf(increment);
        set(key,value);
        return increment;

    }

}
