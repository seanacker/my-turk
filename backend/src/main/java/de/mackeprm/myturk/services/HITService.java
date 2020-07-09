package de.mackeprm.myturk.services;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.LocalHIT;
import de.mackeprm.myturk.model.LocalHITRepository;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HITService {
    private final LocalHITRepository localHITRepository;
    private final MturkApiService mturkApiService;
    private final ExperimentService experimentService;

    @Autowired
    public HITService(LocalHITRepository localHITRepository, MturkApiService mturkApiService, ExperimentService experimentService) {
        this.localHITRepository = localHITRepository;
        this.mturkApiService = mturkApiService;
        this.experimentService = experimentService;
    }

    public List<LocalHIT> listAllHitIds(@NonNull Endpoint endpoint) {
        //TODO update sync?
        return localHITRepository.findAllByEndpoint(endpoint);
        //return mturkApiService.listAllHits(endpoint).stream().map(HIT::hitId).collect(Collectors.toList());
    }

    public Optional<LocalHIT> getHitById(String hitId) {
        //TODO sync here?
        return localHITRepository.findById(hitId);
    }

    public LocalHIT createHitForExperiment(Experiment experiment) throws TransformerException, ParserConfigurationException {
        final HIT hitForExperiment = mturkApiService.createHitForExperiment(experiment);
        final LocalHIT toSave = new LocalHIT(experiment.getEndpoint(), hitForExperiment.hitId(), experiment.getId());
        toSave.updateFieldsFromRemote(hitForExperiment);
        LocalHIT result = localHITRepository.save(toSave);
        experimentService.addHitToExperiment(result, experiment);
        return result;
    }

    public LocalHIT updateLocalHit(LocalHIT toSave) {
        toSave.setLastUpdate(LocalDateTime.now());
        return localHITRepository.save(toSave);
    }

    public void markAsWithoutExperiment(String hitId) {
        final LocalHIT localHIT = localHITRepository.findById(hitId).get();
        localHIT.setExperimentId("unknown");
        localHIT.setLastUpdate(LocalDateTime.now());
        updateLocalHit(localHIT);
    }

    //TODO deleteHIT
}
