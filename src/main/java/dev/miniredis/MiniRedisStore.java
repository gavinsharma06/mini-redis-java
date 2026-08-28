package dev.miniredis;
import java.util.HashMap;
import java.util.Map;

public class MiniRedisStore {
    private Map<String,String> redisStorage = new HashMap<>();

    public void set(String key, String value){
        redisStorage.put(key, value);
    }

    public String get(String key){
        return redisStorage.get(key);
    }
}
