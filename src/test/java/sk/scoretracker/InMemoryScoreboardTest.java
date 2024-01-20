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
        var scorebaord = new InMemoryScoreboard(matches);

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
        var scorebaord = new InMemoryScoreboard(matches);

        // When
        var match = scorebaord.startMatch("Mexico", "Canada");

        // Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scorebaord.startMatch("Mexico", "Canada")
        );

        Assertions.assertEquals(1, matches.size());
        Assertions.assertEquals(match, matches.values().stream().findFirst().orElseThrow());
    }

    @Test
    void updateMatchScoreCanUpdateScoreMultipleTimes() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var matchBeforeUpdate = new Match("Mexico", "Canada");
        var matchBeforeUpdate2 = new Match("Spain", "France");
        matches.put(scorebaord.getMatchId(matchBeforeUpdate.homeTeam(), matchBeforeUpdate.awayTeam()), matchBeforeUpdate);
        matches.put(scorebaord.getMatchId(matchBeforeUpdate2.homeTeam(), matchBeforeUpdate2.awayTeam()), matchBeforeUpdate2);

        // When
        scorebaord.updateMatchScore(matchBeforeUpdate.homeTeam(), 1, matchBeforeUpdate.awayTeam(), 1);
        scorebaord.updateMatchScore(matchBeforeUpdate.homeTeam(), 1, matchBeforeUpdate.awayTeam(), 1);
        var finalUpdatedScore = scorebaord.updateMatchScore(matchBeforeUpdate.homeTeam(), 2, matchBeforeUpdate.awayTeam(), 1);

        // Then
        Assertions.assertEquals(2, matches.size(), "No new match should be started or terminated.");

        var matchAfterUpdate = matches.get(scorebaord.getMatchId(matchBeforeUpdate.homeTeam(), matchBeforeUpdate.awayTeam()));
        Assertions.assertEquals(matchAfterUpdate, finalUpdatedScore, "Returned value should be the same as internally stored value.");

        Assertions.assertEquals(matchBeforeUpdate.homeTeam(), matchAfterUpdate.homeTeam());
        Assertions.assertEquals(2, matchAfterUpdate.homeScore());
        Assertions.assertEquals(matchBeforeUpdate.awayTeam(), matchAfterUpdate.awayTeam());
        Assertions.assertEquals(1, matchAfterUpdate.awayScore());
        Assertions.assertEquals(matchBeforeUpdate.startedAt(), matchAfterUpdate.startedAt(), "Start time should not change");
    }

    @Test
    void updateMatchScoreFailsIfMatchIsNotRunning() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var matchBeforeUpdate = new Match("Mexico", "Canada");
        matches.put(scorebaord.getMatchId(matchBeforeUpdate.homeTeam(), matchBeforeUpdate.awayTeam()), matchBeforeUpdate);

        // When, Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scorebaord.updateMatchScore("Spain", 1, "Canada", 1)
        );

        Assertions.assertEquals(1, matches.size(), "No new match should be started");
    }

    @Test
    void updateMatchScoreFailsIfScoreDecreases() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var matchBeforeUpdate = new Match("Mexico", 10, "Canada", 10, Instant.now());
        matches.put(scorebaord.getMatchId(matchBeforeUpdate.homeTeam(), matchBeforeUpdate.awayTeam()), matchBeforeUpdate);

        // When, Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scorebaord.updateMatchScore(matchBeforeUpdate.homeTeam(), 1, matchBeforeUpdate.awayTeam(), 1)
        );

        Assertions.assertEquals(1, matches.size(), "No new match should be started");
    }

}
