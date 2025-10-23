/* Decompiler 42ms, total 159ms, lines 114 */
package com.kadme.tool.sharepoint.commands.execute.services;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.BufferedDataService;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.webtool.content.index.ContentIndexException;
import com.kadme.rest.webtool.content.index.ContentIndexFactory;
import com.kadme.rest.webtool.content.index.ContentIndexService;
import com.kadme.tool.sharepoint.ContentIndexerAsyncTasksConfiguration;
import com.kadme.tool.sharepoint.DownloadAsyncTasksConfiguration;
import com.kadme.tool.sharepoint.commands.config.ISharePointBaseBean;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("SendingToContentIndexerServiceBean")
@Scope("singleton")
public class SendingToContentIndexerServiceBean extends ISharePointBaseBean<SendingToContentIndexerServiceBean> {
   private static final Log logger = LogFactory.getLog(SendingToContentIndexerServiceBean.class);
   private boolean isAllFilesDownloaded = false;
   private ContentIndexService contentIndexService;
   @Autowired
   private SendingToContentIndexerAsync sendingToContentIndexer;
   @Autowired
   private ContentIndexerAsyncTasksConfiguration connectorAsyncConfiguration;
   @Autowired
   private DownloadAsyncTasksConfiguration downloadAsyncTasksConfiguration;
   private List<CompletableFuture<Boolean>> sendingCIFutures = new ArrayList();
   private BlockingQueue<MetaDomain> metaDomainsForDownloadLst = new LinkedBlockingDeque(3);
   @Autowired
   private SharePointDownloaderService downloadService;

   protected SendingToContentIndexerServiceBean() {
   }

   public void init(BufferedDataService dataServiceRemote) throws WhereoilException {
      String runId = MDC.get("runId");

      try {
         this.contentIndexService = ContentIndexFactory.createContentIndexService(UserConfigurationContainer.getInstance().getContextHolder().getContentIndexServiceConfig());
         if (!this.sendingCIFutures.isEmpty()) {
            this.stopAllTasks();
         }

         this.sendingCIFutures.clear();
         this.metaDomainsForDownloadLst.clear();
         this.isAllFilesDownloaded = false;

         int taskNo;
         for(taskNo = 0; taskNo != 3; ++taskNo) {
            this.downloadService.initDownloading();
         }

         for(taskNo = 0; taskNo != 1; ++taskNo) {
            this.sendingCIFutures.add(this.sendingToContentIndexer.fileProcessorAsync(runId, this, dataServiceRemote));
         }
      } catch (ContentIndexException var4) {
         logger.error(var4, var4);
      }

   }

   public void downloadFile(MetaDomain metaDomain) {
      try {
         this.metaDomainsForDownloadLst.put(metaDomain);
      } catch (InterruptedException var3) {
         logger.warn("Download task interrupted:" + var3.getMessage());
      }

   }

   public boolean isAllFilesDownloaded() {
      return this.isAllFilesDownloaded;
   }

   public void setAllFilesDownloaded(boolean isAllFilesDownloaded) {
      this.isAllFilesDownloaded = isAllFilesDownloaded;
   }

   public BlockingQueue<MetaDomain> getMetaDomainsForDownloadLst() {
      return this.metaDomainsForDownloadLst;
   }

   public ContentIndexService getContentIndexService() {
      return this.contentIndexService;
   }

   public void waitForFinishCISendTasks() {
      CompletableFuture.allOf((CompletableFuture[])this.sendingCIFutures.toArray(new CompletableFuture[this.sendingCIFutures.size()])).join();
      this.sendingCIFutures.clear();
   }

   public void stopAllTasks() {
      try {
         this.connectorAsyncConfiguration.getExecutor().awaitTermination(1L, TimeUnit.SECONDS);
         this.connectorAsyncConfiguration.getExecutor().purge();
         this.downloadAsyncTasksConfiguration.getExecutor().awaitTermination(1L, TimeUnit.SECONDS);
         this.downloadAsyncTasksConfiguration.getExecutor().purge();
      } catch (InterruptedException var2) {
      }

   }
}
