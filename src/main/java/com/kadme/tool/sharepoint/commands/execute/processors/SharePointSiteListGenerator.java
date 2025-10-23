/* Decompiler 55ms, total 729ms, lines 187 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.ItemReference;
import com.microsoft.graph.models.extensions.Site;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.ISiteCollectionPage;
import com.microsoft.graph.requests.extensions.ISiteCollectionRequest;
import com.microsoft.graph.requests.extensions.ISiteCollectionRequestBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharePointSiteListGenerator {
   private static final Log LOG = LogFactory.getLog(SharePointSiteListGenerator.class);
   private static final String SEARCH_QUERY_DEFAULT = "*";
   private static final int SITE_GET_REQ_MAX_RETRIES = 5;
   private static final int SITE_GET_REQ_DELAY = 5;
   private List<SharePointSiteListGenerator.SiteItemInternal> rootSitesList = new ArrayList();

   public void getAllSites(SharePointAuthorizationProcessorBean sharePointAuthorisation) throws WhereoilException {
      List<Option> options = Arrays.asList(new QueryOption("select", "id,webUrl"), new QueryOption("search", "*"));
      LOG.info("Getting all sites list.");
      ISiteCollectionRequest sitesCollectionReq = sharePointAuthorisation.getGraphServiceCilent().sites().buildRequest(options);
      sitesCollectionReq.setMaxRetries(5);
      sitesCollectionReq.setDelay(5L);
      ISiteCollectionPage sitesCollection = null;
      int i = 0;

      while(i != 5) {
         try {
            sitesCollection = sitesCollectionReq.get();
            break;
         } catch (ClientException var9) {
            LOG.info("Exception occured during request wait 3 second and retring [" + i + "/" + 5 + "]" + var9.getMessage());

            try {
               Thread.sleep(3000L);
            } catch (InterruptedException var8) {
            }

            ++i;
         }
      }

      if (sitesCollection == null) {
         throw new WhereoilException("java.lang.Exception: com.microsoft.graph.core.ClientException: Error during http request java.net.SocketTimeoutException: timeout");
      } else {
         if (sitesCollection.getCurrentPage() != null) {
            LOG.info("getAllSites Request:" + sharePointAuthorisation.getGraphServiceCilent().sites().buildRequest(options).getRequestUrl());
            this.getAllRootSites(sharePointAuthorisation, sitesCollection);
            LOG.info("List of sites downloaded.");
         } else {
            LOG.error("Critical error. Could not get root page. Please check clientID,SecretID and MS Graph application rights in sharepoint.");
         }

      }
   }

   private void getAllRootSites(SharePointAuthorizationProcessorBean sharePointAuthorisation, ISiteCollectionPage sitesCollection) {
      while(true) {
         if (sitesCollection.getCurrentPage() != null && sitesCollection.getCurrentPage().size() > 0) {
            LOG.trace("getAllRootSites size:" + sitesCollection.getCurrentPage().size());
            Iterator var3 = sitesCollection.getCurrentPage().iterator();

            while(var3.hasNext()) {
               Site siteItem = (Site)var3.next();
               LOG.trace("URL web:" + siteItem.webUrl);
               if (this.shouldDownloadSite(siteItem.webUrl)) {
                  try {
                     LOG.trace("siteDetails URL:" + sharePointAuthorisation.getGraphServiceCilent().sites(siteItem.id).buildRequest(new Option[0]).getRequestUrl());
                     Site siteDetails = sharePointAuthorisation.getGraphServiceCilent().sites(siteItem.id).buildRequest(new Option[0]).get();
                     LOG.info("Getting site information url:" + siteDetails.webUrl + " display name:" + siteDetails.displayName);
                     this.rootSitesList.add(new SharePointSiteListGenerator.SiteItemInternal(siteDetails.id, siteDetails.displayName, siteDetails.webUrl, siteDetails.lastModifiedDateTime.getTime(), siteDetails.parentReference, siteDetails.createdDateTime.getTime(), (String)null));
                     this.getSubSitesRecursive(siteDetails.displayName, (String)null, siteDetails.id, sharePointAuthorisation, true);
                  } catch (ClientException var6) {
                     LOG.warn("Exception during getting details site:" + siteItem.webUrl + " error:" + var6.getMessage());
                  }
               }
            }

            if (sitesCollection.getNextPage() != null) {
               try {
                  sitesCollection = ((ISiteCollectionRequestBuilder)sitesCollection.getNextPage()).buildRequest(new Option[0]).get();
                  continue;
               } catch (ClientException var7) {
                  LOG.warn("Exception during getting next site: " + var7.getMessage(), var7);
                  LOG.warn("Request:" + ((ISiteCollectionRequestBuilder)sitesCollection.getNextPage()).buildRequest(new Option[0]).getRequestUrl());
               }
            }
         }

         return;
      }
   }

   private void getSubSitesRecursive(String siteName, String subSiteName, String siteID, SharePointAuthorizationProcessorBean sharePointAuthorisation, boolean rootLevel) {
      ISiteCollectionRequest sitesCollectionReq = sharePointAuthorisation.getGraphServiceCilent().sites(siteID).sites().buildRequest(new Option[0]);
      sitesCollectionReq.setMaxRetries(5);
      sitesCollectionReq.setDelay(5L);
      ISiteCollectionPage sitesCollection = sitesCollectionReq.get();

      while(sitesCollection.getCurrentPage() != null && sitesCollection.getCurrentPage().size() > 0) {
         Iterator var8 = sitesCollection.getCurrentPage().iterator();

         while(var8.hasNext()) {
            Site siteItem = (Site)var8.next();
            if (rootLevel) {
               subSiteName = "";
            }

            LOG.info("\tGetting site:" + siteName + ". Subsite url:" + siteItem.webUrl + " subsite path:" + subSiteName + "/" + siteItem.displayName);
            this.rootSitesList.add(new SharePointSiteListGenerator.SiteItemInternal(siteItem.id, siteName, siteItem.webUrl, siteItem.lastModifiedDateTime.getTime(), siteItem.parentReference, siteItem.createdDateTime.getTime(), subSiteName + "/" + siteItem.displayName));
            this.getSubSitesRecursive(siteName, subSiteName + "/" + siteItem.displayName, siteItem.id, sharePointAuthorisation, false);
         }

         if (sitesCollection.getNextPage() == null) {
            break;
         }

         try {
            sitesCollection = ((ISiteCollectionRequestBuilder)sitesCollection.getNextPage()).buildRequest(new Option[0]).get();
         } catch (ClientException var10) {
            LOG.warn("Exception during getting next site: " + var10.getMessage());
            break;
         }
      }

   }

   private boolean shouldDownloadSite(String webUrl) {
      boolean result = true;
      if (webUrl != null && UserConfigurationContainer.getInstance().getSiteListToDownload() != null) {
         String[] var3 = UserConfigurationContainer.getInstance().getSiteListToDownload();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            String siteUrl = var3[var5];
            result = false;
            String siteUrlFormatted = siteUrl.replaceAll("/", "").trim().toLowerCase();
            String webUrlFormatted = webUrl.replaceAll("/", "").trim().toLowerCase();
            if (siteUrlFormatted.equalsIgnoreCase(webUrlFormatted)) {
               result = true;
               break;
            }
         }
      }

      return result;
   }

   public List<SharePointSiteListGenerator.SiteItemInternal> getRootSitesList() {
      return this.rootSitesList;
   }

   public static class SiteItemInternal {
      public final String id;
      public final String displayName;
      public final String subSiteName;
      public final String webUrl;
      public final Date lastModifiedDateTime;
      public final Date createdDateTime;
      public final ItemReference parentReference;

      public SiteItemInternal(String id, String displayName, String webUrl, Date lastModifiedDateTime, ItemReference parentReference, Date createdDateTime, String subSiteName) {
         this.id = id;
         this.displayName = displayName;
         this.webUrl = webUrl;
         this.lastModifiedDateTime = lastModifiedDateTime;
         this.parentReference = parentReference;
         this.createdDateTime = createdDateTime;
         this.subSiteName = subSiteName;
      }

      public String toString() {
         return "SiteItemInternal [id=" + this.id + ", displayName=" + this.displayName + ", subSiteName=" + this.subSiteName + ", webUrl=" + this.webUrl + ", lastModifiedDateTime=" + this.lastModifiedDateTime + ", createdDateTime=" + this.createdDateTime + ", parentReference=" + this.parentReference + "]";
      }
   }
}
