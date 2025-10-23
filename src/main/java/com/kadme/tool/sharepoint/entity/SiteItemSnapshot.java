/* Decompiler 35ms, total 155ms, lines 124 */
package com.kadme.tool.sharepoint.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(
   name = "SiteItemSnapshot",
   indexes = {@Index(
   name = "IDX_MYURI",
   columnList = "id"
)}
)
public class SiteItemSnapshot implements SnapshotDatabaseBase {
   @Id
   @GeneratedValue(
      strategy = GenerationType.AUTO
   )
   private Long siteItemSnapshotID;
   @ManyToOne
   private SnapshotsContainer snapshotsContainer;
   @Column(
      length = 256
   )
   private String id;
   @Column(
      length = 1024
   )
   private String displayName;
   private Date createdDateTime;
   private Date lastModifiedDateTime;
   @Column(
      length = 256
   )
   private String webUrl;
   @Column(
      length = 1024
   )
   private String deltaToken;
   @Column(
      length = 1024
   )
   private String subSiteName;

   public Long getSiteItemSnapshotID() {
      return this.siteItemSnapshotID;
   }

   public void setSiteItemSnapshotID(Long siteItemSnapshotID) {
      this.siteItemSnapshotID = siteItemSnapshotID;
   }

   public SnapshotsContainer getSnapshotsContainer() {
      return this.snapshotsContainer;
   }

   public void setSnapshotsContainer(SnapshotsContainer snapshotsContainer) {
      this.snapshotsContainer = snapshotsContainer;
   }

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public Date getCreatedDateTime() {
      return this.createdDateTime;
   }

   public void setCreatedDateTime(Date createdDateTime) {
      this.createdDateTime = createdDateTime;
   }

   public Date getLastModifiedDateTime() {
      return this.lastModifiedDateTime;
   }

   public void setLastModifiedDateTime(Date lastModifiedDateTime) {
      this.lastModifiedDateTime = lastModifiedDateTime;
   }

   public String getWebUrl() {
      return this.webUrl;
   }

   public void setWebUrl(String webUrl) {
      this.webUrl = webUrl;
   }

   public String getDeltaToken() {
      return this.deltaToken;
   }

   public void setDeltaToken(String deltaToken) {
      this.deltaToken = deltaToken;
   }

   public String getSubSiteName() {
      return this.subSiteName;
   }

   public void setSubSiteName(String subSiteName) {
      this.subSiteName = subSiteName;
   }
}
