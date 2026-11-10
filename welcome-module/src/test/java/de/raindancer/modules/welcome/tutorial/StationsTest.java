package de.raindancer.modules.welcome.tutorial;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("the tutorial's stations")
class StationsTest {

    @Test
    @DisplayName("the topics Tom asked for, in an order that builds up: money first, the rules last")
    void topics() {
        assertThat(Stations.ALL).extracting(Station::id).containsExactly("economy", "cosmetics", "claims", "homes",
                "travel", "jobs", "skills", "guilds", "rules");
    }

    @Test
    @DisplayName("only the placed ones are walked, in the stations' order, not the order they were placed in")
    void placed() {
        assertThat(Stations.placed(Set.of("rules", "economy", "nonsense"))).extracting(Station::id)
                .containsExactly("economy", "rules");
    }

    @Test
    @DisplayName("each has a message key for its title, its words and its hint")
    void keys() {
        Station homes = Stations.byId("homes").orElseThrow();
        assertThat(homes.key("title")).isEqualTo("welcome.tutorial.homes.title");
        assertThat(Stations.byId("nope")).isEmpty();
        assertThat(Map.of()).isEmpty();
    }
}
