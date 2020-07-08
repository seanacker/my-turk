package de.mackeprm.myturk.model;

import de.mackeprm.myturk.mturk.Endpoint;

import javax.persistence.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
public class Experiment {

    //TODO experiment state: hasHits, already begun? finished?

    //Internal name
    @Id
    private String id;

    //Information for mturk workers
    //TODO Nonnull
    @Column(length = 1_000)
    private String title;
    @Column(length = 5_000)
    private String description;
    @ElementCollection
    private List<String> keywords;

    //HIT and Assignments
    private Duration hitExpiration;
    private Duration assignmentDuration;
    private int assignmentsPerHit;
    private double reward;

    //Qualifications
    //Nullable
    @ManyToOne
    private QualificationType completedExperimentQualification;
    //TODO levels of trust here
    //TODO set this explicitly?
    private boolean includeDefaultRequirements;

    //Entrypoint
    @Column(length = 5_000)
    private String entrypoint;

    //Endpoint
    //TODO can i make this endpoint independent?
    @Enumerated(EnumType.STRING)
    private Endpoint endpoint;

    @OneToMany
    private List<HIT> hits;

    //Hibernate
    public Experiment() {
    }

    public Experiment(String id, String title, String description, List<String> keywords, Duration hitExpiration, Duration assignmentDuration, int assignmentsPerHit, double reward, QualificationType completedExperimentQualification, boolean includeDefaultRequirements, String entrypoint, Endpoint endpoint) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.hitExpiration = hitExpiration;
        this.assignmentDuration = assignmentDuration;
        this.assignmentsPerHit = assignmentsPerHit;
        this.reward = reward;
        this.completedExperimentQualification = completedExperimentQualification;
        this.includeDefaultRequirements = includeDefaultRequirements;
        this.entrypoint = entrypoint;
        this.endpoint = endpoint;
        this.hits = new ArrayList<>();
    }


    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public Duration getHitExpiration() {
        return hitExpiration;
    }

    public Duration getAssignmentDuration() {
        return assignmentDuration;
    }

    public int getAssignmentsPerHit() {
        return assignmentsPerHit;
    }

    public double getReward() {
        return reward;
    }

    public QualificationType getCompletedExperimentQualification() {
        return completedExperimentQualification;
    }

    public boolean isIncludeDefaultRequirements() {
        return includeDefaultRequirements;
    }

    public String getEntrypoint() {
        return entrypoint;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public List<HIT> getHits() {
        return hits;
    }

    public String getAvailable() {
        return "0/0";
    }

    public String getPending() {
        return "0/0";
    }

    public String getCompleted() {
        return "0/0";
    }

    public String getWaitingForApproval() {
        return "0/0";
    }

}
