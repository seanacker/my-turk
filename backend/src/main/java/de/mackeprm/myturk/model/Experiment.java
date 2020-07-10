package de.mackeprm.myturk.model;

import de.mackeprm.myturk.mturk.Endpoint;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Experiment {

    public static final String UNKNOWN_EXPERIMENT = "unknown";

    //TODO experiment state: hasHits, already begun? finished?

    //Internal name
    @Id
    @NotBlank
    @Size(min = 2, max = 1_000)
    private String id;

    //Information for mturk workers
    @Size(min = 2, max = 1_000)
    @Column(length = 1_000)
    private String title;
    @Size(min = 2, max = 5_000)
    @Column(length = 5_000)
    private String description;
    @ElementCollection
    private List<String> keywords;

    //HIT and Assignments
    @NotNull
    private Duration hitExpiration;
    @NotNull
    private Duration assignmentDuration;
    @Min(value = 1)
    private int assignmentsPerHit;
    @Min(value = 0)
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
    @Size(min = 2, max = 5_000)
    private String entrypoint;

    //Endpoint
    //TODO can i make this endpoint independent?
    @Enumerated(EnumType.STRING)
    private Endpoint endpoint;

    @OneToMany
    private List<LocalHIT> localHits;

    //Hibernate
    public Experiment() {
        this.localHits = new ArrayList<>();
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
        this.localHits = new ArrayList<>();
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

    public List<LocalHIT> getLocalHits() {
        return localHits;
    }

    public int getNumAssignments() {
        return this.localHits.stream().mapToInt(LocalHIT::getMaxAssignments).sum();
    }

    public String getAvailable() {
        final int available = this.localHits.stream().mapToInt(LocalHIT::getAvailable).sum();
        return available + "/" + getNumAssignments();
    }

    public String getPending() {
        final int pending = this.localHits.stream().mapToInt(LocalHIT::getPending).sum();
        return pending + "/" + getNumAssignments();
    }

    public String getCompleted() {
        final int completed = this.localHits.stream().mapToInt(LocalHIT::getCompleted).sum();
        return completed + "/" + getNumAssignments();
    }

    public String getWaitingForApproval() {
        final int waitingForApproval = this.localHits.stream().mapToInt(LocalHIT::getWaitingForApproval).sum();
        return waitingForApproval + "/" + getNumAssignments();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setHitExpiration(Duration hitExpiration) {
        this.hitExpiration = hitExpiration;
    }

    public void setAssignmentDuration(Duration assignmentDuration) {
        this.assignmentDuration = assignmentDuration;
    }

    public void setAssignmentsPerHit(int assignmentsPerHit) {
        this.assignmentsPerHit = assignmentsPerHit;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public void setCompletedExperimentQualification(QualificationType completedExperimentQualification) {
        this.completedExperimentQualification = completedExperimentQualification;
    }

    public void setIncludeDefaultRequirements(boolean includeDefaultRequirements) {
        this.includeDefaultRequirements = includeDefaultRequirements;
    }

    public void setEntrypoint(String entrypoint) {
        this.entrypoint = entrypoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void setLocalHits(List<LocalHIT> localHits) {
        this.localHits = localHits;
    }


}
