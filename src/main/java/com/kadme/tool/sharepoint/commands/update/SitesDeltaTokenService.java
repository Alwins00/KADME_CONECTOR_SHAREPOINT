/* Decompiler 290ms, total 713ms, lines 246 */
package com.kadme.tool.sharepoint.commands.update;

import com.kadme.dataservice.query.Query;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.DataServiceRemote;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointAuthorizationProcessorBean;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointDriveItemsGetter;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.kadme.tool.sharepoint.database.DatabaseOperationsControllerBean;
import com.kadme.tool.sharepoint.database.CrudBaseRepository.RepositoryName;
import com.kadme.tool.sharepoint.entity.SiteItemSnapshot;
import com.kadme.tool.sharepoint.entity.SnapshotsContainer;
import com.kadme.tool.sharepoint.repositories.SnapshotsContainerRepository;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.DriveItem;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.extensions.IDriveItemDeltaCollectionPage;
import com.microsoft.graph.requests.extensions.IDriveItemDeltaCollectionRequest;
import com.microsoft.graph.requests.extensions.IDriveItemDeltaCollectionRequestBuilder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math3.util.Pair;

public class SitesDeltaTokenService {
   private static final Log LOG = LogFactory.getLog(SitesDeltaTokenService.class);
   private static final String TOKEN_PARAM = "token=";
   private final SnapshotsContainerRepository snapshotsContainerRepository;
   private Set<String> removedItemsLst = new HashSet();
   private int addedDocCounter = 0;
   private final SharePointDriveItemsGetter sharePointDriveItemsGetter;
   private final SharePointAuthorizationProcessorBean sharePointAuthorisation;

   public SitesDeltaTokenService() {
      DatabaseOperationsControllerBean databaseRepository = (DatabaseOperationsControllerBean)DatabaseOperationsControllerBean.getBean(DatabaseOperationsControllerBean.class);
      this.snapshotsContainerRepository = (SnapshotsContainerRepository)databaseRepository.getRepository(RepositoryName.SnapshotsContainerRepository).getGenericRepository();
      this.sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
      this.sharePointDriveItemsGetter = new SharePointDriveItemsGetter(this.sharePointAuthorisation);
   }

   public SnapshotsContainer getLatestSnapshot() {
      return this.snapshotsContainerRepository.getLatestSnapshot();
   }

   public boolean generateSnapshot() throws WhereoilException {
      SharePointSiteListGenerator sharePointSiteListGenerator = new SharePointSiteListGenerator();
      sharePointSiteListGenerator.getAllSites(this.sharePointAuthorisation);
      List<SiteItemInternal> sitesInternalLst = sharePointSiteListGenerator.getRootSitesList();
      SnapshotStoreService snapshotStoreService = new SnapshotStoreService();
      Iterator var4 = sitesInternalLst.iterator();

      while(var4.hasNext()) {
         SiteItemInternal siteItemInternal = (SiteItemInternal)var4.next();
         Pair<String, IDriveItemDeltaCollectionPage> tokenResult = this.createDeltaToken(this.sharePointAuthorisation, siteItemInternal, false);
         if (tokenResult.getFirst() == null) {
            tokenResult = this.createDeltaToken(this.sharePointAuthorisation, siteItemInternal, true);
         }

         snapshotStoreService.createContainer();
         snapshotStoreService.addSiteItem(siteItemInternal, (String)tokenResult.getFirst());
      }

      snapshotStoreService.saveSnapshot();
      return true;
   }

   private Pair<String, IDriveItemDeltaCollectionPage> createDeltaToken(SharePointAuthorizationProcessorBean sharePointAuthorisation, SiteItemInternal sitesInternal, boolean latest) {
      String token = null;
      IDriveItemDeltaCollectionPage deltaCollPage = null;
      int count = 0;

      while(count != 5) {
         try {
            LOG.info("SiteItemInternal:" + sitesInternal);
            if (latest) {
               deltaCollPage = sharePointAuthorisation.getGraphServiceCilent().sites(sitesInternal.id).drive().root().delta("latest").buildRequest(new Option[0]).get();
            } else {
               deltaCollPage = sharePointAuthorisation.getGraphServiceCilent().sites(sitesInternal.id).drive().root().delta().buildRequest(new Option[0]).get();
            }

            String deltaLnk = deltaCollPage.deltaLink();
            if (deltaLnk != null) {
               LOG.info("Delta request created. Delta url:" + deltaLnk);
               token = deltaLnk.substring(deltaLnk.indexOf("token=", 0) + "token=".length(), deltaLnk.length());
               token = token.replaceAll("'", "").replaceAll("\\)", "");
            } else {
               LOG.info("Delta link not found analyze current changess for siteID:" + sitesInternal.id);
            }
            break;
         } catch (ClientException var10) {
            LOG.warn("Exception catch during processing siteID:" + sitesInternal.id + " continue opperation.[" + count + "/" + 5 + "] error:" + var10.getMessage());

            try {
               Thread.sleep(10000L);
            } catch (InterruptedException var9) {
               LOG.error("processDriveItems exception.", var10);
            }

            ++count;
         }
      }

      return Pair.create(token, deltaCollPage);
   }

