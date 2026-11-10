package de.raindancer.modules.welcome.store;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Where the tutorial happens, in {@code plugins/RainsWelcome/tutorial.yml}: where a newcomer and their guide start,
 * and where each station is. Staff set them in game with /tutorial start set and /tutorial station set.
 */
public final class TutorialPlaces {

    public record Place(String world, double x, double y, double z, float yaw, float pitch) {

        public static Place of(Location at) {
            return new Place(at.getWorld().getName(), at.getX(), at.getY(), at.getZ(), at.getYaw(), at.getPitch());
        }

        public Optional<Location> in(Server server) {
            World found = server.getWorld(world);
            return found == null ? Optional.empty() : Optional.of(new Location(found, x, y, z, yaw, pitch));
        }
    }

    private final Path file;
    private Place start;
    private final Map<String, Place> stations = new LinkedHashMap<>();

    private TutorialPlaces(Path file) {
        this.file = file;
    }

    public static TutorialPlaces at(Path file) {
        TutorialPlaces places = new TutorialPlaces(file);
        places.load();
        return places;
    }

    public synchronized Optional<Place> start() {
        return Optional.ofNullable(start);
    }

    public synchronized Map<String, Place> stations() {
        return Map.copyOf(stations);
    }

    public synchronized boolean ready() {
        return start != null && !stations.isEmpty();
    }

    public synchronized void start(Place place) {
        start = place;
        save();
    }

    public synchronized void station(String id, Place place) {
        stations.put(id, place);
        save();
    }

    public synchronized boolean removeStation(String id) {
        if (stations.remove(id) == null) {
            return false;
        }
        save();
        return true;
    }

    private void load() {
        if (!Files.exists(file)) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        start = read(yaml.getConfigurationSection("start"));
        ConfigurationSection all = yaml.getConfigurationSection("stations");
        if (all != null) {
            for (String id : all.getKeys(false)) {
                Place place = read(all.getConfigurationSection(id));
                if (place != null) {
                    stations.put(id, place);
                }
            }
        }
    }

    private static Place read(ConfigurationSection section) {
        if (section == null || !section.isString("world")) {
            return null;
        }
        return new Place(section.getString("world"), section.getDouble("x"), section.getDouble("y"),
                section.getDouble("z"), (float) section.getDouble("yaw"), (float) section.getDouble("pitch"));
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().setHeader(List.of("Where the tutorial happens. Set in game with /tutorial start set and "
                + "/tutorial station set <id>."));
        if (start != null) {
            write(yaml.createSection("start"), start);
        }
        ConfigurationSection all = yaml.createSection("stations");
        stations.forEach((id, place) -> write(all.createSection(id), place));
        try {
            Files.createDirectories(file.getParent());
            yaml.save(file.toFile());
        } catch (IOException failed) {
            throw new UncheckedIOException(failed);
        }
    }

    private static void write(ConfigurationSection section, Place place) {
        section.set("world", place.world());
        section.set("x", place.x());
        section.set("y", place.y());
        section.set("z", place.z());
        section.set("yaw", (double) place.yaw());
        section.set("pitch", (double) place.pitch());
    }
}
