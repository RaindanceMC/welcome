package de.raindancer.modules.welcome.model;

import de.raindancer.core.ui.camera.CameraPath;

import java.util.List;
import java.util.Optional;

/** One shot of the intro: where the camera is, how it looks, and what it says there (blank: nothing). */
public record Shot(String world, double x, double y, double z, float yaw, float pitch, String caption) {

    public Shot {
        caption = caption == null ? "" : caption;
    }

    /** The world the intro flies in: the first shot's. Shots elsewhere are left out. */
    public static Optional<String> world(List<Shot> shots) {
        return shots.isEmpty() ? Optional.empty() : Optional.of(shots.getFirst().world());
    }

    /** How many shots are in that world. */
    public static int usable(List<Shot> shots) {
        return world(shots).map(world -> (int) shots.stream().filter(shot -> shot.world().equals(world)).count())
                .orElse(0);
    }

    /** @param secondsPerShot how long the glide from one shot to the next takes */
    public static CameraPath path(List<Shot> shots, double secondsPerShot) {
        String world = world(shots).orElseThrow(() -> new IllegalArgumentException("no shots"));
        return CameraPath.through(shots.stream().filter(shot -> shot.world().equals(world))
                .map(shot -> new CameraPath.Key(shot.x(), shot.y(), shot.z(), shot.yaw(), shot.pitch(), shot.caption()))
                .toList(), secondsPerShot);
    }
}
