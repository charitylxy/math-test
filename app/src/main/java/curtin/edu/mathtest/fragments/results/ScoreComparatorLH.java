package curtin.edu.mathtest.fragments.results;

import java.util.Comparator;

import curtin.edu.mathtest.classes.Result;

public class ScoreComparatorLH implements Comparator<Result> {
    @Override
    public int compare(Result r1, Result r2) {
        return Double.compare(r1.getScore(), r2.getScore());
    }
}