/* Decompiler 4ms, total 160ms, lines 50 */
package com.kadme.tool.sharepoint.repositories;

import com.kadme.tool.sharepoint.database.CrudBaseGenericRepository;
import com.kadme.tool.sharepoint.database.CrudBaseRepository;
import com.kadme.tool.sharepoint.entity.SiteItemSnapshot;
import com.kadme.tool.sharepoint.transfer.FileItemTO;
import java.util.Date;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteItemSnapshotRepository extends CrudBaseGenericRepository, CrudBaseRepository<SiteItemSnapshot> {
   @Deprecated
   @Query(
      nativeQuery = true,
      value = "SELECT fi.* FROM FileItem fi\n INNER JOIN FileItemsContainer ficLatest ON  ficLatest.fileItemsConteinerID  =  fi.fileItemsConteiner_fileItemsConteinerID\n INNER JOIN SnapshotsContainer scLatest ON  scLatest.SnapshotsContainerID = ficLatest.snapshotsContainer_SnapshotsContainerID\n INNER JOIN FileItem fiPrev ON fi.id = fiPrev.id AND (fi.lastModifiedDateTime  <> fiPrev.lastModifiedDateTime OR fi.size <> fiPrev.size) \n INNER JOIN FileItemsContainer ficPrev ON  fiPrev.fileItemsConteiner_fileItemsConteinerID = ficPrev.fileItemsConteinerID\n INNER JOIN SnapshotsContainer scPrev ON  scPrev.SnapshotsContainerID = ficPrev.snapshotsContainer_SnapshotsContainerID\n WHERE \tscLatest.SnapshotsContainerID = (SELECT scLatest.SnapshotsContainerID FROM SnapshotsContainer scLatest ORDER BY scLatest.snapshotDate DESC LIMIT 1) AND\n\tscPrev.SnapshotsContainerID = (SELECT scPrev1.SnapshotsContainerID FROM SnapshotsContainer scPrev1 ORDER BY scPrev1.snapshotDate DESC LIMIT 1,1) GROUP BY fi.fileItemId"
   )
   List<FileItemTO> getListOfModifiedFileItems();

   @Deprecated
   @Query(
      nativeQuery = true,
      value = "SELECT COUNT(*) FROM FileItem fi WHERE fi.isProcessed = 0 AND fi.id = :id AND fi.driveId = :siteid"
   )
   Integer isFileProcessed(@Param("id") String id, @Param("siteid") String siteID);

   @Deprecated
   @Query(
      nativeQuery = true,
      value = "SELECT COUNT(*) FROM FileItem fiLatest\n INNER JOIN FileItemsContainer ficLatest ON  ficLatest.fileItemsConteinerID  =  fiLatest.fileItemsConteiner_fileItemsConteinerID\n INNER JOIN SnapshotsContainer scLatest ON  scLatest.SnapshotsContainerID = ficLatest.snapshotsContainer_SnapshotsContainerID\n WHERE scLatest.SnapshotsContainerID = (SELECT scLatest1.SnapshotsContainerID FROM SnapshotsContainer scLatest1 ORDER BY scLatest1.snapshotDate DESC LIMIT 1,1) AND\n fiLatest.id = :id AND fiLatest.parentReferenceDriveId = :parentReferenceDriveId AND fiLatest.lastModifiedDateTime <> :lastModifiedDateTime"
   )
   Integer noOfFileItemModified(@Param("id") String id, @Param("parentReferenceDriveId") String parentReferenceDriveId, @Param("lastModifiedDateTime") Date lastModifiedDateTime);

   @Deprecated
   @Query(
      nativeQuery = true,
      value = "SELECT COUNT(*) FROM FileItem fiLatest\n INNER JOIN FileItemsContainer ficLatest ON  ficLatest.fileItemsConteinerID  =  fiLatest.fileItemsConteiner_fileItemsConteinerID\n INNER JOIN SnapshotsContainer scLatest ON  scLatest.SnapshotsContainerID = ficLatest.snapshotsContainer_SnapshotsContainerID\n WHERE scLatest.SnapshotsContainerID = (SELECT scLatest1.SnapshotsContainerID FROM SnapshotsContainer scLatest1 ORDER BY scLatest1.snapshotDate DESC LIMIT 1,1) AND\n fiLatest.id = :id AND fiLatest.parentReferenceDriveId = :parentReferenceDriveId AND fiLatest.isProcessed = true"
   )
   Integer noOfFileItemNew(@Param("id") String id, @Param("parentReferenceDriveId") String parentReferenceDriveId);

   @Deprecated
   @Query(
      value = "SELECT fi.* FROM FileItem fi WHERE fi.id = :id AND fi.driveId = :siteid ORDER BY fi.fileItemId DESC LIMIT 1",
      nativeQuery = true
   )
   FileItemTO getLatestFileItem(@Param("id") String id, @Param("siteid") String siteID);
}
