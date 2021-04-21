package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.quarkus.box.exception.ServiceError;
import gov.tn.dhs.quarkus.box.model.DocumentDeletionResult;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/file")
public class DeleteDocumentResource {

    @Inject
    ConnectionBean connectionBean;

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public DocumentDeletionResult deleteDocument(
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
            file.delete();
        } catch (BoxAPIException e) {
            switch (e.getResponseCode()) {
                case 404: {
                    throw new ServiceError(404, "Document not found");
                }
                default: {
                    throw new ServiceError(500, "Document deletion error");
                }
            }
        }

        DocumentDeletionResult documentDeletionResult = new DocumentDeletionResult();
        documentDeletionResult.setMessage("document successfully deleted");
        return documentDeletionResult;
    }

}
