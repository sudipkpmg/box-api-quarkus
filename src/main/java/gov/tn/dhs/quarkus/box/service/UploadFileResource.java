package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import gov.tn.dhs.quarkus.box.exception.ServiceError;
import gov.tn.dhs.quarkus.box.model.UploadFileResponse;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Path("/file")
public class UploadFileResource {

    private static final Logger logger = LoggerFactory.getLogger(UploadFileResource.class);

    @Inject
    ConnectionBean connectionBean;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public UploadFileResponse uploadFile(
            @QueryParam("boxFolderId") String  boxFolderId,
            @QueryParam("appUserId") String appUserId,
            @QueryParam("user_key") String user_key,
            MultipartFormDataInput input
    ) {
        RecordTransaction.recordTransaction(
                "ECM_API",
                "REST",
                "POST",
                user_key == null ? "sudip" : user_key
        );

        logger.info("boxFolderId = {}", boxFolderId);
        logger.info("appUserId = {}", appUserId);
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();
        if (uploadForm == null) {
            throw new ServiceError(500, "Got null body");
        }
        List<InputPart> inputParts = uploadForm.get("file");
        if (inputParts == null) {
            throw new ServiceError(500, "File content came as null");
        }
        InputPart filePart = inputParts.get(0);
        try {
            MultivaluedMap<String, String> header = filePart.getHeaders();
            String fileName = getFileName(header);
            InputStream inputStream = filePart.getBody(InputStream.class, null);
             UploadFileResponse uploadFileResponse = uploadToBox(inputStream, fileName, boxFolderId, appUserId);
             logger.info("uploadFileResponse = {}", uploadFileResponse);
            return uploadFileResponse;
        } catch (ServiceError se) {
            throw new ServiceError(se.getCode(), se.getMessage());
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new ServiceError(500, "Service error");
        }
    }

    private UploadFileResponse uploadToBox(InputStream inputStream, String fileName, String boxFolderId, String appUserId) {
        BoxDeveloperEditionAPIConnection api = connectionBean.getBoxDeveloperEditionAPIConnection();
        api.asUser(appUserId);
        BoxFolder parentFolder;
        try {
            parentFolder = new BoxFolder(api, boxFolderId);
            BoxFolder.Info info = parentFolder.getInfo();
            logger.info("Parent Folder with ID {} and name {} found", boxFolderId, info.getName());
        } catch (BoxAPIException e) {
            logger.error(e.getMessage());
            throw new ServiceError(400, "Folder not found");
        }
        BoxFile.Info newFileInfo;
        try {
            newFileInfo = parentFolder.uploadFile(inputStream, fileName);
        } catch (BoxAPIException e) {
            logger.error(e.getMessage());
            throw new ServiceError(409, "File with the same name already exists");
        }
        String fileId = newFileInfo.getID();
        UploadFileResponse uploadFileResponse = new UploadFileResponse();
        uploadFileResponse.setStatus("File upload completed");
        uploadFileResponse.setFileId(fileId);
        logger.info(JsonUtil.toJson(uploadFileResponse));
        return uploadFileResponse;
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");
        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }
        return "unknown";
    }
}
