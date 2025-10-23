/* Decompiler 4ms, total 146ms, lines 13 */
package com.kadme.tool.sharepoint.database;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CrudBaseRepository<T> extends JpaRepository<T, Long> {
   public static enum RepositoryName {
      SnapshotsContainerRepository,
      SiteItemRepository;
   }
}
