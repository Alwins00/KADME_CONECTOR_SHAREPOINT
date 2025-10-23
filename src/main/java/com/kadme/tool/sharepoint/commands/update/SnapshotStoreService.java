/* Decompiler 47ms, total 183ms, lines 58 */
package com.kadme.tool.sharepoint.commands.update;

import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.kadme.tool.sharepoint.database.CrudBaseRepository;
import com.kadme.tool.sharepoint.database.DatabaseOperationsControllerBean;
import com.kadme.tool.sharepoint.database.CrudBaseRepository.RepositoryName;
import com.kadme.tool.sharepoint.entity.SiteItemSnapshot;
import com.kadme.tool.sharepoint.entity.SnapshotsContainer;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SnapshotStoreService {
   private static final Log logger = LogFactory.getLog(SnapshotStoreService.class);
   private SnapshotsContainer snapshotContainerPersistent;
   private final DatabaseOperationsControllerBean databaseRepository = (DatabaseOperationsControllerBean)DatabaseOperationsControllerBean.getBean(DatabaseOperationsControllerBean.class);

   public void createContainer() {
      if (this.snapshotContainerPersistent == null) {
         this.snapshotContainerPersistent = new SnapshotsContainer();
         this.snapshotContainerPersistent.setSnapshotDate(new Date());
         this.saveSnapshot();
      }

   }

   public SnapshotsContainer getSnapshotContainerPersistent() {
      return this.snapshotContainerPersistent;
   }

   public SiteItemSnapshot createSiteItemSnapshot(SiteItemInternal siteItem, String deltaToken) {
      SiteItemSnapshot siteItemSnapshot = new SiteItemSnapshot();
      siteItemSnapshot.setCreatedDateTime(siteItem.createdDateTime);
      siteItemSnapshot.setDisplayName(siteItem.displayName);
      siteItemSnapshot.setId(siteItem.id);
      siteItemSnapshot.setLastModifiedDateTime(siteItem.lastModifiedDateTime);
      siteItemSnapshot.setSnapshotsContainer(this.snapshotContainerPersistent);
      siteItemSnapshot.setWebUrl(siteItem.webUrl);
      siteItemSnapshot.setSubSiteName(siteItem.subSiteName);
      siteItemSnapshot.setDeltaToken(deltaToken);
      return siteItemSnapshot;
   }

   public SiteItemSnapshot addSiteItem(SiteItemInternal siteItem, String deltaToken) {
      SiteItemSnapshot siteItemSnapshot = this.createSiteItemSnapshot(siteItem, deltaToken);
      this.snapshotContainerPersistent.getSiteItemSnapshotList().add(siteItemSnapshot);
      return siteItemSnapshot;
   }

   public void saveSnapshot() {
      if (this.snapshotContainerPersistent != null) {
         CrudBaseRepository<SnapshotsContainer> repository = this.databaseRepository.getRepository(RepositoryName.SnapshotsContainerRepository);
         repository.save(this.snapshotContainerPersistent);
      }

   }
}
