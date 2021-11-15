package curtin.edu.mathtest.classes;

import java.io.Serializable;
import java.sql.Time;
import java.util.Comparator;
import java.util.Timer;
import java.util.UUID;

public class Result implements Serializable{
    private String id;
    private String student_id;
    private String start_time;
    private String duration;
    private double score;

    public Result(String id, String student_id, String start_time, String duration, double score) {
        this.id = id;
        this.student_id = student_id;
        this.start_time = start_time;
        this.duration = duration;
        this.score = score;
    }

    public Result(String student_id, String start_time, String duration, double score) {
        this (UUID.randomUUID().toString(), student_id, start_time, duration, score);
    }

    public String getId() {
        return id;
    }

    public String getStudent_id() {
        return student_id;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getDuration() {
        return duration;
    }

    public double getScore() {
        return score;
    }
}
