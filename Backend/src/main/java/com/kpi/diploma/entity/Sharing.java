package com.kpi.diploma.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sharings")
public class Sharing {
    @Id
    @SequenceGenerator(name = "sharings_seq", sequenceName = "sharings_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sharings_seq")
    private Long id;

    @ManyToOne
    private User owner;

    @ManyToOne
    private User target;

    @ManyToOne
    private Exercise exercise;

    public Sharing() {

    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    public User getTarget() {
        return target;
    }

    public void setTarget(User target) {
        this.target = target;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
