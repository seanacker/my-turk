package de.mackeprm.myturk.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AssignmentController {

    @GetMapping("/assignments/")
    public ResponseEntity<String> getAll() {
        throw new IllegalStateException("not implemented");
    }

    @PostMapping("/assignments/{assignmentId}/approve")
    public ResponseEntity<String> approveAssignment(@PathVariable String assignmentId) {
        throw new IllegalStateException("not implemented");
    }

    @PostMapping("/assignments/{assignmentId}/reject")
    public ResponseEntity<String> rejectAssignment(@PathVariable String assignmentId) {
        throw new IllegalStateException("not implemented");
    }

}
