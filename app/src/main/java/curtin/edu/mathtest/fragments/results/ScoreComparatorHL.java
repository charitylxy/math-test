package curtin.edu.mathtest.fragments.results;

import java.util.Comparator;

import curtin.edu.mathtest.classes.Result;

public class ScoreComparatorHL implements Comparator<Result> {
    @Override
    public int compare(Result r1, Result r2) {
        return Double.compare(r2.getScore(), r1.getScore());
    }
}