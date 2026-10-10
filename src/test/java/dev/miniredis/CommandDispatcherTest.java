package dev.miniredis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandDispatcherTest {


    @Test
    void dispatcher_OnSetWithValidArguments_StoresValue_ReturnsStatusResult(){
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
    void dispatcher_OnSetWithInvalidArity_ReturnsErrorResult_AndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result= dispatcher.dispatch("SET",List.of("name"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Valid arguments not found",errorResult.getError());
        assertNull(testStore.get("name"));
    }

    @Test
    void dispatcher_OnSetWithTooManyArguments_ReturnsCorrectErrorResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("SET", List.of("name","GAVIN","something"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult=(ErrorResult) result;
        assertEquals("Valid arguments not found", errorResult.getError());
        assertNull(testStore.get("name"));
    }

    @Test
    void dispatcher_OnUnknownCommand_ReturnsErrorResult_AndDoesNotModifyStore(){
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

    @Test
    void dispatcher_OnGetWithInvalidArity_ReturnsCorrectErrorResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name","Gavin");
        CommandResult result = dispatcher.dispatch("GET", List.of("name","GAVIN"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult=(ErrorResult) result;
        assertEquals("Valid arguments not found", errorResult.getError());
    }

    @Test
    void dispatcher_OnExistsWithExistingKey_ReturnsCorrectIntegerResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name","Bro");
        CommandResult result = dispatcher.dispatch("EXISTS",List.of("name"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(1L,integerResult.getValue());
    }

    @Test
    void dispatcher_OnExistsWithMissingKey_ReturnsCorrectIntegerResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("EXISTS", List.of("something"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(0L, integerResult.getValue());
    }

    @Test
    void dispatcher_OnExistsWithIncorrectArity_ReturnsCorrectErrorResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("EXISTS",List.of("name","Bro"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult=(ErrorResult) result;
        assertEquals("Valid arguments not found", errorResult.getError());
    }

    @Test
    void dispatcher_OnDeleteWithExistingKey_ReturnsCorrectIntegerResult_AndModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name","Bro");
        CommandResult result = dispatcher.dispatch("DEL",List.of("name"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(1L, integerResult.getValue());
        assertFalse(testStore.exists("name"));
    }

    @Test
    void dispatcher_OnDeleteWithMissingKey_ReturnsCorrectIntegerResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("DEL",List.of("something"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(0L, integerResult.getValue());
    }

    @Test
    void dispatcher_OnDeleteWithIncorrectArity_ReturnCorrectErrorResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name", "Bro");
        CommandResult result = dispatcher.dispatch("DEL", List.of("name","Bro"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Valid arguments not found", errorResult.getError());
    }

    @Test
    void dispatcher_OnIncrWithCorrectValue_ReturnsIntegerResult_AndModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("GAVIN","1");
        CommandResult result = dispatcher.dispatch("INCR",List.of("GAVIN"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(2L,integerResult.getValue());
        assertEquals("2",testStore.get("GAVIN"));
    }

    @Test
    void dispatcher_OnIncrWithMissingKey_ReturnsCorrectIntegerResult_AndModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result = dispatcher.dispatch("INCR", List.of("something"));
        assertTrue(result instanceof IntegerResult);
        IntegerResult integerResult = (IntegerResult) result;
        assertEquals(1L,integerResult.getValue());
        assertEquals("1",testStore.get("something"));
    }

    @Test
    void dispatcher_OnIncrWithInvalidValue_ReturnsCorrectErrorResult_AndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("name","GAVIN");
        CommandResult result = dispatcher.dispatch("INCR", List.of("name"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Value is not numeric",errorResult.getError());
        assertEquals("GAVIN",testStore.get("name"));
    }

    @Test
    void dispatcher_OnIncr_OnOverflow_ReturnsCorrectErrorResult_AndDoesNotModifyStore(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("GAVIN",String.valueOf(Long.MAX_VALUE));
        CommandResult result = dispatcher.dispatch("INCR",List.of("GAVIN"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Overflow Occurred, Game Over", errorResult.getError());
        assertEquals(String.valueOf(Long.MAX_VALUE),testStore.get("GAVIN"));
    }

    @Test
    void dispatcher_OnIncrWithIncorrectArity_ReturnsCorrectErrorResult(){
        MiniRedisStore testStore = new MiniRedisStore();
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        testStore.set("GAVIN","1");
        CommandResult result = dispatcher.dispatch("INCR", List.of("Gavin","1"));
        assertTrue(result instanceof ErrorResult);
        ErrorResult errorResult = (ErrorResult) result;
        assertEquals("Valid arguments not found",errorResult.getError());
    }

    @Test
    void dispatcher_OnMixedCaseCommand_ReturnsCorrectCommandResult(){
        MiniRedisStore testStore = new MiniRedisStore() ;
        CommandDispatcher dispatcher = new CommandDispatcher(testStore);
        CommandResult result =
                dispatcher.dispatch("SeT", List.of("name", "Bro"));
        assertTrue(result instanceof StatusResult);
        StatusResult statusResult= (StatusResult) result;
        assertEquals("OK", statusResult.getValue());
        assertEquals("Bro",testStore.get("name"));
    }


}
