package dev.miniredis;

public class StatusResult implements CommandResult{
    private final String value;

    public StatusResult(String value){
        this.value=value;
    }

    public String getValue(){
        return value;
    }

}
