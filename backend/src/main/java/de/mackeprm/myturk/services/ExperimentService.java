package de.mackeprm.myturk.services;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.ExperimentRepository;
import de.mackeprm.myturk.model.HIT;
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

    public Experiment addHitToExperiment(HIT result, Experiment experiment) {
        experiment.getHits().add(result);
        experimentRepository.save(experiment);
        return experiment;
    }

    //TODO add experiment to database
    //TODO update experiment

    //TODO delete experiment by id

}
