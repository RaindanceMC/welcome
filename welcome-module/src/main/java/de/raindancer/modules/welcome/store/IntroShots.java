package de.raindancer.modules.welcome.store;

import de.raindancer.modules.welcome.model.Shot;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The intro's shots in {@code plugins/RainsWelcome/intro.yml}, in the order they play. Staff record them in game;
 * the file is this server's alone, like the rest of the welcome server.
 */
public final class IntroShots {

    private final Path file;
    private final List<Shot> shots = new CopyOnWriteArrayList<>();

    private IntroShots(Path file) {
        this.file = file;
    }

    public static IntroShots at(Path file) {
        IntroShots shots = new IntroShots(file);
        shots.load();
        return shots;
    }

    public List<Shot> all() {
        return List.copyOf(shots);
    }

    public synchronized void add(Shot shot) {
        shots.add(shot);
        save();
    }

    /** @param number 1-based, as /intro shot list shows it */
    public synchronized boolean remove(int number) {
        if (number < 1 || number > shots.size()) {
            return false;
        }
        shots.remove(number - 1);
        save();
        return true;
    }

    /** Moves shot {@code from} to position {@code to}, both 1-based. */
    public synchronized boolean move(int from, int to) {
        if (from < 1 || from > shots.size() || to < 1 || to > shots.size()) {
            return false;
        }
        Shot shot = shots.remove(from - 1);
        shots.add(to - 1, shot);
        save();
        return true;
    }

    private void load() {
        shots.clear();
        if (!Files.exists(file)) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file.toFile());
        for (Map<?, ?> entry : yaml.getMapList("shots")) {
            try {
                shots.add(new Shot(String.valueOf(entry.get("world")), number(entry.get("x")), number(entry.get("y")),
                        number(entry.get("z")), (float) number(entry.get("yaw")), (float) number(entry.get("pitch")),
                        entry.get("caption") == null ? "" : String.valueOf(entry.get("caption"))));
            } catch (RuntimeException broken) {
                // A hand-edited line that is not a shot: left out rather than losing the others.
            }
        }
    }

    private void save() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().setHeader(List.of("The intro's shots, in the order they play. Recorded with /intro shot add."));
        List<Map<String, Object>> out = new ArrayList<>();
        for (Shot shot : shots) {
            out.add(Map.of("world", shot.world(), "x", shot.x(), "y", shot.y(), "z", shot.z(), "yaw", (double) shot.yaw(),
                    "pitch", (double) shot.pitch(), "caption", shot.caption()));
        }
        yaml.set("shots", out);
        try {
            Files.createDirectories(file.getParent());
            yaml.save(file.toFile());
        } catch (IOException failed) {
            throw new UncheckedIOException(failed);
        }
    }

    private static double number(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(String.valueOf(value));
    }
}
