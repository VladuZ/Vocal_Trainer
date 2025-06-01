package com.example.testing;

public class SharingDto {

    private Long id;

    private String exerciseName;

    private String ownerName;

    public SharingDto(String exerciseName, String ownerName, Long id) {
        this.exerciseName = exerciseName;
        this.ownerName = ownerName;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}
