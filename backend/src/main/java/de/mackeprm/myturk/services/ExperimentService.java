package de.mackeprm.myturk.services;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.ExperimentRepository;
import de.mackeprm.myturk.model.LocalHIT;
import de.mackeprm.myturk.mturk.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExperimentService {
    private final ExperimentRepository experimentRepository;

    @Autowired
    public ExperimentService(ExperimentRepository experimentRepository) {
        this.experimentRepository = experimentRepository;
    }

    public List<Experiment> findAllByEndpoint(Endpoint endpoint) {
        return experimentRepository.findAllByEndpoint(endpoint);
    }

    public Optional<Experiment> findById(String id) {
        return experimentRepository.findById(id);
    }

    public Experiment addHitToExperiment(LocalHIT localHIT, Experiment experiment) {
        Experiment experiment1 = experimentRepository.findById(experiment.getId()).get();
        if (!experiment1.getLocalHits().contains(localHIT)) {
            experiment1.getLocalHits().add(localHIT);
            experimentRepository.save(experiment1);
        }
        return experiment1;
    }

    public List<Experiment> findByHitsContaining(LocalHIT hit) {
        return experimentRepository.findByLocalHitsContaining(hit);
    }

    public Experiment save(Experiment toSave) {
        return experimentRepository.save(toSave);
    }

    public boolean exists(String id) {
        return experimentRepository.existsById(id);
    }

    public void deleteById(String s) {
        //TODO update hits -> detached
        experimentRepository.deleteById(s);
    }
}
