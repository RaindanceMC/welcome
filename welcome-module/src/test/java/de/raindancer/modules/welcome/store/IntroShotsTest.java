package de.raindancer.modules.welcome.store;

import de.raindancer.modules.welcome.model.Shot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("the intro's shots, kept in intro.yml")
class IntroShotsTest {

    @TempDir
    Path folder;

    @Test
    @DisplayName("added, listed in order, kept across a restart, moved and removed")
    void keeps() {
        IntroShots shots = IntroShots.at(folder.resolve("intro.yml"));
        shots.add(new Shot("intro", 1, 2, 3, 4, 5, "One"));
        shots.add(new Shot("intro", 6, 7, 8, 9, 10, "Two: with a colon"));
        shots.add(new Shot("intro", 11, 12, 13, 14, 15, ""));

        IntroShots again = IntroShots.at(folder.resolve("intro.yml"));
        assertThat(again.all()).extracting(Shot::caption).containsExactly("One", "Two: with a colon", "");
        assertThat(again.all().get(1).yaw()).isEqualTo(9f);

        assertThat(again.move(3, 1)).isTrue();
        assertThat(again.all()).extracting(Shot::caption).containsExactly("", "One", "Two: with a colon");
        assertThat(again.remove(2)).isTrue();
        assertThat(again.remove(9)).isFalse();
        assertThat(IntroShots.at(folder.resolve("intro.yml")).all()).extracting(Shot::caption)
                .containsExactly("", "Two: with a colon");
    }

    @Test
    @DisplayName("no file yet: no shots")
    void empty() {
        assertThat(IntroShots.at(folder.resolve("missing.yml")).all()).isEmpty();
    }
}
