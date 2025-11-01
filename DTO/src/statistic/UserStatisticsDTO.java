package statistic;

import exception.ProgramNotExecutedYetException;

import java.util.ArrayList;
import java.util.List;

public class UserStatisticsDTO {

    private final List<SingleRunStatisticDTO> statistics;
    private final String username;

    public UserStatisticsDTO(String username) throws ProgramNotExecutedYetException {
        this.username = username;
        List<SingleRunStatistic> userStats = StatisticManager.getInstance().getUserStatistics(username);

        if (userStats == null || userStats.isEmpty()) {
            throw new ProgramNotExecutedYetException("User " + username + " has no execution history");
        }

        this.statistics = new ArrayList<>();
        for (SingleRunStatistic stat : userStats) {
            this.statistics.add(new SingleRunStatisticDTO(stat));
        }
    }

    public String getUsername() {
        return username;
    }

    public List<SingleRunStatisticDTO> getStatistics() {
        return statistics;
    }

    public String getRepresentation() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistics for user: ").append(username).append("\n\n");
        for (SingleRunStatisticDTO stat : statistics) {
            sb.append(stat.getRepresentation()).append("\n\n");
        }
        return sb.toString().trim();
    }
}