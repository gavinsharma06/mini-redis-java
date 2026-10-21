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

    @Test
    void serializeOnStringResult_ReturnsCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        StringResult stringResult = new StringResult("hello");
        byte[] expected = "$5\r\nhello\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(stringResult));
    }

    @Test
    void serializeOnStringResultWithEmptyString_ReturnCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        StringResult stringResult = new StringResult("");
        byte[] expected = "$0\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(stringResult));
    }

    @Test
    void serializeOnStringResult_ReturnsCorrectUTF8Bytes(){
        RespSerializer serializer = new RespSerializer();
        StringResult stringResult = new StringResult("\u00E9");
        byte[] expected = "$2\r\n\u00E9\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(stringResult));
    }

    @Test
    void serializeOnStringResultWithCLRF_ReturnsCorrectBytes(){
        RespSerializer serializer = new RespSerializer();
        StringResult stringResult = new StringResult("a\r\nb");
        byte[] expected = "$4\r\na\r\nb\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(stringResult));
    }
    @Test
    void serializerOnNullInput_throwsNullPointerException(){
        RespSerializer serializer = new RespSerializer();
        assertThrows(NullPointerException.class, () -> serializer.serialize(null));
    }
}
