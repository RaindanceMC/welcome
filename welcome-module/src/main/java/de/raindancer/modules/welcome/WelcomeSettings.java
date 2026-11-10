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

        @In("welcome") @Title("This server is")
        @Describe("welcome: the welcome server itself — greets newcomers, plays the intro, runs the tutorial. network: "
                + "any other server of the network (the SMP): only /trailer, which sends players to the welcome "
                + "server to watch the intro. Takes effect at the next restart.")
        @Key("role")
        String role,

        @In("welcome") @Title("The welcome server")
        @Describe("Where /trailer sends players from a network server — its name in the proxy's velocity.toml.")
        @Key("welcome-server")
        String welcomeServer,

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
        boolean protect,

        @In("welcome") @Title("Intro on")
        @Describe("Whether newcomers watch the intro — a camera flight through the shots recorded with /intro shot "
                + "add — before the welcome window. Needs at least two shots; sneaking skips it.")
        @Key("intro-enabled")
        boolean introEnabled,

        @In("welcome") @Title("Seconds from shot to shot") @Range(min = 2, max = 30)
        @Describe("How long the camera glides from one shot to the next. Slower looks calmer.")
        @Key("seconds-per-shot")
        int secondsPerShot,

        @In("welcome") @Title("Intro music")
        @Describe("custom: the server's own tracks — every .ogg (Vorbis) in plugins/RainsWelcome/music, shuffled; "
                + "they reach players through the resource pack, and whoever has no pack hears the game's menu "
                + "music instead. Or a sound key like minecraft:music.menu. Empty plays nothing. New tracks are "
                + "picked up at the next restart.")
        @Key("music")
        String music,

        @In("welcome") @Title("Music credit")
        @Describe("Shown under the intro while it plays — the credit a Creative Commons Attribution (CC-BY) track "
                + "asks for, e.g. Music: Rise of Spirit by Alexandr Zhelanov (CC-BY 3.0). Empty shows none.")
        @Key("music-credit")
        String musicCredit,

        @In("welcome") @Title("Tutorial on")
        @Describe("Whether the welcome window offers the tour of the tutorial island, where a guide walks newcomers "
                + "round the stations. Needs a start and at least one station (/tutorial island generate, or "
                + "/tutorial start set and /tutorial station set <id>).")
        @Key("tutorial-enabled")
        boolean tutorialEnabled,

        @In("welcome") @Title("The guide's name")
        @Key("guide-name")
        @Describe("What the tutorial's guide is called.")
        String guideName,

        @In("welcome") @Title("The guide's skin")
        @Describe("A player name whose skin the guide wears, or a MineSkin skin (mineskin:<id>). Empty: a suit.")
        @Key("guide-skin")
        String guideSkin) {

    public static final WelcomeSettings DEFAULTS = new WelcomeSettings("welcome", "welcome", true, "smp", 2, true,
            true, 6, "custom", "", true, "Iris", "");
}
