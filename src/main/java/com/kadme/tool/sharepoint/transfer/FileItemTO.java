/* Decompiler 148ms, total 461ms, lines 270 */
package com.kadme.tool.sharepoint.transfer;

import java.util.Date;

public class FileItemTO {
   private FileItemTO.FileItemType fileItemType;
   private Date createdDateTime;
   private String id;
   private Date lastModifiedDateTime;
   private String name;
   private String webUrl;
   private Long size;
   private String createdByEmail;
   private String createdById;
   private String createdByDisplayName;
   private String lastModifiedByEmail;
   private String lastModifiedById;
   private String lastModifiedByDisplayName;
   private String parentReferenceDriveId;
   private String parentReferenceDriveType;
   private String parentReferencePath;
   private String mimeType;
   private Date fileSystemInfoCreatedDateTime;
   private Date fileSystemlastModifiedDateTime;
   private String localFilesystemPath;
   private String downloadUrl;
   private Boolean isProcessed;
   private String kmetaEnt;
   private String documentLibraryName;
   private String subSiteName;
   private String driveId;
   private String siteName;
   private String fileTitle;

   public FileItemTO() {
      this.fileItemType = FileItemTO.FileItemType.UNDEFINED;
   }

   public FileItemTO.FileItemType getFileItemType() {
      return this.fileItemType;
   }

   public void setFileItemType(FileItemTO.FileItemType fileItemType) {
      this.fileItemType = fileItemType;
   }

   public Date getCreatedDateTime() {
      return this.createdDateTime;
   }

   public void setCreatedDateTime(Date createdDateTime) {
      this.createdDateTime = createdDateTime;
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public Date getLastModifiedDateTime() {
      return this.lastModifiedDateTime;
   }

   public void setLastModifiedDateTime(Date lastModifiedDateTime) {
      this.lastModifiedDateTime = lastModifiedDateTime;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getWebUrl() {
      return this.webUrl;
   }

   public void setWebUrl(String webUrl) {
      this.webUrl = webUrl;
   }

   public Long getSize() {
      return this.size;
   }

   public void setSize(Long size) {
      this.size = size;
   }

   public String getCreatedByEmail() {
      return this.createdByEmail;
   }

   public void setCreatedByEmail(String createdByEmail) {
      this.createdByEmail = createdByEmail;
   }

   public String getCreatedById() {
      return this.createdById;
   }

   public void setCreatedById(String createdById) {
      this.createdById = createdById;
   }

   public String getCreatedByDisplayName() {
      return this.createdByDisplayName;
   }

   public void setCreatedByDisplayName(String createdByDisplayName) {
      this.createdByDisplayName = createdByDisplayName;
   }

   public String getLastModifiedByEmail() {
      return this.lastModifiedByEmail;
   }

   public void setLastModifiedByEmail(String lastModifiedByEmail) {
      this.lastModifiedByEmail = lastModifiedByEmail;
   }

   public String getLastModifiedById() {
      return this.lastModifiedById;
   }

   public void setLastModifiedById(String lastModifiedById) {
      this.lastModifiedById = lastModifiedById;
   }

   public String getLastModifiedByDisplayName() {
      return this.lastModifiedByDisplayName;
   }

   public void setLastModifiedByDisplayName(String lastModifiedByDisplayName) {
      this.lastModifiedByDisplayName = lastModifiedByDisplayName;
   }

   public String getParentReferenceDriveId() {
      return this.parentReferenceDriveId;
   }

   public void setParentReferenceDriveId(String parentReferenceDriveId) {
      this.parentReferenceDriveId = parentReferenceDriveId;
   }

   public String getParentReferenceDriveType() {
      return this.parentReferenceDriveType;
   }

   public void setParentReferenceDriveType(String parentReferenceDriveType) {
      this.parentReferenceDriveType = parentReferenceDriveType;
   }

   public String getParentReferencePath() {
      return this.parentReferencePath;
   }

   public void setParentReferencePath(String parentReferencePath) {
      this.parentReferencePath = parentReferencePath;
   }

   public String getMimeType() {
      return this.mimeType;
   }

   public void setMimeType(String mimeType) {
      this.mimeType = mimeType;
   }

   public Date getFileSystemInfoCreatedDateTime() {
      return this.fileSystemInfoCreatedDateTime;
   }

   public void setFileSystemInfoCreatedDateTime(Date fileSystemInfoCreatedDateTime) {
      this.fileSystemInfoCreatedDateTime = fileSystemInfoCreatedDateTime;
   }

   public Date getFileSystemlastModifiedDateTime() {
      return this.fileSystemlastModifiedDateTime;
   }

   public void setFileSystemlastModifiedDateTime(Date fileSystemlastModifiedDateTime) {
      this.fileSystemlastModifiedDateTime = fileSystemlastModifiedDateTime;
   }

   public String getLocalFilesystemPath() {
      return this.localFilesystemPath;
   }

   public void setLocalFilesystemPath(String localFilesystemPath) {
      this.localFilesystemPath = localFilesystemPath;
   }

   public String getDownloadUrl() {
      return this.downloadUrl;
   }

   public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
   }

   public Boolean getIsProcessed() {
      return this.isProcessed;
   }

   public void setIsProcessed(Boolean isProcessed) {
      this.isProcessed = isProcessed;
   }

   public String getKmetaEnt() {
      return this.kmetaEnt;
   }

   public void setKmetaEnt(String kmetaEnt) {
      this.kmetaEnt = kmetaEnt;
   }

   public String getDocumentLibraryName() {
      return this.documentLibraryName;
   }

   public void setDocumentLibraryName(String documentLibraryName) {
      this.documentLibraryName = documentLibraryName;
   }

   public String getSubSiteName() {
      return this.subSiteName;
   }

   public void setSubSiteName(String subSiteName) {
      this.subSiteName = subSiteName;
   }

   public String getDriveId() {
      return this.driveId;
   }

   public void setDriveId(String driveId) {
      this.driveId = driveId;
   }

   public String getSiteName() {
      return this.siteName;
   }

   public void setSiteName(String siteName) {
      this.siteName = siteName;
   }

   public String getFileTitle() {
      return this.fileTitle;
   }

   public void setFileTitle(String fileTitle) {
      this.fileTitle = fileTitle;
   }

   static enum FileItemType {
      UNDEFINED,
      MODIFIED,
      ADDED,
      DELETED;
   }
}
