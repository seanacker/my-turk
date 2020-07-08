package de.mackeprm.myturk.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

//TODO include default qualification types?

/**
 * QualificationTypes mirror
 * I use the QualificationTypeId given by mturk,
 * therefore, qualificationTypes exist only in mturk.
 * Detached Qualification Types will be deleted by sync.
 */
@Entity
public class QualificationType {

    @Id
    private String qualificationTypeId;

    @Column(length = 5_000)
    private String name;
    @Column(length = 5_000)
    private String description;
    //creationTime?

    //hibernate
    public QualificationType() {
    }

    public QualificationType(String qualificationTypeId, String name, String description) {
        this.qualificationTypeId = qualificationTypeId;
        this.name = name;
        this.description = description;
    }

    public String getQualificationTypeId() {
        return qualificationTypeId;
    }

    public void setQualificationTypeId(String qualificationTypeId) {
        this.qualificationTypeId = qualificationTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
