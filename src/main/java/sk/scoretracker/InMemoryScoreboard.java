package sk.scoretracker;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread safe implementation using in-memory storage for matches.
 */
public class InMemoryScoreboard implements Scoreboard {

    /**
     * Map of matchId to a running match.
     * (does not need to be volatile, as we are not reassigning the value)
     */
    private final ConcurrentMap<String, Match> runningMatches;

    public InMemoryScoreboard(ConcurrentMap<String, Match> runningMatches) {
        this.runningMatches = runningMatches;
    }

    @Override
    public Match startMatch(String homeTeam, String awayTeam) {
        var matchId = getMatchId(homeTeam, awayTeam);

        if (runningMatches.containsKey(matchId)) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " is already running.");
        }

        var result = new Match(homeTeam, awayTeam);
        runningMatches.put(matchId, result);
        return result;
    }

    @Override
    public Match updateMatchScore(String homeTeam, int homeScore, String awayTeam, int awayScore) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public Match finishMatch(String homeTeam, String awayTeam) {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public List<Match> getSummary() {
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    private String getMatchId(String homeTeam, String awayTeam) {
        // Ideally we would want a nicer non-string unique ID, but for simplicity
        // we now operate only on Strings names of teams.
        return homeTeam + "#" + awayTeam;
    }

}
