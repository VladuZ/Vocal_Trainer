package com.kpi.diploma.service;

import com.kpi.diploma.entity.Exercise;
import com.kpi.diploma.entity.Sharing;
import com.kpi.diploma.entity.User;
import com.kpi.diploma.exception.ObjAlreadyExistsException;
import com.kpi.diploma.exception.ObjNotFoundException;
import com.kpi.diploma.model.SharingDto;
import com.kpi.diploma.repository.ExerciseRepository;
import com.kpi.diploma.repository.SharingRepository;
import com.kpi.diploma.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SharingService {

    private final SharingRepository sharingRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public SharingService(SharingRepository sharingRepository, ExerciseRepository exerciseRepository, UserRepository userRepository) {
        this.sharingRepository = sharingRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    public void addSharing(Long id, String targetName) {
        User owner = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User target = userRepository.findByUsername(targetName).orElseThrow(() -> new UsernameNotFoundException("Target username not found"));
        if (owner.getUsername().equals(target.getUsername())) {
            throw new RuntimeException("You shall not pass!");
        }
        Exercise exercise = exerciseRepository.findById(id).orElseThrow(() -> new ObjNotFoundException("Exercise not found"));
        if (exerciseRepository.findByOwnerIdAndName(owner.getId(), exercise.getName()).isEmpty()) {
            throw new RuntimeException("Don't steal!");
        }
        if (sharingRepository.findByExerciseIdAndOwnerAndTarget(id, owner, target).isPresent()) {
            throw new ObjAlreadyExistsException("Sharing already exists");
        }
        Sharing sharing = new Sharing();
        sharing.setOwner(owner);
        sharing.setTarget(target);
        sharing.setExercise(exercise);
        sharingRepository.save(sharing);
    }

    @Transactional
    public List<SharingDto> getSharingsList() {
        User target = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return sharingRepository.findByTarget(target).stream().map(SharingDto::toDto).toList();
    }

    @Transactional
    public void acceptSharing(Long id) {
        User target = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Sharing sharing = sharingRepository.findById(id).orElseThrow(() -> new ObjNotFoundException("Sharing not found"));
        Exercise exercise = sharing.getExercise();
        if (exercise == null) {
            throw new ObjNotFoundException("Exercise not found");
        }
        Exercise acceptedExercise = new Exercise();
        acceptedExercise.setData(exercise.getData());
        acceptedExercise.setName(exercise.getName());
        acceptedExercise.setOwner(target);
        acceptedExercise.setBpm(exercise.getBpm());
        sharingRepository.deleteById(id);
        exerciseRepository.save(acceptedExercise);
    }

    public void declineSharing(Long id) {
        sharingRepository.findById(id).orElseThrow(() -> new ObjNotFoundException("Sharing not found"));
        sharingRepository.deleteById(id);
    }
}
