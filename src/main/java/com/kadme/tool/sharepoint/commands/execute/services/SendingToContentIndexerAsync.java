/* Decompiler 44ms, total 232ms, lines 91 */
package com.kadme.tool.sharepoint.commands.execute.services;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.BufferedDataService;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.webtool.content.index.ContentIndexService;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SendingToContentIndexerAsync {
   private static final Log logger = LogFactory.getLog(SendingToContentIndexerAsync.class);
   private static final String QC_FLAG_ERROR = "ERROR_DURING_CONTENT_INDEXING";

   @Async("asyncCITaskExecutor")
   public CompletableFuture<Boolean> fileProcessorAsync(String runId, SendingToContentIndexerServiceBean sendingToCIServiceBean, BufferedDataService dataServiceRemote) {
      MDC.put("runId", runId);
      ContextHolder filesContextHolder = UserConfigurationContainer.getInstance().getContextHolder();

      while(true) {
         while(filesContextHolder.getFilesListToDownload().size() <= 0) {
            try {
               dataServiceRemote.flush();
            } catch (WhereoilException var9) {
               logger.warn("ContextIndexer sending thred unexpected finished.", var9);
            }

            if (sendingToCIServiceBean.isAllFilesDownloaded()) {
               logger.info("Sending to content indexer task finished.");
               return CompletableFuture.completedFuture(Boolean.TRUE);
            }

            try {
               Thread.sleep(500L);
            } catch (InterruptedException var10) {
               logger.warn("ContextIndexer sending thread unexpected finished." + var10.getMessage());
            }
         }

         Iterator<MetaDomain> iter = (new HashSet(filesContextHolder.getFilesListToDownload())).iterator();
         if (iter.hasNext()) {
            MetaDomain metaDomainToProcess = (MetaDomain)iter.next();
            filesContextHolder.getFilesListToDownload().remove(metaDomainToProcess);
            String fileName = metaDomainToProcess.getStringProperty("shpt:LocalFilesystemPath");
            if (this.sendContent(fileName, metaDomainToProcess, sendingToCIServiceBean.getContentIndexService())) {
               try {
                  dataServiceRemote.patchEntities(new MetaDomain[]{metaDomainToProcess});
               } catch (WhereoilException var11) {
                  logger.error("Could not patch metadomain uri:" + metaDomainToProcess.getUri(), var11);
               }
            }
         }
      }
   }

   private boolean sendContent(String fileName, MetaDomain metaDomain, ContentIndexService contentIndexService) {
      boolean result = true;

      try {
         logger.trace("Sending file to CI:" + fileName);
         if (!contentIndexService.indexMetaDomain(Paths.get(fileName), metaDomain)) {
            metaDomain.setProperty("kmeta:QC", Collections.singleton("ERROR_DURING_CONTENT_INDEXING"));
         }
      } catch (Exception var14) {
         result = false;
         logger.error("Send content to WCS exception.", var14);
      } finally {
         try {
            Files.delete(Paths.get(fileName));
         } catch (IOException var13) {
            logger.info("Could not delete file " + fileName + " error:" + var13.getMessage());
         }

      }

      return result;
   }
}
