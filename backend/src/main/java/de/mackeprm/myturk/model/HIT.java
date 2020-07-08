package de.mackeprm.myturk.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class HIT {

    //TODO sync-state, last-sync?
    @Id
    private String hitId;

    private String experimentId;

    //hibernate
    public HIT() {
    }

    public HIT(String hitId, String experimentId) {
        this.hitId = hitId;
        this.experimentId = experimentId;
    }

    public String getHitId() {
        return hitId;
    }

    public String getExperimentId() {
        return experimentId;
    }
}