   public void getAllChanges(SnapshotsContainer snapshotConteiner) {
      SharePointAuthorizationProcessorBean sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
      Iterator var3 = snapshotConteiner.getSiteItemSnapshotList().iterator();

      while(var3.hasNext()) {
         SiteItemSnapshot siteItemSnapshot = (SiteItemSnapshot)var3.next();
         this.getChanges(sharePointAuthorisation, siteItemSnapshot);
      }

      try {
         UserConfigurationContainer.getInstance().getContextHolder().getBufferedDataService().flush();
      } catch (WhereoilException var5) {
         LOG.error("Could not flush added domanis.", var5);
      }

      this.finalizeRemoving();
   }

   private void finalizeRemoving() {
      if (this.removedItemsLst.size() > 0) {
         LOG.info("Starting removing " + this.removedItemsLst.size() + " documents...");
         DataServiceRemote dataServiceRemoter = UserConfigurationContainer.getInstance().getContextHolder().getDataServiceRemote();
         String ontologyNamespace = UserConfigurationContainer.getInstance().getOntologyNamespace();
         Query query = null;
         int index = 1;

         for(Iterator var5 = this.removedItemsLst.iterator(); var5.hasNext(); ++index) {
            String itemID = (String)var5.next();
            if (query == null) {
               query = new Query();
            }

            query.getOr().add((new Query()).like("shpt:InternalSharepointItemID", itemID));
            if (index % 50 == 0) {
               try {
                  dataServiceRemoter.deleteMetadataEntities(ontologyNamespace, query, true);
                  query = null;
               } catch (WhereoilException var9) {
                  LOG.error("Could not delete metadomainds query:" + query, var9);
               }
            }
         }

         if (query != null) {
            try {
               dataServiceRemoter.deleteMetadataEntities(ontologyNamespace, query, true);
            } catch (WhereoilException var8) {
               LOG.error("Could not delete metadomainds query:" + query, var8);
            }
         }
      }

   }

   private void getChanges(SharePointAuthorizationProcessorBean sharePointAuthorisation, SiteItemSnapshot siteItemSnapshot) {
      IDriveItemDeltaCollectionPage deltaCollPage = null;
      int count = 0;

      while(count != 5) {
         try {
            IDriveItemDeltaCollectionRequest req = sharePointAuthorisation.getGraphServiceCilent().sites(siteItemSnapshot.getId()).drive().root().delta(siteItemSnapshot.getDeltaToken()).buildRequest(new Option[0]);
            LOG.debug("Request URL:" + req.getRequestUrl());
            deltaCollPage = req.get();
            break;
         } catch (ClientException var8) {
            LOG.warn("Exception catch during processing siteID:" + siteItemSnapshot.getId() + " continue opperation.[" + count + "/" + 5 + "] error:" + var8.getMessage());

            try {
               Thread.sleep(10000L);
            } catch (InterruptedException var7) {
               LOG.error("processDriveItems exception.", var8);
            }

            ++count;
         }
      }

      if (deltaCollPage != null) {
         this.analyzeDeltaResponse(deltaCollPage, siteItemSnapshot);
      }

   }

   private void analyzeDeltaResponse(IDriveItemDeltaCollectionPage deltaCollPage, SiteItemSnapshot siteItemSnapshot) {
      LOG.trace("deltaLnk:" + deltaCollPage.deltaLink());
      List<DriveItem> driveItemLst = deltaCollPage.getCurrentPage();
      Iterator var4 = driveItemLst.iterator();

      while(var4.hasNext()) {
         DriveItem driveItem = (DriveItem)var4.next();
         this.processDriveItem(driveItem, siteItemSnapshot);
      }

      if (deltaCollPage.getNextPage() != null) {
         int count = 0;

         while(count != 5) {
            try {
               this.analyzeDeltaResponse(((IDriveItemDeltaCollectionRequestBuilder)deltaCollPage.getNextPage()).buildRequest(new Option[0]).get(), siteItemSnapshot);
               break;
            } catch (ClientException var8) {
               LOG.warn("Exception catch during processing siteID:" + siteItemSnapshot.getId() + " continue opperation.[" + count + "/" + 5 + "] error:" + var8.getMessage());

               try {
                  Thread.sleep(10000L);
               } catch (InterruptedException var7) {
                  LOG.error("processDriveItems exception.", var8);
               }

               ++count;
            }
         }
      }

   }

   private void processDriveItem(DriveItem driveItem, SiteItemSnapshot siteItemSnapshot) {
      if (driveItem.deleted != null) {
         this.removedItemsLst.add(driveItem.id);
      } else if (driveItem.parentReference != null && driveItem.file != null) {
         String filePath = "";
         MetaDomain md = this.sharePointDriveItemsGetter.processFile(driveItem, driveItem.parentReference.driveId, filePath, siteItemSnapshot.getDisplayName(), siteItemSnapshot.getSubSiteName(), false);
         if (md != null) {
            try {
               UserConfigurationContainer.getInstance().getContextHolder().getBufferedDataService().registerEntities(new MetaDomain[]{md});
               ++this.addedDocCounter;
            } catch (WhereoilException var6) {
               LOG.error("Error saving to WRS", var6);
            }
         }
      }

   }
}
