package sk.scoretracker;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Concurrent implementation using in-memory storage for matches.
 * <p>
 * Can be used by multiple threads concurrently. However, does not guarantee
 * synchronization of operations performed on a single match. (i.e, multiple
 * threads can update scoreboard at the same time, but there should not be
 * multiple updates on a particular match at the same time.)
 */
public class InMemoryScoreboard implements Scoreboard {

    /**
     * Map of matchId to a running match.
     * (does not need to be volatile, as we are not reassigning the value)
     */
    private final ConcurrentMap<String, Match> runningMatches;

    /**
     *
     */
    private final ConcurrentMap<String, Integer> teamScores;

    public InMemoryScoreboard(ConcurrentMap<String, Match> runningMatches) {
        this.runningMatches = runningMatches;

        this.teamScores = new ConcurrentHashMap<>();
        for (Match match : runningMatches.values()) {
            teamScores.put(match.homeTeam(), match.homeScore());
            teamScores.put(match.awayTeam(), match.awayScore());
        }
    }

    @Override
    public Match startMatch(String homeTeam, String awayTeam) {
        var matchId = getMatchId(homeTeam, awayTeam);

        if (runningMatches.containsKey(matchId)) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " is already running.");
        }

        var result = new Match(homeTeam, awayTeam);

        teamScores.put(homeTeam, result.homeScore());
        teamScores.put(awayTeam, result.awayScore());
        runningMatches.put(matchId, result);
        return result;
    }

    @Override
    public Match updateMatchScore(String homeTeam, int homeScore, String awayTeam, int awayScore) {
        var matchId = getMatchId(homeTeam, awayTeam);

        var match = runningMatches.getOrDefault(matchId, null);
        if (match == null) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " is not running.");
        }

        // We don't need to check for score < 0, because Match score always starts at 0, as per the interface contract.
        if (homeScore < match.homeScore()) {
            throw new IllegalArgumentException("Score " + homeScore + " for home team " + homeTeam + " cannot be smaller than their current score " + match.homeScore() + " . Decreasing scores in Football are not allowed.");
        }
        if (awayScore < match.awayScore()) {
            throw new IllegalArgumentException("Score " + awayScore + " for away team " + awayTeam + " cannot be smaller than their current score " + match.awayScore() + " . Decreasing scores in Football are not allowed.");
        }

        var result = match.withUpdatedScore(homeScore, awayScore);
        teamScores.put(homeTeam, result.homeScore());
        teamScores.put(awayTeam, result.awayScore());
        runningMatches.put(matchId, result);
        return result;
    }

    @Override
    public Match finishMatch(String homeTeam, String awayTeam) {
        var matchId = getMatchId(homeTeam, awayTeam);

        var match = runningMatches.getOrDefault(matchId, null);
        if (match == null) {
            throw new IllegalArgumentException("Match between " + homeTeam + " and " + awayTeam + " is not running.");
        }

        teamScores.remove(homeTeam);
        teamScores.remove(awayTeam);
        runningMatches.remove(matchId);
        return match;
    }

    @Override
    public List<Match> getSummary() {
        // If this method becomes the main bottleneck, it is possible to store pre-sorted results
        // in TreeMap during `startMatch`, `updateMatchScore` and `finishMatch`. This would reduce
        // the time complexity of this method from O(n log n) down to O(n)... but would increase
        // complexities of other methods from O(1) to O(log n) and O(n) in the case of finish.

        return runningMatches
                .values()
                .stream()
                .sorted(
                        Comparator
                                .comparingInt(Match::totalScore)
                                .thenComparing(Match::startedAt)
                                .reversed()
                ).toList();
    }

    @Override
    public int getTeamScore(String teamName) {
        return teamScores.get(teamName);
    }

    static String getMatchId(String homeTeam, String awayTeam) {
        // Ideally we would want a nicer non-string unique ID, but for simplicity
        // we now operate only on Strings names of teams.
        return homeTeam + "#" + awayTeam;
    }

}
