package statistic;

import java.util.ArrayList;
import java.util.List;

public interface SingleRunStatistic {
    int getRunNumber();

    int getRunDegree();

    List<Long> getInputCopy();

    long getResult();

    long getCycles();


}
