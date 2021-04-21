package gov.tn.dhs.quarkus.box.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentDeletionResult {

    @JsonProperty("message")
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

