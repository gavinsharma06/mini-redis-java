package dev.miniredis;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RespRequestParserTest {

    @Test
    void parsedValidGetCommand() throws IOException {
        byte[] value = "*2\r\n$3\r\nGET\r\n$4\r\nname\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value) ;
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("GET",parsedCommand.commandName());
        assertEquals(List.of("name"),parsedCommand.arguments() );
    }

    @Test
    void parsedValidSetCommand() throws IOException{
        byte [] value = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$5\r\nGavin\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("SET",parsedCommand.commandName());
        assertEquals(List.of("name","Gavin"),parsedCommand.arguments());
    }
}
