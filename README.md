# Scoretracker Exercise

Interview exercise - Implementation of the Live Football World Cup Score Board as a simple library.

## Assumptions

- Methods are called in the order the events happened. (i.e., methods won't be called in wrong
  order: `Update("A", 1, "B", 2)`, `Finish("A", "B")`, `Start("A", "B")`)
- Methods related to a single match won't be called concurrently. (i.e., `Finish("A", "B")` won't be called, until
  previously called `Update("A", 1, "B", 2)` finishes.)
- If same event is called multiple times at the same time, we are not required to always return the same result. (i.e.,
  if `Start("A", "B")` is called by 2 threads at the same time, one of the method calls is allowed to throw an
  exception.)
- `Start` method will be called with only team names that are not currently playing any other game. (i.e., If `A` is
  already playing `B`, start won't be called with either `A` or `B` as a home/away team until the match is finished.)
- Negative score is not a valid score in Football. (i.e., `Update("A", -1, "B", 0)` should fail).
- Scores may never decrease in Football. (i.e., `Update("A", 1, "B", 0)`, `Update("A", 0, "B", 0)` should fail).
- Performance of `getSummary` method is not way more important than the performance of methods that perform updates.
- We always receive normalized team names. (i.e., After receiving team name `Canada`, we won't ever receive the name in
  a slightly different format: `CANADA`, `canada`, `CA`, etc.)
