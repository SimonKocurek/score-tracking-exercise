package sk.scoretracker;

import java.util.List;

/**
 * Live Football World Cup Scoreboard that shows all the ongoing matches and their scores.
 */
public interface Scoreboard {

    /**
     * Start a new match, assuming initial score 0 – 0 and adding it the scoreboard.
     * @throws IllegalArgumentException if match is already in progress.
     */
    Match startMatch(String homeTeam, String awayTeam);

    /**
     * Update score of a match.
     * @throws IllegalArgumentException if match is not currently in progress.
     */
    Match updateMatchScore(String homeTeam, int homeScore, String awayTeam, int awayScore);

    /**
     * Finish match currently in progress. This removes a match from the scoreboard.
     * @throws IllegalArgumentException if match is not currently in progress.
     */
    Match finishMatch(String homeTeam, String awayTeam);

    /**
     * Get a summary of matches in progress ordered by their total score. The matches with the
     * same total score will be returned ordered by the most recently started match in the
     * scoreboard.
     */
    List<Match> getSummary();

}
