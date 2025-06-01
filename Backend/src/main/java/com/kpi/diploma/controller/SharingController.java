package com.kpi.diploma.controller;

import com.kpi.diploma.model.SharingDto;
import com.kpi.diploma.service.SharingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("sharing")
public class SharingController {
    public final SharingService sharingService;

    public SharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createSharing (@RequestParam("id") Long id, @RequestParam("targetName") String targetName) {
        sharingService.addSharing(id, targetName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/find")
    public ResponseEntity<?> findSharings () {
        List<SharingDto> sharingsList = sharingService.getSharingsList();
        return ResponseEntity.ok(sharingsList);
    }

    @PostMapping("/accept/{id}")
    public ResponseEntity<?> acceptSharing (@PathVariable Long id) {
        sharingService.acceptSharing(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/decline/{id}")
    public ResponseEntity<?> declineSharing (@PathVariable Long id) {
        sharingService.declineSharing(id);
        return ResponseEntity.ok().build();
    }
}
