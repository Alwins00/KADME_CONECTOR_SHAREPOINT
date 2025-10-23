// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
package com.kadme.tool.sharepoint.database;

import com.kadme.tool.sharepoint.commands.config.ISharePointBaseBean;
import com.kadme.tool.sharepoint.database.CrudBaseRepository.RepositoryName;
import com.kadme.tool.sharepoint.entity.SnapshotDatabaseBase;
import com.kadme.tool.sharepoint.repositories.SiteItemSnapshotRepository;
import com.kadme.tool.sharepoint.repositories.SnapshotsContainerRepository;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("DatabaseOperationsControllerBean")
@Scope("singleton")
public class DatabaseOperationsControllerBean extends ISharePointBaseBean<DatabaseOperationsControllerBean> {
   @Autowired
   private SnapshotsContainerRepository snapshotsContainerRepository;
   @Autowired
   private SiteItemSnapshotRepository siteItemRepository;
   @Autowired
   DataSource dataSource;

   public DatabaseOperationsControllerBean() {
   }

   public synchronized <T extends SnapshotDatabaseBase> DatabaseOperationsController<T> getRepository(CrudBaseRepository.RepositoryName repositoryName) {
      DatabaseOperationsController<T> baseRepository = null;
      if (repositoryName == RepositoryName.SnapshotsContainerRepository) {
         baseRepository = new DatabaseOperationsController(this.snapshotsContainerRepository, this);
      } else if (repositoryName == RepositoryName.SiteItemRepository) {
         baseRepository = new DatabaseOperationsController(this.siteItemRepository, this);
      }

      return baseRepository;
   }

   public synchronized void closeConnection() {
   }
}
