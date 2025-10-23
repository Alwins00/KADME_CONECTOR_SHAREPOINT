/* Decompiler 66ms, total 187ms, lines 214 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.ISharePointBaseBean;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Drive;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.IDriveCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveItemCollectionRequestBuilder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component("SharePointSiteGetter")
@Scope("prototype")
public class SharePointSiteGetter extends ISharePointBaseBean<SharePointSiteGetter> {
   public static final QueryOption REQUEST_ADDITIONAL_PARAMETERS = new QueryOption("expand", "listItem");
   private static final Log logger = LogFactory.getLog(SharePointSiteGetter.class);
   private SharePointAuthorizationProcessorBean sharePointAuthorisation;
   private SharePointDriveItemsGetter sharePointDriveItemsGetter;

   public void init() {
      this.sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
      this.sharePointDriveItemsGetter = new SharePointDriveItemsGetter(this.sharePointAuthorisation);
   }

   @Async("asyncSharepointGetterExecutor")
   public CompletableFuture<Boolean> processFileItems(SiteItemInternal siteItem, String runId) throws Exception {
      boolean result = true;
      IDriveCollectionRequestBuilder request = null;
      IDriveCollectionPage driversLst = null;

      for(int count = 0; count != 5; ++count) {
         try {
            if (!this.sharePointAuthorisation.isTokenValid()) {
               request = null;
            }

            if (request == null) {
               request = this.sharePointAuthorisation.getGraphServiceCilent().sites(siteItem.id).drives();
               logger.info("Getting sites request URL:" + request.getRequestUrl() + " dispaly name:" + siteItem.displayName);
            }

            driversLst = request.buildRequest(new Option[0]).get();
         } catch (Exception var10) {
            if (var10 instanceof ClientException) {
               logger.warn("MS Graph ClientException occured next try [" + count + "/" + 5 + "] site:" + siteItem.displayName, var10);

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException var9) {
                  logger.error(var9);
               }

               request = null;
               continue;
            }

            throw new Exception(var10);
         }

         if (driversLst != null) {
            break;
         }
      }

      if (driversLst == null) {
         driversLst = request.buildRequest(new Option[0]).get();
      }

      List<Drive> driveLst = driversLst.getCurrentPage();
      Iterator var7 = driveLst.iterator();

      while(var7.hasNext()) {
         Drive drive = (Drive)var7.next();
         logger.info("Getting drive URL: " + drive.webUrl + " site display name:" + siteItem.displayName + " subSiteName:" + siteItem.subSiteName);
         this.processDrive(drive, siteItem.displayName, siteItem.subSiteName);
         this.storeMetaDomains();
         UserConfigurationContainer.getInstance().getContextHolder().getBufferedDataService().flush();
      }

      return CompletableFuture.completedFuture(result);
   }

   public void processDrive(Drive drive, String siteName, String subSiteName) {
      int count = 0;

      while(count != 5) {
         try {
            IDriveItemCollectionRequestBuilder request = this.sharePointAuthorisation.getGraphServiceCilent().drives(drive.id).root().children();
            IDriveItemCollectionPage iDriveCollection = request.buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();
            List<DriveItem> driveItemsLst = iDriveCollection.getCurrentPage();
            String filePath = siteName + "/" + drive.name;
            this.processDriveItems(driveItemsLst, drive.id, filePath, siteName, subSiteName, true);
            if (iDriveCollection.getNextPage() == null) {
               break;
            }

            IDriveItemCollectionPage nextIDriveCollection = ((IDriveItemCollectionRequestBuilder)iDriveCollection.getNextPage()).buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();

            while(nextIDriveCollection != null && nextIDriveCollection.getCurrentPage() != null) {
               driveItemsLst = nextIDriveCollection.getCurrentPage();
               this.processDriveItems(driveItemsLst, drive.id, filePath, siteName, subSiteName, true);
               if (nextIDriveCollection.getNextPage() != null) {
                  nextIDriveCollection = ((IDriveItemCollectionRequestBuilder)nextIDriveCollection.getNextPage()).buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();
               } else {
                  nextIDriveCollection = null;
               }
            }

            return;
         } catch (ClientException var11) {
            logger.warn("MS Graph ClientException occured during drive process next try [" + count + "/" + 5 + "] drive name:" + drive.name + "error:" + var11.getMessage());

            try {
               Thread.sleep(10000L);
            } catch (InterruptedException var10) {
               logger.error(var10);
            }

            ++count;
         }
      }

   }

   public void processDriveItems(List<DriveItem> driveItemsLst, String driveId, String filePath, String siteName, String subSite, boolean firstCall) {
      for(Iterator var7 = driveItemsLst.iterator(); var7.hasNext(); this.storeMetaDomains()) {
         DriveItem driveItem = (DriveItem)var7.next();
         int count = 0;

         while(count != 5) {
            try {
               this.processDriveItem(driveItem, driveId, filePath, siteName, subSite, firstCall);
               break;
            } catch (ClientException var13) {
               logger.warn("Exception catch during processing drive:" + driveId + " folder JSON:" + driveItem.folder.getRawObject() + " continue opperation.[" + count + "/" + 5 + "] error:" + var13.getMessage());

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException var12) {
                  logger.error("processDriveItems exception.", var13);
               }

               ++count;
            }
         }
      }

   }

   private synchronized void storeMetaDomains() {
      ContextHolder contextHolder = UserConfigurationContainer.getInstance().getContextHolder();
      if (!contextHolder.getProcessedMetadomain().isEmpty()) {
         try {
            synchronized(contextHolder.getProcessedMetadomain()) {
               contextHolder.getBufferedDataService().registerEntities(contextHolder.getProcessedMetadomain());
               contextHolder.cleanProcessedMetadomains();
            }
         } catch (WhereoilException var5) {
            logger.error("Could not store " + contextHolder.getProcessedMetadomain().size() + " metadomains.", var5);
         }
      }

   }

   private void processDriveItem(DriveItem driveItem, String driveId, String filePath, String siteName, String subSite, boolean firstCall) throws ClientException {
      if (driveItem.folder != null) {
         if (driveItem.folder.childCount > 0) {
            this.processFolder(driveItem, driveId, filePath, siteName, subSite);
         }
      } else {
         this.sharePointDriveItemsGetter.processFile(driveItem, driveId, filePath, siteName, subSite, true);
      }

   }

   public void processFolder(DriveItem driveItem, String driveId, String filePath, String siteName, String subSite) {
      IDriveItemCollectionRequestBuilder request = this.sharePointAuthorisation.getGraphServiceCilent().drives(driveId).items(driveItem.id).children();
      logger.trace("Getting drive items request URL:" + request.getRequestUrl());
      filePath = filePath + "/" + driveItem.name;
      IDriveItemCollectionPage iDriveitemCollection = request.buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();
      List<DriveItem> driveItemsLst = iDriveitemCollection.getCurrentPage();
      this.processDriveItems(driveItemsLst, driveId, filePath, siteName, subSite, false);
      if (iDriveitemCollection.getNextPage() != null) {
         IDriveItemCollectionPage nextIDriveCollection = ((IDriveItemCollectionRequestBuilder)iDriveitemCollection.getNextPage()).buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();

         while(nextIDriveCollection != null && nextIDriveCollection.getCurrentPage() != null) {
            driveItemsLst = nextIDriveCollection.getCurrentPage();
            this.processDriveItems(driveItemsLst, driveId, filePath, siteName, subSite, false);
            if (nextIDriveCollection.getNextPage() != null) {
               nextIDriveCollection = ((IDriveItemCollectionRequestBuilder)nextIDriveCollection.getNextPage()).buildRequest(Arrays.asList(REQUEST_ADDITIONAL_PARAMETERS)).get();
            } else {
               nextIDriveCollection = null;
            }
         }
      }

   }
}
