package gov.tn.dhs.quarkus.box.service;

import com.box.sdk.*;
import gov.tn.dhs.quarkus.box.exception.ServiceError;
import gov.tn.dhs.quarkus.box.model.CreateFolderRequest;
import gov.tn.dhs.quarkus.box.model.FileInfo;
import gov.tn.dhs.quarkus.box.model.SearchResult;
import gov.tn.dhs.quarkus.box.util.RecordTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Path("/folder")
public class SearchResource {

    private static final String CITIZEN_METADATA_SCOPE = "enterprise";

    private static final Logger logger = LoggerFactory.getLogger(SearchResource.class);

    @Inject
    ConnectionBean connectionBean;

    @Inject
    AppProperties appProperties;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult search(
            @QueryParam("appUserId") String appUserId,
            @QueryParam("folderId") String folderId,
            @QueryParam("fileName") String fileName,
            @QueryParam("offset") long offset,
            @QueryParam("limit") long limit,
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

        String searchType = "folder";
        if (fileName != null) {
            searchType = "file";
        }

        switch (searchType) {
            case "folder": {
                try {
                    BoxFolder folder = new BoxFolder(api, folderId);
                    Metadata folderMetadata = folder.getMetadata(appProperties.getCitizenMetadataTemplate(), CITIZEN_METADATA_SCOPE);
                    logger.info(folderMetadata.toString());
                    List<FileInfo> files = new ArrayList<>();
                    PartialCollection<BoxItem.Info> items = folder.getChildrenRange(offset, limit);
                    for (BoxItem.Info itemInfo : items) {
                        FileInfo fileInfo = getItemInfo(itemInfo, folderMetadata);
                        files.add(fileInfo);
                    }
                    long allItemCount = items.fullSize();
                    boolean complete = (allItemCount > (offset+limit));
                    SearchResult searchResult = prepareSearchResult(files, complete);
                    return searchResult;
                } catch (BoxAPIException e) {
                    int responseCode = e.getResponseCode();
                    switch (responseCode) {
                        case 404: {
                            throw new ServiceError(404, "Folder not found");
                        }
                        default: {
                            throw new ServiceError(500, "Search error");
                        }
                    }
                }
            }
            case "file": {
                BoxFolder folder = new BoxFolder(api, folderId);
                Metadata folderMetadata = folder.getMetadata(appProperties.getCitizenMetadataTemplate(), CITIZEN_METADATA_SCOPE);
                limit++;
                long position = offset;
                long count = 0;
                List<FileInfo> files = new ArrayList<>();
                for (BoxItem.Info info : folder) {
                    if (info instanceof BoxFile.Info) {
                        String itemName = info.getName();
                        if (fileName.equals(itemName)) {
                            position++;
                            if (position >= offset) {
                                FileInfo fileInfo = getItemInfo(info, folderMetadata);
                                files.add(fileInfo);
                                count++;
                                if (count == limit) {
                                    break;
                                }
                            }
                        }
                    }
                }
                boolean complete = (count < limit);
                SearchResult searchResult = prepareSearchResult(files, complete);
                return searchResult;
            }
        }
        throw new ServiceError(500, "Search error");
    }

    private SearchResult prepareSearchResult(List<FileInfo> files, boolean complete) {
        SearchResult searchResult = new SearchResult();
        searchResult.setFileData(files);
        searchResult.setComplete(Boolean.toString(complete));
        return searchResult;
    }

    private FileInfo getItemInfo(BoxItem.Info itemInfo, Metadata folderMetadata) {
        FileInfo fileInfo = new FileInfo();
        String fileId = itemInfo.getID();
        String name = itemInfo.getName();
        String itemType = itemInfo.getType();
        fileInfo.setFileId(fileId);
        fileInfo.setFileName(name);
        fileInfo.setItemType(itemType);
        CreateFolderRequest createFolderRequest = getCitizenMetadata(folderMetadata);
        fileInfo.setCitizenMetadata(createFolderRequest);
        return fileInfo;
    }

    private CreateFolderRequest getCitizenMetadata(Metadata folderMetadata) {
        CreateFolderRequest createFolderRequest = new CreateFolderRequest();
        createFolderRequest.setFirstName(getMetadataStringField(folderMetadata, "/" + appProperties.getCitizenMetadataTemplateFirstName()));
        createFolderRequest.setLastName(getMetadataStringField(folderMetadata, "/" + appProperties.getCitizenMetadataTemplateLastName()));
        createFolderRequest.setMpiId(getMetadataStringField(folderMetadata, "/" + appProperties.getCitizenMetadataTemplateMpiId()));
        createFolderRequest.setLogonUserId(getMetadataStringField(folderMetadata, "/" + appProperties.getCitizenMetadataTemplateLogonUserId()));
        return createFolderRequest;
    }

    private String getMetadataStringField(Metadata metadata, String fieldPath) {
        String fieldValue = null;
        try {
            fieldValue = metadata.getString(fieldPath);
        } catch (Exception e)
        {}
        return fieldValue;
    }

    private LocalDate getMetadataDateField(Metadata metadata, String fieldPath) {
        LocalDate fieldValue = null;
        try {
            String fieldValueAsString = metadata.getString(fieldPath);
            String dateValueAsString = fieldValueAsString.substring(0, fieldValueAsString.indexOf('T'));
            fieldValue = LocalDate.parse(dateValueAsString);
        } catch (Exception e)
        {}
        return fieldValue;
    }

}
