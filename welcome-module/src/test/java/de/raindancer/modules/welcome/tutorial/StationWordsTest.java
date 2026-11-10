package de.raindancer.modules.welcome.tutorial;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("every station has its words in messages.yml — their keys are built, so the key scan cannot see them")
class StationWordsTest {

    @Test
    void everyStationSpeaks() {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(
                Path.of("src/main/resources/de/raindancer/modules/welcome/messages.yml").toFile());
        for (Station station : Stations.ALL) {
            for (String part : List.of("title", "say1", "say2", "hint")) {
                assertThat(yaml.isString(station.key(part))).as(station.key(part)).isTrue();
            }
        }
    }
}
