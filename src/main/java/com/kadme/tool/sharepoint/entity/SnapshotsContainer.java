/* Decompiler 9ms, total 121ms, lines 71 */
package com.kadme.tool.sharepoint.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(
   name = "SnapshotsContainer"
)
public class SnapshotsContainer implements SnapshotDatabaseBase {
   @Id
   @GeneratedValue(
      strategy = GenerationType.AUTO
   )
   private Long SnapshotsContainerID;
   private Date snapshotDate;
   @Column(
      columnDefinition = "boolean default false"
   )
   private boolean isFinished;
   @OneToMany(
      fetch = FetchType.EAGER,
      mappedBy = "snapshotsContainer",
      cascade = {CascadeType.ALL},
      orphanRemoval = true
   )
   private List<SiteItemSnapshot> siteItemSnapshotList = new ArrayList();

   public Long getSnapshotsContainerID() {
      return this.SnapshotsContainerID;
   }

   public void setSnapshotsContainerID(Long snapshotsContainerID) {
      this.SnapshotsContainerID = snapshotsContainerID;
   }

   public Date getSnapshotDate() {
      return this.snapshotDate;
   }

   public void setSnapshotDate(Date snapshotDate) {
      this.snapshotDate = snapshotDate;
   }

   public List<SiteItemSnapshot> getSiteItemSnapshotList() {
      return this.siteItemSnapshotList;
   }

   public void setSiteItemSnapshotList(List<SiteItemSnapshot> siteItemSnapshotList) {
      this.siteItemSnapshotList = siteItemSnapshotList;
   }

   public boolean isFinished() {
      return this.isFinished;
   }

   public void setFinished(boolean isFinished) {
      this.isFinished = isFinished;
   }
}
