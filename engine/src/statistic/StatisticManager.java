package statistic;

import java.util.*;

public class StatisticManager{

    private static final StatisticManager instance = new StatisticManager();
    public static StatisticManager getInstance() {return instance;}
    private StatisticManager() {}

    private final Map<String, List<SingleRunStatistic>> statisticMap = new HashMap<>();

    private final Map<String, Integer> runCount = new HashMap<>();

    private final Map<String, List<SingleRunStatistic>> userStatisticMap = new HashMap<>();

    private final Map<String, Integer> userRunCount = new HashMap<>();

    private static final int startCount = 1;


    public int getStartCount() {
        return startCount;
    }

    public List<SingleRunStatistic> getProgramStatistics(String progName) {
        return Objects.requireNonNullElse(statisticMap.get(progName), List.of());
    }

    public List<SingleRunStatistic> getUserStatistics(String username) {
        return Objects.requireNonNullElse(userStatisticMap.get(username), List.of());
    }

    public void addRunStatistic(String progName, SingleRunStatistic statistic) {
        statisticMap.computeIfAbsent(progName, pn -> new ArrayList<>()).add(statistic);
    }

    public void addUserRunStatistic(String username, SingleRunStatistic statistic) {
        userStatisticMap.computeIfAbsent(username, un -> new ArrayList<>()).add(statistic);
    }

    public int getRunCount(String progName) {
        return runCount.computeIfAbsent(progName, p -> 0);
    }

    public void incrementRunCount(String progName) {
        runCount.put(progName, getRunCount(progName) + 1);
    }

    public int getUserRunCount(String username) {
        return userRunCount.computeIfAbsent(username, u -> 0);
    }

    public void incrementUserRunCount(String username) {
        userRunCount.put(username, getUserRunCount(username) + 1);
    }
}