package dev.miniredis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespRequestParser {

    private static final int MAX_ARRAY_ELEMENTS = 1024;
    private static final int MAX_BULK_STRING_BYTES = 1_048_576;
    private static final int MAX_HEADER_BYTES = 64;

    public ParsedCommand parse (InputStream inputStream) throws IOException{
        int firstValue = inputStream.read();
        if (firstValue == -1) {
            return null;
        }
        if (firstValue == '*'){
            String countText = readLine(inputStream);
            int elementCount;
            if (!isAsciiDecimal(countText)) {
                throw new IOException("Invalid RESP command array length");
            }
            try {
                elementCount = Integer.parseInt(countText);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid RESP command array length",e);
            }
            if (elementCount <= 0){
                throw new IOException("Invalid RESP command array length");
            } else if (elementCount > MAX_ARRAY_ELEMENTS) {
                throw new IOException("RESP array exceeds maximum elements");
            }
            List<String> elements = new ArrayList<>();
            for ( int i =0; i< elementCount; i++){
                String lengthHeader = readLine(inputStream);
                if (!lengthHeader.startsWith("$")) {
                    throw new IOException("Invalid bulk string prefix");
                }
                int length;
                if (!isAsciiDecimal(lengthHeader.substring(1))) {
                    throw new IOException("Invalid bulk string length");
                }
               try {
                   length = Integer.parseInt(lengthHeader.substring(1));
               } catch (NumberFormatException e) {
                   throw new IOException("Invalid bulk string length", e);
               }
               if (length < 0){
                   throw new IOException("Invalid bulk string length");
               } else if (length > MAX_BULK_STRING_BYTES) {
                   throw new IOException("RESP bulk string exceeds maximum length");
               }
                byte[] data = inputStream.readNBytes(length);
                if (data.length != length) {
                    throw new IOException("Incomplete bulk string");
                }
                int cr = inputStream.read();
                int lf = inputStream.read();
                if (cr != '\r' || lf != '\n') {
                    throw new IOException("Invalid bulk string terminator");
                }
                var decoder = StandardCharsets.UTF_8.newDecoder();
                decoder.onMalformedInput(CodingErrorAction.REPORT);
                String decoded = decoder.decode(ByteBuffer.wrap(data)).toString();
                elements.add(decoded);
            }
            return new ParsedCommand(
                    elements.get(0),
                    elements.subList(1, elements.size())
            );
        }
        throw  new IOException("Expected RESP array prefix '*'");
    }

    private String readLine(InputStream inputStream) throws IOException{
        int currentByte;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        while ((currentByte = inputStream.read())!=-1){
            if (currentByte == '\r') {
                int nextByte = inputStream.read();
                if (nextByte == '\n') {
                    String result = buffer.toString(StandardCharsets.UTF_8);
                    return result;
                }
                throw new IOException("Invalid RESP line ending");
            } else if (currentByte == '\n') {
                throw new IOException("Invalid RESP line ending");
            }else if (buffer.size() >= MAX_HEADER_BYTES) {
                throw new IOException("RESP header exceeds maximum length");
            } else {
                buffer.write(currentByte);
            }

        }
        throw new IOException("Unexpected EOF while reading RESP line");

    }

    private boolean isAsciiDecimal(String value){
        if (value.isEmpty()){
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char current = value.charAt(i);
            if (current < '0' || current > '9'){
                return false;
            }
        }
        return true;
    }
}
