package dev.miniredis;

import java.util.List;

public class CommandDispatcher {
    private final MiniRedisStore store;

    public CommandDispatcher(MiniRedisStore store) {
        this.store = store;
    }

    public CommandResult dispatch(String commandName, List<String> arguments){
        if (commandName.equalsIgnoreCase("SET")){
            if (arguments.size()!=2){
                return new ErrorResult("Valid arguments not found");
            }
            store.set(arguments.get(0),arguments.get(1));
            return new StatusResult("OK") ;
        } else if (commandName.equalsIgnoreCase("GET")) {

            if (arguments.size()!=1){
                return new ErrorResult("Valid arguments not found") ;
            }
            String value = store.get(arguments.get(0));
            if (value==null) {
                return new NullResult();
            }
            return new StringResult(value);

        }
        return new ErrorResult("command not available / incorrect command") ;

    }


}
