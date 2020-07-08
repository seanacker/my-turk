package de.mackeprm.myturk.services;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HITService {
    private final MturkApiService mturkApiService;
    private final ExperimentService experimentService;

    @Autowired
    public HITService(MturkApiService mturkApiService, ExperimentService experimentService) {
        this.mturkApiService = mturkApiService;
        this.experimentService = experimentService;
    }

    public List<String> listAllHitIds(@NonNull Endpoint endpoint) {
        return mturkApiService.listAllHits(endpoint).stream().map(HIT::hitId).collect(Collectors.toList());
    }

    public HIT getHitById() {
        //TODO sync here?
        throw new IllegalStateException("not implemented");
    }

    public de.mackeprm.myturk.model.HIT createHitForExperiment(Experiment experiment) throws TransformerException, ParserConfigurationException {
        HIT hitForExperiment = mturkApiService.createHitForExperiment(experiment);
        de.mackeprm.myturk.model.HIT result = new de.mackeprm.myturk.model.HIT(hitForExperiment.hitId(), experiment.getId());
        experimentService.addHitToExperiment(result,experiment);
        return result;
    }

    //TODO deleteHIT
}
