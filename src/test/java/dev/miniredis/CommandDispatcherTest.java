package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandDispatcherTest {


    @Test
    void dispatcher_OnSetWithValidArguments_StoresValueReturnsOk(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);

        assertEquals("OK", dispatcher.dispatch("SET", List.of("name","Bro"))) ;
        assertEquals("Bro",testStore.get("name"));
    }

    @Test
    void dispatcher_OnSetWithInvalidArguments_ReturnsErrorAndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);

        assertEquals("Valid arguments not found", dispatcher.dispatch("SET", List.of("name"))) ;
        assertNull(testStore.get("name"));
    }

}
