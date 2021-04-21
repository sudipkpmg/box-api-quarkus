package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxSharedLink;
import gov.tn.dhs.quarkus.box.exception.ServiceError;
import gov.tn.dhs.quarkus.box.model.DocumentLinkResult;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Path("/file-link")
public class GetDocumentLinkResource {

    private static Logger logger = LoggerFactory.getLogger(GetDocumentLinkResource.class);

    private static long LINK_DURATION = 15 * 60 * 1000L; // 15 minutes

    @Inject
    ConnectionBean connectionBean;

    @Inject
    AppProperties appProperties;

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentLinkResult getDocumentLink(
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
            Date currentDate = new Date();
            long currentDateTimeval = currentDate.getTime();
            long linkDuration = appProperties.getDocumentLinkDuration();
            long unshareDateTimeval = currentDateTimeval + linkDuration;
            Date unshareDate = new Date(unshareDateTimeval);
            BoxSharedLink.Permissions permissions = new BoxSharedLink.Permissions();
            permissions.setCanPreview(true);
            permissions.setCanDownload(true);
            BoxSharedLink sharedLink = file.createSharedLink(BoxSharedLink.Access.OPEN, unshareDate, permissions);
            String linkUrl = sharedLink.getURL();
            DocumentLinkResult documentLinkResult = new DocumentLinkResult();
            documentLinkResult.setLinkUrl(linkUrl);
            logger.info("documentLinkResult = {}", JsonUtil.toJson(documentLinkResult));
            return documentLinkResult;
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

}
