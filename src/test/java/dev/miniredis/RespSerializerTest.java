package dev.miniredis;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class RespSerializerTest {

    @Test
    void SerializerOnStatusResult_returnsCorrectBytes(){
        StatusResult statusResult = new StatusResult("OK");
        RespSerializer serializer = new RespSerializer();
        byte[] expected = "+OK\r\n".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expected, serializer.serialize(statusResult));

    }
}
