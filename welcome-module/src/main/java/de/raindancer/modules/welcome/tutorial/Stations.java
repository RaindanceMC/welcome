package de.raindancer.modules.welcome.tutorial;

import org.bukkit.Material;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** The tutorial's stations, in the order they are walked. Their words are in messages.yml. */
public final class Stations {

    public static final List<Station> ALL = List.of(
            new Station("economy", Material.EMERALD, List.of("balance", "bal", "shop", "sell", "market")),
            new Station("cosmetics", Material.FIREWORK_ROCKET, List.of("cosmetics")),
            new Station("claims", Material.GOLDEN_SHOVEL, List.of("claim")),
            new Station("homes", Material.RED_BED, List.of("sethome", "home")),
            new Station("travel", Material.ENDER_PEARL, List.of("warp", "tpa", "spawn")),
            new Station("jobs", Material.IRON_PICKAXE, List.of("jobs", "quests")),
            new Station("skills", Material.EXPERIENCE_BOTTLE, List.of("skills")),
            new Station("guilds", Material.WHITE_BANNER, List.of("guild")),
            new Station("rules", Material.WRITABLE_BOOK, List.of("rules")));

    private Stations() {
    }

    public static Optional<Station> byId(String id) {
        return ALL.stream().filter(station -> station.id().equalsIgnoreCase(id)).findFirst();
    }

    /** The stations that have a place, in their order. */
    public static List<Station> placed(Collection<String> ids) {
        return ALL.stream().filter(station -> ids.contains(station.id())).toList();
    }
}
