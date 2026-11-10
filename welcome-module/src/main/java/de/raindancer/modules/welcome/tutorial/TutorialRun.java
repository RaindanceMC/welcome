package de.raindancer.modules.welcome.tutorial;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * One newcomer's way through the tutorial: for each station the guide walks there ({@link Phase#WALK}), explains it
 * ({@link Phase#EXPLAIN}), and they try it ({@link Phase#TRY}) by typing one of its commands — then the next station,
 * until {@link Phase#FINISHED}. Typing the command while the guide still talks counts too.
 */
public final class TutorialRun {

    public enum Phase { WALK, EXPLAIN, TRY, FINISHED }

    private final List<Station> stations;
    private int index;
    private Phase phase;

    public TutorialRun(List<Station> stations) {
        this.stations = List.copyOf(stations);
        this.phase = stations.isEmpty() ? Phase.FINISHED : Phase.WALK;
    }

    public Phase phase() {
        return phase;
    }

    public Optional<Station> station() {
        return phase == Phase.FINISHED ? Optional.empty() : Optional.of(stations.get(index));
    }

    /** 1-based, for "station 3 of 9". */
    public int number() {
        return Math.min(index + 1, stations.size());
    }

    public int total() {
        return stations.size();
    }

    public void arrived() {
        if (phase == Phase.WALK) {
            phase = Phase.EXPLAIN;
        }
    }

    public void explained() {
        if (phase != Phase.EXPLAIN) {
            return;
        }
        if (stations.get(index).commands().isEmpty()) {
            next();
        } else {
            phase = Phase.TRY;
        }
    }

    /** @param line what they typed, with its slash — {@code /sethome base} */
    public boolean typed(String line) {
        if (phase != Phase.EXPLAIN && phase != Phase.TRY) {
            return false;
        }
        String first = line.strip().replaceFirst("^/", "").split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        String name = first.contains(":") ? first.substring(first.indexOf(':') + 1) : first;
        if (!stations.get(index).commands().contains(name)) {
            return false;
        }
        next();
        return true;
    }

    public void skip() {
        if (phase != Phase.FINISHED) {
            next();
        }
    }

    private void next() {
        index++;
        phase = index >= stations.size() ? Phase.FINISHED : Phase.WALK;
    }
}
