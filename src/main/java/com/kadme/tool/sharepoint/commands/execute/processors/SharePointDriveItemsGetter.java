/* Decompiler 91ms, total 224ms, lines 182 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.google.common.collect.Sets;
import com.kadme.dataservice.DefaultQueryResults;
import com.kadme.dataservice.query.Query;
import com.kadme.dataservice.query.SearchRequest;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.DataServiceRemote;
import com.kadme.rest.utils.json.JsonUtil;
import com.kadme.tool.sharepoint.command.execute.rdfizer.imp.GeneralFilesRdfizer;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.services.SharePointStoreHelperService;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.kadme.tool.sharepoint.transfer.FileItemTO;
import com.kadme.tool.sharepoint.util.ldap.SharePointToLdapUserMaping;
import com.kadme.util.KDateUtil;
import com.kadme.util.RdfUrlUtil;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.models.extensions.Permission;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IPermissionCollectionPage;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharePointDriveItemsGetter {
   private static final Log logger = LogFactory.getLog(SharePointDriveItemsGetter.class);
   private final Map<String, String> permissionMap = new ConcurrentHashMap();
   private final SharePointAuthorizationProcessorBean sharePointAuthorisation;

   public SharePointDriveItemsGetter(SharePointAuthorizationProcessorBean sharePointAuthorisation) {
      this.sharePointAuthorisation = sharePointAuthorisation;
   }

   public MetaDomain processFile(DriveItem driveItem, String driveId, String filePath, String siteName, String subSite, boolean addToContextHolder) {
      logger.trace("Getting subsite:" + subSite + " \tFilename: " + driveItem.name);
      MetaDomain metaDomain = null;
      if (this.shouldDownloadFile(driveItem, siteName)) {
         StringBuilder kmetaEntBuff = new StringBuilder();
         String kmetaEnt = null;
         boolean isMDExists = this.isMetadomainExists(driveItem, kmetaEntBuff);
         if (isMDExists) {
            logger.trace("File has not changed.");
            if (kmetaEntBuff.length() > 0) {
               kmetaEnt = kmetaEntBuff.toString();
            }
         }

         if (!isMDExists || UserConfigurationContainer.getInstance().isForceUpdateEnabled()) {
            if (kmetaEnt == null) {
               kmetaEnt = this.gettingPermission(driveItem, driveId);
            }

            FileItemTO fileItem = SharePointStoreHelperService.createFileItem(driveItem, driveId, filePath, kmetaEnt, filePath, subSite, siteName);
            metaDomain = GeneralFilesRdfizer.createMetaDomain(fileItem);
            if (addToContextHolder) {
               synchronized(UserConfigurationContainer.getInstance().getContextHolder().getProcessedMetadomain()) {
                  UserConfigurationContainer.getInstance().getContextHolder().getProcessedMetadomain().add(metaDomain);
               }
            }
         }
      }

      return metaDomain;
   }

   private boolean shouldDownloadFile(DriveItem driveItem, String siteName) {
      boolean result = false;
      String currFileExt = FilenameUtils.getExtension(driveItem.name);
      String[] fileExtensions = UserConfigurationContainer.getInstance().getFileExtensions();
      if (fileExtensions != null) {
         String[] var6 = fileExtensions;
         int var7 = fileExtensions.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String fileExt = var6[var8];
            if (currFileExt.equalsIgnoreCase(fileExt)) {
               result = true;
               break;
            }
         }
      } else {
         result = true;
      }

      return result;
   }

   private boolean isMetadomainExists(DriveItem driveItem, StringBuilder kmetaEnt) {
      boolean isExisting = false;
      String metaClassName = ((DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class)).generateFileItemSchema("Documents");
      String uri = RdfUrlUtil.createURI(metaClassName, driveItem.id + driveItem.webUrl);
      SearchRequest srLocators = new SearchRequest();
      srLocators.getQuery().clas(metaClassName);
      srLocators.getQuery().getAnd().add((new Query()).setId(uri));
      srLocators.setLoadProperties(Sets.newHashSet(new String[]{"shpt:FileSystemlastModifiedDateTime", "kmeta:Ent"}));
      DataServiceRemote dataServiceRemote = UserConfigurationContainer.getInstance().getContextHolder().getDataServiceRemote();

      try {
         DefaultQueryResults<MetaDomain> searchResult = dataServiceRemote.search(UserConfigurationContainer.getInstance().getOntologyNamespace(), srLocators);
         if (searchResult.getEntities() != null && searchResult.getEntities().size() > 0) {
            Date lastModTime = KDateUtil.parseDate(((MetaDomain)searchResult.getEntities().get(0)).getProperties().get("shpt:FileSystemlastModifiedDateTime").toString());
            Date lastModTimeItem = driveItem.fileSystemInfo.lastModifiedDateTime.getTime();
            if (lastModTimeItem.compareTo(lastModTime) == 0) {
               isExisting = true;
               Object kmetaEntObj = ((MetaDomain)searchResult.getEntities().get(0)).getProperty("kmeta:Ent");
               if (kmetaEntObj != null && kmetaEntObj instanceof String[]) {
                  kmetaEnt.append(StringUtils.join((String[])kmetaEntObj, ','));
               }
            }
         }
      } catch (Exception var12) {
         logger.warn("Search query returned error:" + var12.getMessage() + " for query:" + JsonUtil.toJson(srLocators));
      }

      return isExisting;
   }

   private String gettingPermission(DriveItem driveItem, String driveId) {
      String userListResult = null;
      List<Option> options = Arrays.asList(new QueryOption("select", "grantedTo"));
      int count = 0;

      while(count != 5) {
         try {
            if (this.permissionMap.containsKey(driveId + ":" + driveItem.id)) {
               userListResult = (String)this.permissionMap.get(driveId + ":" + driveItem.id);
               break;
            }

            IPermissionCollectionPage permissions = this.sharePointAuthorisation.getGraphServiceCilent().drives(driveId).items(driveItem.id).permissions().buildRequest(options).get();
            if (permissions.getCurrentPage() == null) {
               break;
            }

            Set<String> permisionUsersSet = new HashSet();
            Iterator var8 = permissions.getCurrentPage().iterator();

            while(var8.hasNext()) {
               Permission permision = (Permission)var8.next();
               if (permision.grantedTo != null && permision.grantedTo.user != null && permision.grantedTo.user.displayName != null && !permision.grantedTo.user.displayName.isEmpty()) {
                  permisionUsersSet.add(permision.grantedTo.user.displayName);
               }
            }

            if (!permisionUsersSet.isEmpty()) {
               userListResult = StringUtils.join(SharePointToLdapUserMaping.mapLDAPPermissionsUsers(permisionUsersSet), ',');
               this.permissionMap.put(driveId + ":" + driveItem.id, userListResult);
            }
            break;
         } catch (ClientException var11) {
            logger.warn("Could not get permission for drive: " + driveId + " wait 10 sec. [" + count + "/" + 5 + "] error message:" + var11.getMessage());

            try {
               Thread.sleep(10000L);
            } catch (InterruptedException var10) {
               logger.error("Exception catched:", var11);
            }

            ++count;
         }
      }

      if (userListResult == null) {
         logger.warn("Could not get permission for drive: " + driveId + " item ID" + driveItem.id);
      }

      return userListResult;
   }
}
