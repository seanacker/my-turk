package de.mackeprm.myturk.mturk;

import java.net.URI;

public enum Endpoint {
    sandbox("https://mturk-requester-sandbox.us-east-1.amazonaws.com"),
    production("https://mturk-requester.us-east-1.amazonaws.com");

    private final URI uri;

    private Endpoint(String uri) {
        this.uri = URI.create(uri);
    }

    public URI getUri() {
        return uri;
    }
}
