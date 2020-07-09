package de.mackeprm.myturk.api.auth;

import de.mackeprm.myturk.mturk.Endpoint;
import de.mackeprm.myturk.mturk.MturkApiService;
import de.mackeprm.myturk.sync.MturkSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.mturk.model.MTurkException;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class LoginController {
    private final MturkApiService mturkApiService;
    private final MturkSyncService mturkSyncService;

    @Autowired
    public LoginController(MturkApiService mturkApiService, MturkSyncService mturkSyncService) {
        this.mturkApiService = mturkApiService;
        this.mturkSyncService = mturkSyncService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            mturkApiService.login(Endpoint.sandbox, loginRequest.getAwsAccessKeyId(), loginRequest.getAwsSecretAccessKey());
            final String balance = mturkApiService.login(Endpoint.production, loginRequest.getAwsAccessKeyId(), loginRequest.getAwsSecretAccessKey());
            //TODO success:true
            mturkSyncService.initialSync();
            //token: accessKeyId
            return new ResponseEntity<>(new LoginResponse("You can pass", balance, loginRequest.getAwsAccessKeyId()), HttpStatus.OK);
        } catch (final MTurkException e) {
            if (e.getMessage().contains("The security token included in the request is invalid")) {
                return new ResponseEntity<>(new LoginResponse("Invalid Login"), HttpStatus.BAD_REQUEST);
            } else {
                throw new RuntimeException(e);
            }
        }
    }
}
