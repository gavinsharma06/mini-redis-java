package dev.miniredis;

public class ErrorResult implements CommandResult{
    private final String error;

    public ErrorResult(String error){
        this.error=error;
    }

    public String getError() {
        return error;
    }
}
