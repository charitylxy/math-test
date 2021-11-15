package curtin.edu.mathtest.classes;

import java.util.List;

public class Question {
    private String question;
    private List<Integer> options;
    private String result;
    private Long timeToSolve;

    public Question() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Integer> getOptions() {
        return options;
    }

    public void setOptions(List<Integer> options) {
        this.options = options;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Long getTimeToSolve() {
        return timeToSolve;
    }

    public void setTimeToSolve(Long timeToSolve) {
        this.timeToSolve = timeToSolve;
    }
}
