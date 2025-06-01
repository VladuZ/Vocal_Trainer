package com.kpi.diploma.controller;

import com.kpi.diploma.model.ExerciseDto;
import com.kpi.diploma.service.ExerciseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("exercise")
public class ExerciseController {
    public final ExerciseService exerciseService;
    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createExercise (@RequestParam("exerciseName") String exerciseName,
                                             @RequestParam("file") MultipartFile file,
                                             @RequestParam("exerciseBpm") int exerciseBpm){
        try {
            exerciseService.createExercise(file.getBytes(), exerciseName, exerciseBpm);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            logger.error("Internal error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/find")
    public ResponseEntity<?> findExercises () {
        List<ExerciseDto> exList = exerciseService.getExercises();
        return ResponseEntity.ok(exList);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<?> getExercise (@PathVariable Long id) {
        ExerciseDto exercise = exerciseService.getExercise(id);
        return ResponseEntity.ok(exercise);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteExercise (@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.ok().build();
    }
}
