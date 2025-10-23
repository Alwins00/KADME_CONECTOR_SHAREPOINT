/* Decompiler 25ms, total 148ms, lines 25 */
package com.kadme.tool.sharepoint.commands.update;

import com.kadme.tool.sharepoint.entity.SnapshotsContainer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpdateDocumentsStatusProcessor {
   private static final Log LOG = LogFactory.getLog(UpdateDocumentsStatusProcessor.class);
   private final SitesDeltaTokenService sitesDeltaTokenService = new SitesDeltaTokenService();

   public void startProcessing() throws Exception {
      SnapshotsContainer snapshotConteiner = this.sitesDeltaTokenService.getLatestSnapshot();
      if (snapshotConteiner == null) {
         LOG.info("Detect first run of update command. Only delta snapshot will be generated.");
         this.sitesDeltaTokenService.generateSnapshot();
      } else {
         LOG.info("Starting getting changes from: " + snapshotConteiner.getSnapshotDate());
         this.sitesDeltaTokenService.getAllChanges(snapshotConteiner);
         LOG.info("Generating delta changes tokens.");
         this.sitesDeltaTokenService.generateSnapshot();
      }

   }
}
