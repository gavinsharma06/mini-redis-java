package dev.miniredis;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RespSerializer {

    public byte[] serialize(CommandResult commandResult){
        Objects.requireNonNull(commandResult, "commandResult must not be null");
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
        } else if (commandResult instanceof ErrorResult) {
            ErrorResult errorResult = (ErrorResult) commandResult;
            String value = errorResult.getError();
            byte[] serializedValue = ("-ERR "+value+"\r\n").getBytes(StandardCharsets.UTF_8);
            return serializedValue;
        } else if (commandResult instanceof NullResult) {
            byte[] serializedValue = "$-1\r\n".getBytes(StandardCharsets.UTF_8);
            return serializedValue;
        } else if (commandResult instanceof StringResult) {
            StringResult stringResult = (StringResult) commandResult;
            String value = stringResult.getValue();
            int count =value.getBytes(StandardCharsets.UTF_8).length;
            byte [] serializedValue = ("$"+count+"\r\n"+value+"\r\n").getBytes(StandardCharsets.UTF_8);
            return serializedValue;
        }
        throw new IllegalArgumentException("Unsupported command result type");
    }
}
