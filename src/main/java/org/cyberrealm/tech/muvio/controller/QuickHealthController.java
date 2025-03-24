package org.cyberrealm.tech.muvio.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QuickHealthController {
    @GetMapping("/quick-health")
    public ResponseEntity<String> quickHealth() {
        return ResponseEntity.ok("UP");
    }
}
