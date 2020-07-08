package de.mackeprm.myturk.api;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.HIT;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.services.ExperimentService;
import de.mackeprm.myturk.services.HITService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.mackeprm.myturk.mturk.Endpoint.production;
import static de.mackeprm.myturk.mturk.Endpoint.sandbox;

@RestController
@RequestMapping("/api/v1/experiments")
public class ExperimentsController {
    private final ExperimentService experimentService;
    private final HITService hitService;

    @Autowired
    public ExperimentsController(ExperimentService experimentService, HITService hitService) {
        this.experimentService = experimentService;
        this.hitService = hitService;
    }

    @GetMapping("/")
    public ResponseEntity<Map<Endpoint, List<Experiment>>> getAllExperiments() {
        final Map<Endpoint, List<Experiment>> result = new HashMap<>();
        result.put(sandbox, experimentService.findAllByEndpoint(sandbox));
        result.put(production, experimentService.findAllByEndpoint(production));
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/{id}/")
    public ResponseEntity<Experiment> getById(@PathVariable String id) {
        Optional<Experiment> byId = experimentService.findById(id);
        return byId.map(experiment -> new ResponseEntity<>(experiment, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    //TODO put?
    @PostMapping("/{id}/hits/")
    public ResponseEntity<HIT> createHitForExperiment(@PathVariable String id) throws Exception {
        final Optional<Experiment> byId = experimentService.findById(id);
        if (byId.isPresent()) {
            return new ResponseEntity<>(hitService.createHitForExperiment(byId.get()), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PostMapping("/")
    public ResponseEntity<String> saveExperiment() {
        throw new IllegalStateException("not implemented");
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<String> deleteExperimentById(@PathVariable String id) {
        throw new IllegalStateException("not implemented");
    }

}
