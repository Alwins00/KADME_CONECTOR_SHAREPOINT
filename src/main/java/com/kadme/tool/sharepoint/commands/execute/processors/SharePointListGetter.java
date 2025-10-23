/* Decompiler 86ms, total 227ms, lines 274 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.BufferedDataService;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.command.execute.rdfizer.imp.GeneralListRdfizer;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.kadme.tool.sharepoint.util.SitePermissionCheck;
import com.kadme.tool.sharepoint.util.ldap.SharePointToLdapUserMaping;
import com.kadme.util.RdfUrlUtil;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.http.GraphServiceException;
import com.microsoft.graph.models.extensions.ColumnDefinition;
import com.microsoft.graph.models.extensions.ListItem;
import com.microsoft.graph.models.extensions.Permission;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IListCollectionPage;
import com.microsoft.graph.requests.extensions.IListCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IPermissionCollectionPage;
import com.microsoft.graph.serializer.AdditionalDataManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Jsoup;
import org.springframework.web.util.HtmlUtils;

public class SharePointListGetter {
   private static final Log logger = LogFactory.getLog(SharePointListGetter.class);
   private static final String REQUEST_ADDITIONAL_PARAMETERS = "columns,items(expand=fields)";
   private static final List<String> COLUMNS_IGNORE = Arrays.asList("_UIVersionString", "FolderChildCount", "ItemChildCount", "End_x0020_date", "Start_x0020_date", "_UIVersionString", "Total_x0020_number_x0020_of_x002", "Attachments");
   private static final String COLUMN_GROUP_HIDDEN = "_Hidden";
   final SharePointAuthorizationProcessorBean sharePointAuthorisation;
   private static final String GENERIC_LIST_TYPE = "genericList";
   private static final String TASK_LIST_TYPE = "task";
   private static final String COLUMN_NAME_URL = "Url";
   private final Reporter reporter;

   public SharePointListGetter(Reporter reporter) {
      this.reporter = reporter;
      this.sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
   }

   public void processList(SiteItemInternal site, List<User> sharePointUsersLst) throws Exception {
      IListCollectionRequestBuilder request = null;
      List<Option> options = Arrays.asList(new QueryOption("expand", "columns,items(expand=fields)"));
      IListCollectionPage listCollection = null;

      for(int count = 0; count != 5; ++count) {
         try {
            if (!this.sharePointAuthorisation.isTokenValid()) {
               request = null;
            }

            if (request == null) {
               request = this.sharePointAuthorisation.getGraphServiceCilent().sites(site.id).lists();
            }

            listCollection = request.buildRequest(options).get();
         } catch (Exception var11) {
            if (var11 instanceof ClientException) {
               logger.warn("MS Graph ClientException occured next try [" + count + "/" + 5 + "] site:" + site.displayName, var11);

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException var10) {
                  logger.error(var10);
               }

               request = null;
               continue;
            }

            throw new Exception(var11);
         }

         if (listCollection != null) {
            break;
         }
      }

      if (listCollection == null) {
         listCollection = request.buildRequest(options).get();
      }

      Set<MetaDomain> metaDomiansLst = new HashSet();
      Iterator var7 = listCollection.getCurrentPage().iterator();

      while(var7.hasNext()) {
         com.microsoft.graph.models.extensions.List sharepointList = (com.microsoft.graph.models.extensions.List)var7.next();
         if ("genericList".equalsIgnoreCase(sharepointList.list.template)) {
            this.analyzeGenericList(site, sharepointList, metaDomiansLst, sharePointUsersLst);
         }
      }

      if (metaDomiansLst.size() > 0) {
         BufferedDataService bufferedDataService = UserConfigurationContainer.getInstance().getContextHolder().getBufferedDataService();

         try {
            bufferedDataService.registerEntities(metaDomiansLst);
            bufferedDataService.flush();
         } catch (WhereoilException var9) {
            logger.error(var9);
         }
      }

   }

   private String gettingPermission(String siteId, List<User> sharePointUsersLst) {
      try {
         this.sharePointAuthorisation.getGraphServiceCilent().sites(siteId).drive().buildRequest(new Option[0]).get();
      } catch (GraphServiceException var8) {
         Set<String> permisionUsersSet = new HashSet();
         sharePointUsersLst.forEach((u) -> {
            if (u.displayName != null && !u.displayName.isEmpty()) {
               permisionUsersSet.add(u.displayName);
            }

         });
         return StringUtils.join(SharePointToLdapUserMaping.mapLDAPPermissionsUsers(permisionUsersSet), ',');
      }

      IPermissionCollectionPage permissions = this.sharePointAuthorisation.getGraphServiceCilent().sites(siteId).drive().root().permissions().buildRequest(new Option[0]).get();
      String userListResult = null;
      if (permissions.getCurrentPage() != null) {
         Set<String> permisionUsersSet = new HashSet();
         Iterator var6 = permissions.getCurrentPage().iterator();

         while(var6.hasNext()) {
            Permission permision = (Permission)var6.next();
            if (permision.grantedTo != null && permision.grantedTo.user != null && permision.grantedTo.user.displayName != null && !permision.grantedTo.user.displayName.isEmpty()) {
               if (SitePermissionCheck.isFullAccessSite(permision.grantedTo.user.displayName)) {
                  sharePointUsersLst.forEach((u) -> {
                     if (u.displayName != null && !u.displayName.isEmpty()) {
                        permisionUsersSet.add(u.displayName);
                     }

                  });
               } else {
                  permisionUsersSet.add(permision.grantedTo.user.displayName);
               }
            }
         }

         if (!permisionUsersSet.isEmpty()) {
            userListResult = StringUtils.join(SharePointToLdapUserMaping.mapLDAPPermissionsUsers(permisionUsersSet), ',');
         }
      }

      return userListResult;
   }

   private String getValueAsString(JsonElement jsonElement, String title) {
      String value = null;

      try {
         if (!jsonElement.isJsonArray() && !jsonElement.isJsonPrimitive()) {
            if (jsonElement.isJsonObject()) {
               JsonObject jsonObject = (JsonObject)jsonElement;
               if ("Url".equalsIgnoreCase(title)) {
                  value = jsonObject.get("Url").getAsString();
               } else {
                  value = jsonObject.toString();
               }
            }
         } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = (JsonArray)jsonElement;
            if (jsonArray.size() > 1) {
               value = "";

               JsonElement jsonElementInt;
               for(Iterator var5 = jsonArray.iterator(); var5.hasNext(); value = value + StringUtils.join(new JsonElement[]{jsonElementInt})) {
                  jsonElementInt = (JsonElement)var5.next();
               }
            } else {
               value = jsonElement.getAsString();
            }
         } else {
            value = jsonElement.getAsString();
         }
      } catch (UnsupportedOperationException var7) {
         logger.warn("Could not get string from JSON column name:" + title);
      }

      return value;
   }

   public static boolean isHtml(String input) {
      boolean isHtml = false;
      if (input != null && !input.equals(HtmlUtils.htmlEscape(input))) {
         isHtml = true;
      }

      return isHtml;
   }

   private String parseColumnValue(String columnValue) {
      String result = columnValue;
      if (this.isBinaryImageData(columnValue)) {
         result = null;
      } else if (isHtml(columnValue)) {
         result = Jsoup.parse(columnValue).wholeText();
      }

      return result;
   }

   private boolean isBinaryImageData(String columnValue) {
      boolean isBinary = false;
      if (columnValue != null && columnValue.startsWith("data:image/")) {
         isBinary = true;
      }

      return isBinary;
   }

   private void analyzeGenericList(SiteItemInternal site, com.microsoft.graph.models.extensions.List sharepointList, Set<MetaDomain> metaDomiansLst, List<User> sharePointUsersLst) {
      if (sharepointList.columns != null && sharepointList.items != null && sharepointList.items.getCurrentPage().size() > 0) {
         String sitePermissions = this.gettingPermission(site.id, sharePointUsersLst);
         this.reporter.reportInfo("\tGetting list:" + sharepointList.name + " url:" + sharepointList.webUrl);
         Iterator var6 = sharepointList.columns.getCurrentPage().iterator();

         while(true) {
            ColumnDefinition columnDef;
            int rowCount;
            do {
               if (!var6.hasNext()) {
                  return;
               }

               columnDef = (ColumnDefinition)var6.next();
               rowCount = sharepointList.items.getCurrentPage().size();
            } while(rowCount <= 0);

            for(int rowNumber = 0; rowNumber != rowCount; ++rowNumber) {
               if (((ListItem)sharepointList.items.getCurrentPage().get(rowNumber)).fields != null) {
                  AdditionalDataManager addDataManager = ((ListItem)sharepointList.items.getCurrentPage().get(rowNumber)).fields.additionalDataManager();
                  if (addDataManager.get(columnDef.name) != null && !COLUMNS_IGNORE.contains(columnDef.name) && !"_Hidden".equalsIgnoreCase(columnDef.columnGroup) && !"CacheData".equals(columnDef.displayName)) {
                     JsonElement jsonElement = (JsonElement)addDataManager.get(columnDef.name);
                     String value = this.parseColumnValue(this.getValueAsString(jsonElement, columnDef.name));
                     if (value != null && !value.isEmpty()) {
                        MetaDomain metaDomain = GeneralListRdfizer.createMetaDomain(site, sharepointList);
                        metaDomain.setUri(RdfUrlUtil.createURI(metaDomain.getType(), sharepointList.id + ":" + columnDef.id + ":" + rowNumber));
                        if (value.length() > 32000) {
                           value = value.substring(0, 32000) + "...";
                           metaDomain.setProperty("kmeta:QC", Collections.singleton("VALUE_CLIPPED"));
                        }

                        metaDomain.setProperty("shpt:ColumnName", columnDef.displayName);
                        metaDomain.setProperty("shpt:ColumnValue", value);
                        metaDomain.setProperty("shpt:RowNumber", rowNumber + 1);
                        metaDomain.setProperty("kmeta:Ent", sitePermissions);
                        metaDomiansLst.add(metaDomain);
                     }
                  }
               }
            }
         }
      }
   }
}
