package de.raindancer.modules.welcome.tutorial;

import de.raindancer.modules.welcome.tutorial.TutorialRun.Phase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("a newcomer's way through the tutorial: walk, hear, try, next")
class TutorialRunTest {

    private static final Station HOMES = new Station("homes", org.bukkit.Material.RED_BED, List.of("sethome", "home"));
    private static final Station RULES = new Station("rules", org.bukkit.Material.WRITABLE_BOOK, List.of("rules"));
    private static final Station LOOK = new Station("look", org.bukkit.Material.SPYGLASS, List.of());

    @Test
    @DisplayName("walks to the first station, explains, waits for the task, then the next one")
    void flow() {
        TutorialRun run = new TutorialRun(List.of(HOMES, RULES));
        assertThat(run.phase()).isEqualTo(Phase.WALK);
        assertThat(run.station()).contains(HOMES);
        run.arrived();
        assertThat(run.phase()).isEqualTo(Phase.EXPLAIN);
        run.explained();
        assertThat(run.phase()).isEqualTo(Phase.TRY);
        assertThat(run.typed("/spawn")).isFalse();
        assertThat(run.typed("/sethome base")).isTrue();
        assertThat(run.phase()).isEqualTo(Phase.WALK);
        assertThat(run.station()).contains(RULES);
        assertThat(run.number()).isEqualTo(2);
    }

    @Test
    @DisplayName("the last task done: finished")
    void finishes() {
        TutorialRun run = new TutorialRun(List.of(RULES));
        run.arrived();
        run.explained();
        run.typed("/RULES");
        assertThat(run.phase()).isEqualTo(Phase.FINISHED);
        assertThat(run.station()).isEmpty();
    }

    @Test
    @DisplayName("a command counts with a plugin's prefix, or by any of the station's names")
    void commandNames() {
        TutorialRun run = new TutorialRun(List.of(HOMES));
        run.arrived();
        run.explained();
        assertThat(run.typed("/rainshomes:home")).isTrue();
    }

    @Test
    @DisplayName("typing the command before the guide is done talking counts too — they got it")
    void early() {
        TutorialRun run = new TutorialRun(List.of(HOMES, RULES));
        run.arrived();
        assertThat(run.typed("/sethome")).isTrue();
        assertThat(run.station()).contains(RULES);
        assertThat(new TutorialRun(List.of(HOMES)).typed("/sethome")).as("not while still walking there").isFalse();
    }

    @Test
    @DisplayName("a station with nothing to try goes on as soon as it is explained")
    void nothingToTry() {
        TutorialRun run = new TutorialRun(List.of(LOOK, RULES));
        run.arrived();
        run.explained();
        assertThat(run.station()).contains(RULES);
        assertThat(run.phase()).isEqualTo(Phase.WALK);
    }

    @Test
    @DisplayName("a step can be skipped; no stations at all is finished at once")
    void skip() {
        TutorialRun run = new TutorialRun(List.of(HOMES, RULES));
        run.skip();
        assertThat(run.station()).contains(RULES);
        assertThat(new TutorialRun(List.of()).phase()).isEqualTo(Phase.FINISHED);
    }

    @Test
    @DisplayName("how far along: station n of m")
    void counts() {
        TutorialRun run = new TutorialRun(List.of(HOMES, RULES));
        assertThat(run.number()).isEqualTo(1);
        assertThat(run.total()).isEqualTo(2);
    }
}
