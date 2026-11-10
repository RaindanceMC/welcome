package de.raindancer.modules.welcome.tutorial;

import org.bukkit.Material;

import java.util.List;

/**
 * A stop on the tutorial: what the guide explains there (its message keys, shown in a dialog with its icon) and the
 * commands a newcomer can type to try it; none, and there is nothing to try.
 */
public record Station(String id, Material icon, List<String> commands) {

    public Station {
        commands = List.copyOf(commands);
    }

    /** {@code welcome.tutorial.<id>.<part>} — title, say1, say2, hint. */
    public String key(String part) {
        return "welcome.tutorial.%s.%s".formatted(id, part);
    }
}
