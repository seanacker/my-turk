package de.mackeprm.myturk.api;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.LocalHIT;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.services.ExperimentService;
import de.mackeprm.myturk.services.HITService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
    public ResponseEntity<LocalHIT> createHitForExperiment(@PathVariable String id) throws Exception {
        final Optional<Experiment> byId = experimentService.findById(id);
        if (byId.isPresent()) {
            return new ResponseEntity<>(hitService.createHitForExperiment(byId.get()), HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    @PutMapping("/")
    public ResponseEntity<Experiment> createExperiment(@Valid @RequestBody Experiment input) {
        final boolean exists = experimentService.exists(input.getId());
        if (exists) {
            throw new IllegalStateException("Element with id " + input.getId() + " already exists. Please use another id, or, if you want to update, use POST");
        } else {
            final Experiment toSave = new Experiment();
            update(input, toSave);
            return new ResponseEntity<>(experimentService.save(toSave), HttpStatus.CREATED);
        }
    }

    @PostMapping("/")
    public ResponseEntity<Experiment> saveExperiment(@Valid @RequestBody Experiment input) {
        final Optional<Experiment> byId = experimentService.findById(input.getId());
        if (byId.isEmpty()) {
            throw new IllegalStateException("Element with id " + input.getId() + " already exists. Please use another id, or, if you want to update, use POST");
        } else {
            //Does the experiment exist? -> update
            final Experiment existing = byId.get();
            update(input, existing);
            return new ResponseEntity<>(experimentService.save(existing), HttpStatus.OK);
        }
    }

    private void update(Experiment input, Experiment toSave) {
        toSave.setId(input.getId());
        toSave.setTitle(input.getTitle());
        toSave.setDescription(input.getDescription());
        toSave.setKeywords(input.getKeywords());
        //TODO award qualification
        toSave.setReward(input.getReward());
        toSave.setHitExpiration(input.getHitExpiration());
        toSave.setAssignmentDuration(input.getAssignmentDuration());
        toSave.setAssignmentsPerHit(input.getAssignmentsPerHit());
        toSave.setIncludeDefaultRequirements(input.isIncludeDefaultRequirements());
        toSave.setEntrypoint(input.getEntrypoint());
        toSave.setEndpoint(input.getEndpoint());
    }

    @DeleteMapping("/{id}/")
    public ResponseEntity<String> deleteExperimentById(@PathVariable String id) {
        final boolean exists = experimentService.exists(id);
        if (exists) {
            experimentService.deleteById(id);
            return new ResponseEntity<>("{\"deleted\":\"" + id + "\"}", HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //TODO add HIT to experiment manually
}
