package de.raindancer.modules.welcome;

import de.raindancer.core.data.settings.Describe;
import de.raindancer.core.data.settings.In;
import de.raindancer.core.data.settings.Key;
import de.raindancer.core.data.settings.Range;
import de.raindancer.core.data.settings.Settings;
import de.raindancer.core.data.settings.Title;
import de.raindancer.core.data.settings.Topic;
import org.bukkit.Material;

/** What an owner decides about the welcome server. The schema of the file and of the /settings page. */
@Settings(id = "welcome", topics = {
        @Topic(path = "welcome", title = "Welcome server", icon = Material.OAK_SAPLING),
})
public record WelcomeSettings(

        @In("welcome") @Title("Welcome on")
        @Describe("Whether players arriving here are welcomed and offered the way on to the network. Off, they "
                + "just stand here.")
        @Key("enabled")
        boolean enabled,

        @In("welcome") @Title("Send them on to")
        @Describe("The server players go to once they are done or skip — its name in the proxy's velocity.toml.")
        @Key("next-server")
        String nextServer,

        @In("welcome") @Title("Welcome after (seconds)") @Range(min = 0, max = 30)
        @Describe("How long after arriving the welcome window opens, so the world has loaded behind it.")
        @Key("offer-after-seconds")
        int offerAfterSeconds,

        @In("welcome") @Title("Keep newcomers safe")
        @Describe("Whether newcomers here take no damage and get no hungrier. Staff are never protected.")
        @Key("protect")
        boolean protect) {

    public static final WelcomeSettings DEFAULTS = new WelcomeSettings(true, "smp", 2, true);
}
