package dev.miniredis;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    @Test
    void parsedValidSetCommand_WithEmptyString() throws IOException{
        byte [] value = "*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$0\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("SET",parsedCommand.commandName());
        assertEquals(List.of("name",""),parsedCommand.arguments());
    }

    @Test
    void preservesEmbeddedCRLF() throws IOException{
        byte[] value = "*3\r\n$3\r\nSET\r\n$3\r\nmsg\r\n$12\r\nHello\r\nWorld\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("SET",parsedCommand.commandName());
        assertEquals(List.of("msg","Hello\r\nWorld"),parsedCommand.arguments());
    }

    @Test
    void parsedCommandWithNoArguments() throws IOException{
        byte [] value = "*1\r\n$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("PING",parsedCommand.commandName());
        assertEquals(List.of(), parsedCommand.arguments());
    }

    @Test
    void rejectsInvalidArrayPrefix(){
        byte [] value = "!2\r\n$3\r\nGET\r\n$4\r\nname\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsEmptyRESPCommandArray(){
        byte[] value = "*0\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsInvalidBulkStringPrefix(){
        byte [] value = "*2\r\n$3\r\nGET\r\n!4\r\nname\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsInvalidBulkStringLength(){
        byte [] value = "*1\r\n$abc\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsNegativeBulkStringLength(){
        byte [] value = "*1\r\n$-2\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsTruncatedBulkString(){
        byte[] value = "*2\r\n$3\r\nGET\r\n$5\r\nabc".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("Incomplete bulk string",exception.getMessage());
    }

    @Test
    void parsesMultibyteUTF8() throws IOException{
        byte[] value = "*2\r\n$3\r\nGET\r\n$9\r\nMontréal\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("GET",parsedCommand.commandName());
        assertEquals(List.of("Montréal"),parsedCommand.arguments());
    }
    @Test
    void rejectsInvalidBulkStringTerminator(){
        byte[] value = "*2\r\n$3\r\nGET\r\n$4\r\nname\rX".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void parsesConsecutiveCommands() throws IOException{
        byte[] value = "*2\r\n$3\r\nGET\r\n$4\r\nname\r\n*1\r\n$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        ParsedCommand parsedCommand = parser.parse(request);
        assertEquals("GET",parsedCommand.commandName());
        assertEquals(List.of("name"),parsedCommand.arguments());
        ParsedCommand parsedAgain = parser.parse(request);
        assertEquals("PING",parsedAgain.commandName());
        assertEquals(List.of(), parsedAgain.arguments());
    }

    @Test
    void rejectsNonNumericArrayCount(){
        byte[] value = "*abc\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsExcessiveArrayCount(){
        byte[] value = "*1025\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("RESP array exceeds maximum elements",exception.getMessage());
    }

    @Test
    void rejectsExcessiveBulkStringLength(){
        byte[] value = "*1\r\n$1048577\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("RESP bulk string exceeds maximum length",exception.getMessage());
    }

    @Test
    void rejectsOversizedRespHeader() {
        String input = "*" + "1".repeat(65) + "\r\n";
        byte[] value = input.getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("RESP header exceeds maximum length",exception.getMessage());
    }

    @Test
    void returnsNullOnCleanEOF() throws IOException {
        byte[] value = "".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        assertNull(parser.parse(request));
    }

    @Test
    void rejectsPositiveSignInArrayCount(){
        byte[] value = "*+1\r\n$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("Invalid RESP command array length",exception.getMessage());
    }

    @Test
    void rejectsUnicodeDigitInArrayCount(){
        byte[] value = "*١\r\n$4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("Invalid RESP command array length",exception.getMessage());
    }

    @Test
    void rejectsPositiveSignInBulkLength(){
        byte[] value = "*1\r\n$+4\r\nPING\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("Invalid bulk string length",exception.getMessage());
    }

    @Test
    void rejectsNegativeZeroBulkLength(){
        byte[] value = "*1\r\n$-0\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream request = new ByteArrayInputStream(value);
        RespRequestParser parser = new RespRequestParser();
        IOException exception = assertThrows(IOException.class, () -> parser.parse(request));
        assertEquals("Invalid bulk string length",exception.getMessage());
    }

    @Test
    void rejectsMalformedUTF8Key() throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write("*3\r\n$3\r\nSET\r\n$1\r\n".getBytes(StandardCharsets.UTF_8));
        bytes.write(0x80);
        bytes.write("\r\n$5\r\nGavin\r\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream request = new ByteArrayInputStream(bytes.toByteArray());
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsMalformedUTF8Value() throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write("*3\r\n$3\r\nSET\r\n$4\r\nname\r\n$1\r\n".getBytes(StandardCharsets.UTF_8));
        bytes.write(0x81);
        bytes.write("\r\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream request = new ByteArrayInputStream(bytes.toByteArray());
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

    @Test
    void rejectsTruncatedUTF8Sequence() throws IOException{
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bytes.write("*1\r\n$1\r\n".getBytes(StandardCharsets.UTF_8));
        bytes.write(0xC3);
        bytes.write("\r\n".getBytes(StandardCharsets.UTF_8));
        ByteArrayInputStream request = new ByteArrayInputStream(bytes.toByteArray());
        RespRequestParser parser = new RespRequestParser();
        assertThrows(IOException.class, () -> parser.parse(request));
    }

}
