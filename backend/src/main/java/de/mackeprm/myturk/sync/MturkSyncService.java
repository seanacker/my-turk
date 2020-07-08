package de.mackeprm.myturk.sync;

import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import de.mackeprm.myturk.services.ExperimentService;
import de.mackeprm.myturk.services.HITService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MturkSyncService {
    private final MturkApiService mturkApiService;
    private final HITService hitService;
    private final ExperimentService experimentService;

    @Autowired
    public MturkSyncService(MturkApiService mturkApiService, HITService hitService, ExperimentService experimentService) {
        this.mturkApiService = mturkApiService;
        this.hitService = hitService;
        this.experimentService = experimentService;
    }

    public void syncHit(String hitId) {

    }

    public void syncEndpoint(Endpoint endpoint) {

    }

    public void initialSync() {
        //TODO sandbox
        //TODO production
    }



    //TODO initial sync after login
    //TODO manual sync
}
