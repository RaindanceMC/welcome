package de.raindancer.modules.welcome.rules;

import de.raindancer.core.content.pack.PackStatus;

/** Whether a newcomer's intro plays, and when it may start. */
public final class IntroRule {

    /** How long the intro waits for the resource pack (which carries its music) before it starts anyway. */
    public static final int PACK_PATIENCE_SECONDS = 30;

    private IntroRule() {
    }

    /** At least two shots: one alone is nothing to fly between. */
    public static boolean plays(boolean enabled, int usableShots) {
        return enabled && usableShots >= 2;
    }

    /**
     * @param packExists whether this server sends a resource pack at all
     * @param waited     seconds since they arrived
     */
    public static boolean ready(PackStatus status, boolean packExists, double waited) {
        if (!packExists || waited >= PACK_PATIENCE_SECONDS) {
            return true;
        }
        return status.isWearing() || status == PackStatus.DECLINED || status == PackStatus.FAILED;
    }
}
