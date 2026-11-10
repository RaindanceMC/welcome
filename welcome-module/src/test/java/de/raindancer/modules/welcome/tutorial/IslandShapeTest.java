package de.raindancer.modules.welcome.tutorial;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("the generated tutorial island's shape")
class IslandShapeTest {

    private final IslandShape island = new IslandShape(40, 7L);

    @Test
    @DisplayName("land in the middle, sea far out; the coast wobbles but stays round")
    void land() {
        assertThat(island.height(0, 0)).isPresent();
        assertThat(island.height(60, 0)).isEmpty();
        assertThat(island.height(0, -60)).isEmpty();
        assertThat(island.height(30, 0)).as("well inside the radius").isPresent();
    }

    @Test
    @DisplayName("gentle: the middle a few blocks above the sea, never cliffs between neighbours")
    void gentle() {
        assertThat(island.height(0, 0).orElseThrow()).isBetween(3, 7);
        for (int x = -38; x < 38; x++) {
            for (int z = -38; z < 38; z++) {
                var here = island.height(x, z);
                var east = island.height(x + 1, z);
                if (here.isPresent() && east.isPresent()) {
                    assertThat(Math.abs(here.get() - east.get())).as("at %d %d", x, z).isLessThanOrEqualTo(1);
                }
            }
        }
    }

    @Test
    @DisplayName("the same seed, the same island")
    void deterministic() {
        assertThat(new IslandShape(40, 7L).height(12, -9)).isEqualTo(island.height(12, -9));
    }

    @Test
    @DisplayName("stations in a ring around the middle, evenly spread, all on land")
    void ring() {
        List<int[]> spots = island.ring(9);
        assertThat(spots).hasSize(9);
        for (int[] spot : spots) {
            assertThat(Math.hypot(spot[0], spot[1])).isCloseTo(24, within(1.5));
            assertThat(island.height(spot[0], spot[1])).isPresent();
        }
    }
}
