package de.mackeprm.myturk.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/qualificationTypes")
public class QualificationTypeController {

    @PostMapping("/")
    public ResponseEntity<String> saveQualificationType() {
        throw new IllegalStateException("not implemented");
    }

    //TODO levels of trust here: use level of trust as an INT
    //https://docs.aws.amazon.com/AWSMechTurk/latest/AWSMturkAPI/ApiReference_AssociateQualificationWithWorkerOperation.html
}
