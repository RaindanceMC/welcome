package de.raindancer.modules.welcome.store;

import de.raindancer.modules.welcome.store.TutorialPlaces.Place;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("where the tutorial happens, kept in tutorial.yml")
class TutorialPlacesTest {

    @TempDir
    Path folder;

    @Test
    @DisplayName("the start and each station's place, kept across a restart; a station can be taken away")
    void keeps() {
        TutorialPlaces places = TutorialPlaces.at(folder.resolve("tutorial.yml"));
        places.start(new Place("tutorial", 0.5, 65, 0.5, 90, 0));
        places.station("homes", new Place("tutorial", 10.5, 66, 3.5, 0, 0));
        places.station("rules", new Place("tutorial", -8.5, 65, 12.5, 180, 0));

        TutorialPlaces again = TutorialPlaces.at(folder.resolve("tutorial.yml"));
        assertThat(again.start()).contains(new Place("tutorial", 0.5, 65, 0.5, 90, 0));
        assertThat(again.stations()).containsOnlyKeys("homes", "rules");
        assertThat(again.stations().get("homes").x()).isEqualTo(10.5);

        assertThat(again.removeStation("rules")).isTrue();
        assertThat(again.removeStation("rules")).isFalse();
        assertThat(TutorialPlaces.at(folder.resolve("tutorial.yml")).stations()).containsOnlyKeys("homes");
    }

    @Test
    @DisplayName("ready once there is a start and at least one station")
    void ready() {
        TutorialPlaces places = TutorialPlaces.at(folder.resolve("tutorial.yml"));
        assertThat(places.ready()).isFalse();
        places.start(new Place("tutorial", 0, 64, 0, 0, 0));
        assertThat(places.ready()).isFalse();
        places.station("homes", new Place("tutorial", 5, 64, 0, 0, 0));
        assertThat(places.ready()).isTrue();
    }
}
