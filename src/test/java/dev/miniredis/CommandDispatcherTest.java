package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandDispatcherTest {


    @Test
    void dispatcher_OnSetWithValidArguments_StoresValueReturnsStatusResult(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result =
                dispatcher.dispatch("SET", List.of("name", "Bro"));
        assertTrue(result instanceof StatusResult);
        StatusResult statusResult= (StatusResult) result;
        assertEquals("OK", statusResult.getValue());
        assertEquals("Bro",testStore.get("name"));
    }

    @Test
    void dispatcher_OnSetWithInvalidArguments_ReturnsErrorResultAndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result= dispatcher.dispatch("SET",List.of("name"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Valid arguments not found",errorResult.getError());
        assertNull(testStore.get("name"));
    }

    @Test
    void dispatcher_OnUnknownCommand_ReturnsErrorResultAndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("something",List.of("irrelevant"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult=(ErrorResult) result;
        assertEquals("command not available / incorrect command",errorResult.getError());
        assertNull(testStore.get("irrelevant"));
    }

    @Test
    void dispatcher_OnGetWithExistingKey_ReturnsStringResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name","Bro");
        CommandResult result = dispatcher.dispatch("GET",List.of("name"));
        assertTrue(result instanceof StringResult);
        StringResult stringResult = (StringResult) result;
        assertEquals("Bro",stringResult.getValue());
        assertEquals("Bro",testStore.get("name"));
    }

    @Test
    void dispatcher_OnGetWithMissingKey_ReturnsNullResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("GET",List.of("name"));
        assertTrue(result instanceof NullResult);
    }

}
