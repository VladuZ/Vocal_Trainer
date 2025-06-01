package com.kpi.diploma.model;

import com.kpi.diploma.entity.Exercise;

public class ExerciseDto {

    private String exerciseName;

    private byte[] data;

    private Long id;

    private int exerciseBpm;

    public ExerciseDto(String exerciseName, byte[] data, Long id, int exerciseBpm) {
        this.exerciseName = exerciseName;
        this.data = data;
        this.id = id;
        this.exerciseBpm = exerciseBpm;
    }

    public ExerciseDto(String exerciseName, int exerciseBpm, Long id) {
        this.exerciseName = exerciseName;
        this.exerciseBpm = exerciseBpm;
        this.id = id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
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

    public static ExerciseDto toDto(Exercise exercise) {
        return new ExerciseDto(exercise.getName(), exercise.getData(), exercise.getId(), exercise.getBpm());
    }

    public static ExerciseDto toRawDto(Exercise exercise) {
        return new ExerciseDto(exercise.getName(), exercise.getBpm(), exercise.getId());
    }
}
