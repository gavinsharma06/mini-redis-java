package dev.miniredis;

import java.util.List;

public class CommandDispatcher {
    private final MiniRedisStore store;

    public CommandDispatcher(MiniRedisStore store) {
        this.store = store;
    }

    public String dispatch(String commandName, List<String> arguments){
        if (commandName.equalsIgnoreCase("SET")){
            if (arguments.size()!=2){
                return "Valid arguments not found";
            }
            store.set(arguments.get(0),arguments.get(1));
            return "OK" ;
        } else if (commandName.equalsIgnoreCase("GET")) {
            if (arguments.size()!=1){
                return "Valid arguments not found" ;
            }
            return store.get(arguments.get(0));

        }
        return "command not available / incorrect command" ;

    }


}
