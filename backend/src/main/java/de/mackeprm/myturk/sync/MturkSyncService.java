package de.mackeprm.myturk.sync;

import de.mackeprm.myturk.model.Experiment;
import de.mackeprm.myturk.model.LocalHIT;
import de.mackeprm.myturk.model.SyncState;
import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import de.mackeprm.myturk.services.ExperimentService;
import de.mackeprm.myturk.services.HITService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.mturk.model.HIT;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Service
public class MturkSyncService {
    private static final Logger log = LoggerFactory.getLogger(MturkSyncService.class);

    private final MturkApiService mturkApiService;
    private final HITService hitService;
    private final ExperimentService experimentService;

    @Autowired
    public MturkSyncService(MturkApiService mturkApiService, HITService hitService, ExperimentService experimentService) {
        this.mturkApiService = mturkApiService;
        this.hitService = hitService;
        this.experimentService = experimentService;
    }

    public Optional<Experiment> resolveExperimentForHit(HIT hit) {
        Optional<Experiment> result = Optional.empty();
        String requesterAnnotation = hit.requesterAnnotation();
        if (isNotBlank(requesterAnnotation) && requesterAnnotation.contains("createdForExperiment")) {
            final JSONObject annotationObject = new JSONObject(requesterAnnotation);
            final String createdForExperiment = annotationObject.getString("createdForExperiment");
            result = experimentService.findById(createdForExperiment);
        }
        if (result.isEmpty()) {
            Optional<LocalHIT> hitById = hitService.getHitById(hit.hitId());
            if (hitById.isPresent()) {
                List<Experiment> byHitsContaining = experimentService.findByHitsContaining(hitById.get());
                if (byHitsContaining.size() == 1) {
                    result = Optional.of(byHitsContaining.get(0));
                } else {
                    log.warn("Found " + byHitsContaining.size() + " Experiments for hit: " + hit.hitId());
                }
            }
        }
        return result;
    }

    public void syncEndpoint(Endpoint endpoint) {
        final List<HIT> hits = mturkApiService.listAllHits(endpoint);
        for (HIT hit : hits) {
            Optional<LocalHIT> localHIT = hitService.getHitById(hit.hitId());
            updateHit(endpoint, localHIT, Optional.of(hit));
            final Optional<Experiment> experiment = resolveExperimentForHit(hit);
            if (experiment.isPresent()) {
                updateExperiment(experiment.get(), hit);
            } else {
                hitService.markAsWithoutExperiment(hit.hitId());
            }
        }
    }

    private void updateExperiment(Experiment experiment, HIT remoteHit) {
        LocalHIT localHIT = hitService.getHitById(remoteHit.hitId()).get();
        experimentService.addHitToExperiment(localHIT, experiment);
    }

    public LocalHIT syncHit(LocalHIT localHIT) {
        Optional<HIT> remote = mturkApiService.findHitById(localHIT.getEndpoint(), localHIT.getHitId());
        if (remote.isPresent()) {
            localHIT.updateFieldsFromRemote(remote.get());
            localHIT.setSyncState(SyncState.toDate);
        } else {
            localHIT.setSyncState(SyncState.localOnly);
        }
        return hitService.updateLocalHit(localHIT);
    }

    public void updateHit(Endpoint endpoint, Optional<LocalHIT> localHITOptional, Optional<HIT> remoteHITOptional) {
        if (remoteHITOptional.isEmpty()) {
            if (localHITOptional.isEmpty()) {
                throw new IllegalStateException("Both remote and local are empty. Don't know what to do.");
            } else {
                final LocalHIT toSave = localHITOptional.get();
                toSave.setSyncState(SyncState.localOnly);
                hitService.updateLocalHit(toSave);
            }
        } else {
            if (localHITOptional.isEmpty()) {
                final HIT remoteHit = remoteHITOptional.get();
                final LocalHIT toSave = new LocalHIT(endpoint, remoteHit.hitId(), null);
                toSave.updateFieldsFromRemote(remoteHit);
                toSave.setSyncState(SyncState.remoteOnly);
                hitService.updateLocalHit(toSave);
            } else {
                final HIT remoteHit = remoteHITOptional.get();
                final LocalHIT localHIT = localHITOptional.get();
                localHIT.setSyncState(SyncState.toDate);
                localHIT.updateFieldsFromRemote(remoteHit);
                hitService.updateLocalHit(localHIT);
            }
        }
    }

    public void initialSync() {
        //TODO remove all old HIts, as they could be from another session.
        syncEndpoint(Endpoint.sandbox);
        syncEndpoint(Endpoint.production);
    }

}
