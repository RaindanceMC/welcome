package de.raindancer.modules.welcome.rules;

import java.util.Locale;

/**
 * What this server is to the module: {@link #WELCOME} greets, plays the intro and runs the tutorial; {@link #NETWORK}
 * — the SMP and any other server of the network — only offers /trailer, which sends you to the welcome server.
 */
public enum Role {
    WELCOME, NETWORK;

    public static Role of(String text) {
        return text != null && text.strip().toLowerCase(Locale.ROOT).equals("network") ? NETWORK : WELCOME;
    }
}
