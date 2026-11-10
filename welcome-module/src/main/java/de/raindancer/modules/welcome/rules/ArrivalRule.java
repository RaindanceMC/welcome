package de.raindancer.modules.welcome.rules;

import java.util.Optional;

/** Who arriving on the welcome server is welcomed and sent on, and when a send has not worked. */
public final class ArrivalRule {

    public enum Verdict {
        /** Welcomed, and offered the way on to the network. */
        WELCOME,
        /** Staff or a builder: here to work, never sent on. */
        STAY,
        /** Switched off, or no server to send anybody to. */
        OFF
    }

    /** What a welcomed player sees first. */
    public enum First {
        INTRO,
        TOUR
    }

    /** The note /tutorial on a network server leaves with the player it sends over. */
    public static final String CAME_FOR_THE_TOUR = "tour";

    private ArrivalRule() {
    }

    public static Verdict decide(boolean enabled, String nextServer, boolean mayStay) {
        if (!enabled || nextServer == null || nextServer.isBlank()) {
            return Verdict.OFF;
        }
        return mayStay ? Verdict.STAY : Verdict.WELCOME;
    }

    /**
     * @param sentAt when they were sent on, {@code -1} when they were not
     * @return whether they should be gone by now — the server they were sent to is down or full
     */
    public static boolean stuck(long sentAt, long now, int patienceSeconds) {
        return sentAt >= 0 && now - sentAt >= patienceSeconds * 1000L;
    }

    /** @param note what the server that sent them left with them, if anything */
    public static First first(Optional<String> note, boolean tourReady) {
        return tourReady && note.filter(CAME_FOR_THE_TOUR::equals).isPresent() ? First.TOUR : First.INTRO;
    }
}
