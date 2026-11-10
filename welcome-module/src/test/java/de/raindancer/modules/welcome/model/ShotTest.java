package de.raindancer.modules.welcome.model;

import de.raindancer.core.ui.camera.CameraPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("recorded shots become the intro's camera path")
class ShotTest {

    private static final List<Shot> SHOTS = List.of(
            new Shot("intro", 0, 80, 0, 10, 20, "Welcome to Hyades"),
            new Shot("intro", 30, 85, 5, 90, 10, "Guild halls"),
            new Shot("other", 99, 99, 99, 0, 0, "somewhere else"),
            new Shot("intro", 60, 90, 40, 180, 0, ""));

    @Test
    @DisplayName("the path runs through the first shot's world, in order, captions kept")
    void path() {
        CameraPath path = Shot.path(SHOTS, 6);
        assertThat(path.keys()).hasSize(3);
        assertThat(path.keys().get(1).caption()).isEqualTo("Guild halls");
        assertThat(path.keys().get(2).x()).isEqualTo(60);
        assertThat(path.seconds()).isEqualTo(12.0);
    }

    @Test
    @DisplayName("which world that is, and how many shots are in it")
    void world() {
        assertThat(Shot.world(SHOTS)).contains("intro");
        assertThat(Shot.usable(SHOTS)).isEqualTo(3);
        assertThat(Shot.world(List.of())).isEmpty();
        assertThat(Shot.usable(List.of())).isZero();
    }
}
