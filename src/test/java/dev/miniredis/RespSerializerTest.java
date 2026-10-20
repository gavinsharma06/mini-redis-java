package dev.miniredis;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RespSerializerTest {

    @Test
    void serializeOnStatusResult_ReturnsCorrectBytes(){
        StatusResult statusResult = new StatusResult("OK");
        RespSerializer serializer = new RespSerializer();
        byte[] expected = "+OK\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(statusResult));

    }

    @Test
    void serializeOnIntegerResult_ReturnsCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        IntegerResult integerResult = new IntegerResult(56L);
        byte[] expected = ":56\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected,serializer.serialize(integerResult));
    }

    @Test
    void serializeOnErrorResult_ReturnsCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        ErrorResult errorResult = new ErrorResult(ErrorResult.INVALID_COMMAND);
        byte[] expected = "-ERR command not available / incorrect command\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected,serializer.serialize(errorResult));
    }

    @Test
    void serializeOnNullResult_ReturnsCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        NullResult nullResult = new NullResult();
        byte[] expected = "$-1\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected,serializer.serialize(nullResult));
    }
}
