/* Decompiler 1ms, total 118ms, lines 25 */
package com.kadme.tool.sharepoint.repositories;

import com.kadme.tool.sharepoint.database.CrudBaseGenericRepository;
import com.kadme.tool.sharepoint.database.CrudBaseRepository;
import com.kadme.tool.sharepoint.entity.SnapshotsContainer;
import java.math.BigInteger;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SnapshotsContainerRepository extends CrudBaseGenericRepository, CrudBaseRepository<SnapshotsContainer> {
   @Query(
      nativeQuery = true,
      value = "SELECT SnapshotsContainerID FROM SnapshotsContainer ORDER BY snapshotDate DESC"
   )
   List<BigInteger> getAllSnapshotsIDsOrderByDate();

   @Query(
      nativeQuery = true,
      value = "SELECT * FROM SnapshotsContainer ORDER BY snapshotDate DESC LIMIT 1"
   )
   SnapshotsContainer getLatestSnapshot();
}
