package dev.miniredis;

import java.nio.charset.StandardCharsets;

public class RespSerializer {

    public byte[] serialize(CommandResult commandResult){
        if (commandResult instanceof StatusResult){
            StatusResult statusResult = (StatusResult) commandResult;
            String value = statusResult.getValue();
            byte[] serializedValue = ("+"+value+"\r\n").getBytes(StandardCharsets.UTF_8);
            return serializedValue;
        } else if (commandResult instanceof  IntegerResult) {
            IntegerResult integerResult = (IntegerResult) commandResult;
            long value = integerResult.getValue();
            byte[] serializedValue = (":"+value+"\r\n").getBytes(StandardCharsets.UTF_8);
            return serializedValue;
        }
        return null;
    }
}
