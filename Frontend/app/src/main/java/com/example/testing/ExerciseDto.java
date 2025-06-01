package com.example.testing;

public class ExerciseDto {

    private Long id;
    private String exerciseName;

    private String data;

    private int exerciseBpm;

    public ExerciseDto(String exerciseName, String data, int exerciseBpm) {
        this.exerciseName = exerciseName;
        this.data = data;
        this.exerciseBpm = exerciseBpm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getExerciseBpm() {
        return exerciseBpm;
    }

    public void setExerciseBpm(int exerciseBpm) {
        this.exerciseBpm = exerciseBpm;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
