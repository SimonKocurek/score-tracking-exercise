package sk.scoretracker;

import java.util.List;
import java.util.Map;

public class InMemoryScoreboard implements Scoreboard {

    private final Map<String, Match> runningMatches;

    public InMemoryScoreboard(Map<String, Match> runningMatches) {
        this.runningMatches = runningMatches;
    }

    @Override
    public Match startMatch(String homeTeam, String awayTeam) {
        throw new UnsupportedOperationException("Method not implemented yet.");
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

}
