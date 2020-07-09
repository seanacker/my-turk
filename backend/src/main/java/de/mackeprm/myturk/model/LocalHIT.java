package de.mackeprm.myturk.model;

import de.mackeprm.myturk.mturk.Endpoint;
import software.amazon.awssdk.services.mturk.model.HIT;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
public class LocalHIT {

    @Id
    private String hitId;

    private String experimentId;

    private LocalDateTime lastUpdate;

    @Enumerated(EnumType.STRING)
    private SyncState syncState;

    @Enumerated(EnumType.STRING)
    private Endpoint endpoint;

    private String title;
    private Integer maxAssignments;
    private String description;
    private String status;
    private String reviewStatus;
    private Integer available;
    private Integer pending;
    private Integer completed;
    private LocalDateTime creationTime;

    //hibernate
    public LocalHIT() {
    }

    public LocalHIT(Endpoint endpoint, String hitId, String experimentId) {
        this.endpoint = endpoint;
        this.hitId = hitId;
        this.experimentId = experimentId;
        this.lastUpdate = LocalDateTime.now();
    }

    public String getHitId() {
        return hitId;
    }

    public String getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(String experimentId) {
        this.experimentId = experimentId;
    }

    public SyncState getSyncState() {
        return syncState;
    }

    public void setSyncState(SyncState syncState) {
        this.syncState = syncState;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setMaxAssignments(Integer maxAssignments) {
        this.maxAssignments = maxAssignments;
    }

    public Integer getMaxAssignments() {
        return maxAssignments;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setAvailable(Integer available) {
        this.available = available;
    }

    public Integer getAvailable() {
        return available;
    }

    public void setPending(Integer pending) {
        this.pending = pending;
    }

    public Integer getPending() {
        return pending;
    }

    public void setCompleted(Integer completed) {
        this.completed = completed;
    }

    public Integer getCompleted() {
        return completed;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public Integer getWaitingForApproval() {
        return this.maxAssignments
                - this.available
                - this.pending
                - this.completed;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void updateFieldsFromRemote(HIT remoteHit) {
        this.setTitle(remoteHit.title());
        this.setDescription(remoteHit.description());
        this.setStatus(remoteHit.hitStatusAsString());
        this.setReviewStatus(remoteHit.hitReviewStatusAsString());
        this.setMaxAssignments(remoteHit.maxAssignments());
        this.setAvailable(remoteHit.numberOfAssignmentsAvailable());
        this.setPending(remoteHit.numberOfAssignmentsPending());
        this.setCompleted(remoteHit.numberOfAssignmentsCompleted());
        this.setCreationTime(LocalDateTime.ofInstant(remoteHit.creationTime(), ZoneId.of("US/Eastern")));
        //TODO do i have to set any other fields?
    }
}
