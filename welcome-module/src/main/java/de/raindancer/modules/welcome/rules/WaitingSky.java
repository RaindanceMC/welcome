package de.raindancer.modules.welcome.rules;

/** Where a player waiting in the proxy's queue spends the wait: high over the island, under the midnight stars. */
public final class WaitingSky {

    /** Where the clouds hang since 1.21.6; above them nothing but sky. */
    public static final int CLOUDS = 192;
    /** Midnight: the most stars, the moon overhead. */
    public static final long TIME = 18000L;
    /** A little upwards, so the island drops out of sight. */
    public static final float PITCH = -25f;

    private WaitingSky() {
    }

    public static int height(int groundY, int maxHeight) {
        return Math.min(Math.max(groundY + 60, CLOUDS + 30), maxHeight - 10);
    }
}
