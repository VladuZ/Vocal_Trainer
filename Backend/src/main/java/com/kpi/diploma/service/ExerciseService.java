package com.kpi.diploma.service;

import com.kpi.diploma.entity.Exercise;
import com.kpi.diploma.entity.User;
import com.kpi.diploma.exception.ObjAlreadyExistsException;
import com.kpi.diploma.exception.ObjNotFoundException;
import com.kpi.diploma.model.ExerciseDto;
import com.kpi.diploma.repository.ExerciseRepository;
import com.kpi.diploma.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExerciseService {
    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public void createExercise(byte[] data, String name, int bpm) {
        if (exerciseRepository.findByName(name).isPresent()) {
            throw new ObjAlreadyExistsException("Exercise with this name already exists");
        }
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setOwner((User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal());
        exercise.setData(data);
        exercise.setBpm(bpm);
        exerciseRepository.save(exercise);
    }

    @Transactional
    public List<ExerciseDto> getExercises() {
        return exerciseRepository.findByOwnerId(
                ((User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                        .getId())
                .stream()
                .map(ExerciseDto::toRawDto)
                .toList();
    }

    @Transactional
    public void deleteExercise(Long id) {
        if (exerciseRepository.findById(id).isEmpty()) {
            throw new ObjNotFoundException("Exercise does not exist");
        }
        exerciseRepository.deleteById(id);
    }

    @Transactional
    public ExerciseDto getExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new ObjNotFoundException("Exercise does not exist"));
        return ExerciseDto.toDto(exercise);
    }
}
