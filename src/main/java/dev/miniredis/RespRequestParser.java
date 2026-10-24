package dev.miniredis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespRequestParser {

    public ParsedCommand parse (InputStream inputStream) throws IOException{
        int firstValue = inputStream.read();
        if (firstValue == '*'){
            String countText = readLine(inputStream);
            int elementCount = Integer.parseInt(countText);
            List<String> elements = new ArrayList<>();
            for ( int i =0; i< elementCount; i++){
                String lengthHeader = readLine(inputStream);
                if (!lengthHeader.startsWith("$")) {
                    throw new IOException("Invalid bulk string prefix");
                }
                int length = Integer.parseInt(lengthHeader.substring(1));
                byte[] data = inputStream.readNBytes(length);
                if (data.length != length) {
                    throw new IOException("Incomplete bulk string");
                }
                int cr = inputStream.read();
                int lf = inputStream.read();
                if (cr != '\r' || lf != '\n') {
                    throw new IOException("Invalid bulk string terminator");
                }
                String decoded = new String(data,StandardCharsets.UTF_8);
                elements.add(decoded);
            }
            return new ParsedCommand(
                    elements.get(0),
                    elements.subList(1, elements.size())
            );
        }
        throw  new IOException("Expected RESP array prefix '*'");
    }

    private String readLine(InputStream inputStream) throws IOException {
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
            } else {
                buffer.write(currentByte);
            }

        }
        throw new IOException("Unexpected EOF while reading RESP line");

    }
}
