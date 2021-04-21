package gov.tn.dhs.quarkus.box.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentLinkResult {

    @JsonProperty("linkUrl")
    private String linkUrl;

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

}

