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

    @Test
    void finishMatchCanFinishAllMatches() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var match1 = new Match("Mexico", "Canada");
        var match2 = new Match("Spain", "France");
        matches.put(scorebaord.getMatchId(match1.homeTeam(), match1.awayTeam()), match1);
        matches.put(scorebaord.getMatchId(match2.homeTeam(), match2.awayTeam()), match2);

        // When
        var returnedMatch1 = scorebaord.finishMatch(match1.homeTeam(), match1.awayTeam());
        scorebaord.finishMatch(match2.homeTeam(), match2.awayTeam());

        // Then
        Assertions.assertEquals(0, matches.size(), "All matches should be finished");
        Assertions.assertEquals(match1, returnedMatch1, "Match should not be modified when finished");
    }

    @Test
    void finishMatchDoesOnlyFinishesSpecifiedMatch() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var match1 = new Match("Mexico", "Canada");
        var match2 = new Match("Spain", "France");
        matches.put(scorebaord.getMatchId(match1.homeTeam(), match1.awayTeam()), match1);
        matches.put(scorebaord.getMatchId(match2.homeTeam(), match2.awayTeam()), match2);

        // When
        var returnedMatch1 = scorebaord.finishMatch(match1.homeTeam(), match1.awayTeam());

        // Then
        Assertions.assertEquals(1, matches.size(), "Finishing one match should not finish all matches");
        Assertions.assertEquals(match1, returnedMatch1, "Match should not be modified when finished");
    }

    @Test
    void finishMatchFailsOnNonRunningMatch() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var match = new Match("Mexico", "Canada");
        matches.put(scorebaord.getMatchId(match.homeTeam(), match.awayTeam()), match);

        // When, Then
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                scorebaord.finishMatch("Spain", "France")
        );

        Assertions.assertEquals(1, matches.size(), "Existing match should not be finished");
    }

    @Test
    void getSummaryCanReturnSummaryWhenNoMatchesAreRunning() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        // When
        var summary = scorebaord.getSummary();

        // Then
        Assertions.assertEquals(0, summary.size(), "Summary should be empty");
    }

    @Test
    void getSummaryCanReturnComplexSummary() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();
        var scorebaord = new InMemoryScoreboard(matches);

        var match = new Match("Mexico", 0, "Canada", 5, Instant.now().minusSeconds(3600));
        var match2 = new Match("Spain", 10, "Brazil", 2, Instant.now().minusSeconds(3600));
        var match3 = new Match("Germany", 2, "France", 2, Instant.now().minusSeconds(3600));
        var match4 = new Match("Uruguay", 6, "Italy", 6, Instant.now());
        var match5 = new Match("Argentina", 3, "Australia", 1, Instant.now());
        matches.put(scorebaord.getMatchId(match.homeTeam(), match.awayTeam()), match);
        matches.put(scorebaord.getMatchId(match2.homeTeam(), match2.awayTeam()), match2);
        matches.put(scorebaord.getMatchId(match3.homeTeam(), match3.awayTeam()), match3);
        matches.put(scorebaord.getMatchId(match4.homeTeam(), match4.awayTeam()), match4);
        matches.put(scorebaord.getMatchId(match5.homeTeam(), match5.awayTeam()), match5);

        // When
        var summary = scorebaord.getSummary();

        // Then
        Assertions.assertEquals(5, summary.size(), "Summary should be of the same size as running matches");

        Assertions.assertEquals(match4, summary.get(0), "Match with highest total score that is newest should be first.");
        Assertions.assertEquals(match2, summary.get(1));
        Assertions.assertEquals(match, summary.get(2));
        Assertions.assertEquals(match5, summary.get(3));
        Assertions.assertEquals(match3, summary.get(4));
    }

    @Test
    void getTeamScoreCanGetScoreOfHomeTeam() {
        // Given
        var matches = new ConcurrentHashMap<String, Match>();

        var match = new Match("Spain", 10, "Brazil", 2, Instant.now());
        var match2 = new Match("Germany", 2, "France", 2, Instant.now());
        matches.put(InMemoryScoreboard.getMatchId(match.homeTeam(), match.awayTeam()), match);
        matches.put(InMemoryScoreboard.getMatchId(match2.homeTeam(), match2.awayTeam()), match2);

        var scorebaord = new InMemoryScoreboard(matches);

        // When
        var score = scorebaord.getTeamScore("Spain");

        // Then
        Assertions.assertEquals(10, score);
    }

}
