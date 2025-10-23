/* Decompiler 2ms, total 362ms, lines 17 */
package com.kadme.tool.sharepoint.command.execute.rdfizer;

import com.kadme.rest.data.BufferedDataService;

public abstract class SharepointRdfizerBase {
   protected static final String DATA_SOURCE_DOCUMENTS = "SHAREPOINT-DOCUMENTS";
   protected static final String DATA_SOURCE_LISTS = "SHAREPOINT-LIST";
   protected BufferedDataService dataServiceRemote;

   protected SharepointRdfizerBase() {
   }

   public void setDataServiceRemote(BufferedDataService dataServiceRemote) {
      this.dataServiceRemote = dataServiceRemote;
   }
}
