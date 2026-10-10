package dev.miniredis;

public class StringResult implements CommandResult{
    private final String value;

    public StringResult(String value){
        this.value=value;
    }

    public String getValue() {
        return value;
    }
}
