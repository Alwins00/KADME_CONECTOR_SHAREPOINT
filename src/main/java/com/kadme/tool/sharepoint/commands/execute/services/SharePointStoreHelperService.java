/* Decompiler 14ms, total 126ms, lines 62 */
package com.kadme.tool.sharepoint.commands.execute.services;

import com.google.gson.JsonElement;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.transfer.FileItemTO;
import com.microsoft.graph.models.extensions.DriveItem;
import org.apache.commons.io.FilenameUtils;

public class SharePointStoreHelperService {
   public static FileItemTO createFileItem(DriveItem driveItem, String driveId, String filePath, String kmetaEnt, String directory, String subSiteName, String siteName) {
      FileItemTO fileItem = new FileItemTO();
      if (driveItem.createdBy != null && driveItem.createdBy.user != null) {
         fileItem.setCreatedByDisplayName(driveItem.createdBy.user.displayName);
         fileItem.setCreatedById(driveItem.createdBy.user.id);
      }

      fileItem.setCreatedDateTime(driveItem.createdDateTime.getTime());
      fileItem.setDownloadUrl(driveItem.webUrl);
      if (driveItem.fileSystemInfo != null && driveItem.fileSystemInfo.createdDateTime != null) {
         fileItem.setFileSystemInfoCreatedDateTime(driveItem.fileSystemInfo.createdDateTime.getTime());
      }

      if (driveItem.fileSystemInfo != null && driveItem.fileSystemInfo.lastModifiedDateTime != null) {
         fileItem.setFileSystemlastModifiedDateTime(driveItem.fileSystemInfo.lastModifiedDateTime.getTime());
      }

      fileItem.setId(driveItem.id);
      if (driveItem.lastModifiedBy != null && driveItem.lastModifiedBy.user != null) {
         fileItem.setLastModifiedByDisplayName(driveItem.lastModifiedBy.user.displayName);
         fileItem.setLastModifiedById(driveItem.lastModifiedBy.user.id);
         fileItem.setLastModifiedDateTime(driveItem.lastModifiedDateTime.getTime());
      }

      fileItem.setMimeType(driveItem.file.mimeType);
      fileItem.setName(driveItem.name);
      fileItem.setParentReferenceDriveId(driveItem.parentReference.driveId);
      fileItem.setParentReferenceDriveType(driveItem.parentReference.driveType);
      fileItem.setParentReferencePath(driveItem.parentReference.path);
      fileItem.setSize(driveItem.size);
      fileItem.setWebUrl(driveItem.webUrl);
      fileItem.setLocalFilesystemPath(filePath);
      fileItem.setDocumentLibraryName(directory);
      fileItem.setKmetaEnt(kmetaEnt);
      fileItem.setSubSiteName(subSiteName);
      fileItem.setSiteName(siteName);
      fileItem.setIsProcessed(false);
      fileItem.setDriveId(driveId);
      if (UserConfigurationContainer.getInstance().getCustomFileTitleColumnName() != null && !UserConfigurationContainer.getInstance().getCustomFileTitleColumnName().isEmpty()) {
         if (driveItem.listItem != null && driveItem.listItem.fields != null && driveItem.listItem.fields.additionalDataManager() != null) {
            JsonElement custField = (JsonElement)driveItem.listItem.fields.additionalDataManager().get(UserConfigurationContainer.getInstance().getCustomFileTitleColumnName());
            fileItem.setFileTitle(custField != null ? custField.getAsString() : null);
         } else {
            fileItem.setFileTitle((String)null);
         }
      } else {
         fileItem.setFileTitle(FilenameUtils.removeExtension(driveItem.name));
      }

      return fileItem;
   }
}
