/* Decompiler 25ms, total 276ms, lines 17 */
package com.kadme.tool.sharepoint.snapshot;

import com.kadme.tool.sharepoint.transfer.FileItemTO;
import java.util.List;

public class SharePointFilesSnapshotConteiner {
   private final List<FileItemTO> fileItems;

   public SharePointFilesSnapshotConteiner(List<FileItemTO> fileItems) {
      this.fileItems = fileItems;
   }

   public List<FileItemTO> getFileItems() {
      return this.fileItems;
   }
}
