package sk.scoretracker;

import java.time.Instant;

/**
 * A single world cup match with mutable score.
 */
public record Match(
        String homeTeam,
        int homeScore,
        String awayTeam,
        int awayScore,
        Instant startedAt
) {
    public Match(String homeTeam, String awayTeam) {
        this(homeTeam, 0, awayTeam, 0, Instant.now());
    }

    public Match withUpdatedScore(int homeScore, int awayScore) {
        return new Match(homeTeam, homeScore, awayTeam, awayScore, startedAt);
    }

    /**
     * @return Home and away score summed up.
     */
    int totalScore() {
        return homeScore + awayScore;
    }
}
