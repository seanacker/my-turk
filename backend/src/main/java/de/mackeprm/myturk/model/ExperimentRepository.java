package de.mackeprm.myturk.model;

import de.mackeprm.myturk.mturk.Endpoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExperimentRepository extends CrudRepository<Experiment, String> {
    List<Experiment> findAllByEndpoint(Endpoint endpoint);
}
