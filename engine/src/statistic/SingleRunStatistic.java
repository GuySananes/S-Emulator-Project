package statistic;

import java.util.List;

public interface SingleRunStatistic {
    int getRunNumber();

    int getRunDegree();

    List<Long> getInput();

    long getResult();

    long getCycles();

    String getRepresentation();


}
