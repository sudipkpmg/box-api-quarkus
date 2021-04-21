package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.quarkus.box.exception.ServiceError;
import gov.tn.dhs.quarkus.box.model.DocumentViewResult;
import gov.tn.dhs.quarkus.box.model.ShortUrlRequest;
import gov.tn.dhs.quarkus.box.model.ShortUrlRequestDomain;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.net.URL;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;

@Path("/file-view")
public class ViewDocumentResource {

    private static Logger logger = LoggerFactory.getLogger(ViewDocumentResource.class);

    @Inject
    ConnectionBean connectionBean;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentViewResult viewDocument(
            @QueryParam("appUserId") String appUserId,
            @QueryParam("fileId") String fileId,
            @QueryParam("user_key") String user_key
    ) {
        RecordTransaction.recordTransaction(
                "ECM_API",
                "REST",
                "POST",
                user_key == null ? "sudip" : user_key
        );

        BoxDeveloperEditionAPIConnection api = connectionBean.getBoxDeveloperEditionAPIConnection();
        api.asUser(appUserId);

        try {
            BoxFile file = new BoxFile(api, fileId);
            URL previewUrl = file.getPreviewLink();
            String previewUrlString = previewUrl.toString();
            String shortUrl = getShortPreviewUrl(previewUrlString);
            DocumentViewResult documentViewResult = new DocumentViewResult();
            documentViewResult.setPreviewUrl(previewUrlString);
            documentViewResult.setShortPreviewUrl(shortUrl);
            logger.info(JsonUtil.toJson(documentViewResult));
            return documentViewResult;
        } catch (BoxAPIException e) {
            switch (e.getResponseCode()) {
                case 404: {
                    throw new ServiceError(409, "Document not found");
                }
                default: {
                    throw new ServiceError(500, "Document view error");
                }
            }
        }
    }

    private String getShortPreviewUrl(String url) {
        ShortUrlRequestDomain domain = new ShortUrlRequestDomain();
        domain.setFullname("rebrand.ly");
        ShortUrlRequest shortUrlRequest = new ShortUrlRequest();
        shortUrlRequest.setDestination(url);
        shortUrlRequest.setDomain(domain);
        HttpResponse<JsonNode> response = Unirest.post("https://api.rebrandly.com/v1/links")
                .header("Content-Type", "application/json")
                .header("apikey", "b6dcfd08101e4972965087d77d1b1f9f")
                .body(shortUrlRequest)
                .asJson();
        String shortUrl = "http://" + response.getBody().getObject().getString("shortUrl");
        logger.info("shortUrl = {}", shortUrl);
        return shortUrl;
    }

}
