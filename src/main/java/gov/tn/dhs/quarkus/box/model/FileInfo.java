package gov.tn.dhs.quarkus.box.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileInfo {

  @JsonProperty("fileId")
  private String fileId;

  @JsonProperty("fileName")
  private String fileName;

  @JsonProperty("itemType")
  private String itemType;

  @JsonProperty("citizenMetadata")
  private CreateFolderRequest createFolderRequest;

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getItemType() {
    return itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public CreateFolderRequest getCitizenMetadata() {
    return createFolderRequest;
  }

  public void setCitizenMetadata(CreateFolderRequest createFolderRequest) {
    this.createFolderRequest = createFolderRequest;
  }
}

