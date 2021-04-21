package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;

@Path("/file")
public class DownloadFileResource {

    @Inject
    ConnectionBean connectionBean;

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(
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
        BoxFile file = null;
        BoxFile.Info info = null;
        try {
            file = new BoxFile(api, fileId);
            info = file.getInfo();
        } catch (BoxAPIException e) {
            Response.ResponseBuilder responseBuilder = Response.status(400, "File not found");
            return responseBuilder.build();
        }
        try {
            String fileName = info.getName();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            file.download(outputStream);
            final byte[] bytes = outputStream.toByteArray();
            String fileNameSuggestion = String.format("attachment; filename=\"%s\"", fileName);
            Response.ResponseBuilder response = Response.ok(bytes);
            response.header("Content-Disposition", fileNameSuggestion);
            return response.build();
        } catch (Exception ex) {
            Response.ResponseBuilder responseBuilder = Response.status(500, "Download Error");
            return responseBuilder.build();
        }
    }

}
