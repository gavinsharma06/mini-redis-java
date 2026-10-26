package dev.miniredis;

import java.util.List;

public record ParsedCommand(String commandName, List<String> arguments) {
}
