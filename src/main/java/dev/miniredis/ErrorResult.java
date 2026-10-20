package dev.miniredis;

public class ErrorResult implements CommandResult{
    private final String error;
    public static final String INVALID_ARITY = "Valid arguments not found";
    public static final String INVALID_COMMAND = "command not available / incorrect command";
    public static final String INVALID_NUMBER = "Value is not a valid number";
    public static final String ILLEGAL_OVERFLOW = "Overflow Occurred, Game Over";


    public ErrorResult(String error){
        this.error=error;
    }

    public String getError() {
        return error;
    }
}
