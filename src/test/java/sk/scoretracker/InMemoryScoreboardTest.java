package sk.scoretracker;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;

class InMemoryScoreboardTest {

    @Test
    void startMatchCanStartMultipleMatches() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        Scoreboard scorebaord = new InMemoryScoreboard(matches);

        // When
        scorebaord.startMatch("Mexico", "Canada");
        var match2 = scorebaord.startMatch("Spain", "Brazil");
        scorebaord.startMatch("Germany", "France");

        // Then
        Assertions.assertEquals(3, matches.size());

        Assertions.assertEquals("Spain", match2.homeTeam());
        Assertions.assertEquals(0, match2.homeScore());
        Assertions.assertEquals("Brazil", match2.awayTeam());
        Assertions.assertEquals(0, match2.awayScore());
        assertFalse(match2.startedAt().isAfter(Instant.now()), "Started time should not be in future.");
    }

    @Test
    void startMatchCannotStartSameMatchMultipleTimes() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        Scoreboard scorebaord = new InMemoryScoreboard(matches);

        // When
        var match = scorebaord.startMatch("Mexico", "Canada");

        // Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scorebaord.startMatch("Mexico", "Canada")
        );

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(match, matches.values().stream().findFirst().orElseThrow());
    }

}
