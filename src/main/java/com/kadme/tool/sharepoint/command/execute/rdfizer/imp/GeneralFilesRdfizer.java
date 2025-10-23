/* Decompiler 7ms, total 139ms, lines 56 */
package com.kadme.tool.sharepoint.command.execute.rdfizer.imp;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.tool.sharepoint.command.execute.rdfizer.SharepointRdfizerBase;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.metadataupdate.UpdateMetadataDocOrganisationInfo;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.kadme.tool.sharepoint.transfer.FileItemTO;
import com.kadme.util.FormatMimeUtil;
import com.kadme.util.RdfUrlUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GeneralFilesRdfizer extends SharepointRdfizerBase {
   private static final Log logger = LogFactory.getLog(GeneralFilesRdfizer.class);

   public static MetaDomain createMetaDomain(FileItemTO fileItem) {
      MetaDomain metaDomain = new MetaDomain();
      String metaClassName = ((DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class)).generateFileItemSchema("Documents");
      metaDomain.setType(metaClassName);
      metaDomain.setUri(RdfUrlUtil.createURI(metaDomain.getType(), fileItem.getId() + fileItem.getWebUrl()));
      metaDomain.setProperty("kmeta:Creator", fileItem.getCreatedByDisplayName());
      metaDomain.setProperty("shpt:CreatorEmail", fileItem.getCreatedByEmail());
      metaDomain.setProperty("kmeta:Created", fileItem.getCreatedDateTime());
      metaDomain.setProperty("shpt:FileSystemInfoCreatedDateTime", fileItem.getFileSystemInfoCreatedDateTime());
      metaDomain.setProperty("shpt:FileSystemlastModifiedDateTime", fileItem.getFileSystemlastModifiedDateTime());
      metaDomain.setProperty("kmeta:LastModified", fileItem.getLastModifiedDateTime());
      metaDomain.setProperty("kmeta:FileLastModified", fileItem.getLastModifiedDateTime());
      metaDomain.setProperty("shpt:ModifedEmail", fileItem.getLastModifiedByEmail());
      metaDomain.setProperty("kmeta:LastEditor", fileItem.getLastModifiedByDisplayName());
      metaDomain.setProperty("shpt:LocalFilesystemPath", fileItem.getLocalFilesystemPath());
      metaDomain.setProperty("shpt:MimeType", fileItem.getMimeType());
      metaDomain.setProperty("kmeta:Format", FormatMimeUtil.getMimeType(fileItem.getName()));
      metaDomain.setProperty("shpt:ParentReferencePath", fileItem.getParentReferencePath());
      metaDomain.setProperty("kmeta:ResourcePath", fileItem.getWebUrl());
      metaDomain.setProperty("kmeta:Datasource", "SHAREPOINT-DOCUMENTS");
      metaDomain.setProperty("kmeta:FileName", fileItem.getName());
      metaDomain.setProperty("kmeta:FileTitle", fileItem.getFileTitle());
      metaDomain.setProperty("kmeta:Size", fileItem.getSize());
      metaDomain.setProperty("shpt:FileExt", FilenameUtils.getExtension(fileItem.getName()));
      metaDomain.setProperty("kmeta:Ent", fileItem.getKmetaEnt());
      metaDomain.setProperty("shpt:Folder", fileItem.getDocumentLibraryName());
      metaDomain.setProperty("shpt:SubSite", fileItem.getSubSiteName());
      metaDomain.setProperty("shpt:Site", fileItem.getSiteName());
      metaDomain.setProperty("shpt:InternalSharepointItemID", fileItem.getId());
      metaDomain.setProperty("shpt:InternalSharepointSiteID", fileItem.getDriveId());
      if (UserConfigurationContainer.getInstance().isUpdatingColumnEnabled()) {
         UpdateMetadataDocOrganisationInfo mdUpdate = new UpdateMetadataDocOrganisationInfo();
         mdUpdate.updatePWAMetadomainInformation(metaDomain);
      }

      return metaDomain;
   }
}
