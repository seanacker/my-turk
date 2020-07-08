package de.mackeprm.myturk.api.auth;

import javax.validation.constraints.NotBlank;

public class LoginRequestDTO {
    @NotBlank(message = "Please set an accessKeyId")
    private String awsAccessKeyId;
    @NotBlank(message = "Please set a secretAccessKey")
    private String awsSecretAccessKey;

    public LoginRequestDTO(String awsAccessKeyId, String awsSecretAccessKey) {
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return awsSecretAccessKey;
    }

    public void setAwsSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }
}
