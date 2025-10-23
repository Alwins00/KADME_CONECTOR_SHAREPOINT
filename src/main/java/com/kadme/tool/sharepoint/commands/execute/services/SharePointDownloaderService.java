/* Decompiler 17ms, total 131ms, lines 124 */
package com.kadme.tool.sharepoint.commands.execute.services;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointAuthorizationProcessorBean;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.extensions.IDriveItemContentStreamRequestBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ext.com.google.common.io.Files;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SharePointDownloaderService {
   private static final Log LOG = LogFactory.getLog(SharePointDownloaderService.class);
   private final int MAX_DOWNLOADED_DOCUMENTS = 100;
   private final int MAX_WAIT_TIME_FOR_CI = 15;
   private SharePointAuthorizationProcessorBean sharePointAuthorisation;
   private SendingToContentIndexerServiceBean sendingBean;

   @Async("asyncSharepointDownloadExecutor")
   public CompletableFuture<Boolean> initDownloading() {
      if (this.sharePointAuthorisation == null) {
         this.sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
         this.sendingBean = (SendingToContentIndexerServiceBean)SendingToContentIndexerServiceBean.getBean(SendingToContentIndexerServiceBean.class);
      }

      do {
         try {
            this.downloadFile((MetaDomain)this.sendingBean.getMetaDomainsForDownloadLst().take());
            Thread.sleep(500L);
         } catch (InterruptedException var2) {
            LOG.warn("Download task interrupted.");
            break;
         }
      } while(!this.sendingBean.getMetaDomainsForDownloadLst().isEmpty() || !this.sendingBean.isAllFilesDownloaded());

      return CompletableFuture.completedFuture(Boolean.TRUE);
   }

   private void downloadFile(MetaDomain metaDomain) {
      String driveId = metaDomain.getStringProperty("shpt:InternalSharepointSiteID");
      String driveItemId = metaDomain.getStringProperty("shpt:InternalSharepointItemID");
      String filePath = metaDomain.getStringProperty("shpt:Folder");
      String fileName = metaDomain.getStringProperty("kmeta:FileName");
      IDriveItemContentStreamRequestBuilder request = this.sharePointAuthorisation.getGraphServiceCilent().drives(driveId).items(driveItemId).content();
      LOG.trace("Download file request URL:" + request.getRequestUrl());
      String tempFilePath = UserConfigurationContainer.getInstance().getContextHolder().getTempDirectoryPath().toString();

      try {
         File tempFile = new File(tempFilePath, fileName);
         (new StringBuilder()).append(tempFilePath).append(File.separator).append(filePath).toString();
         Files.createParentDirs(tempFile);
         InputStream inputFileStream = request.buildRequest(new Option[0]).get();

         try {
            FileOutputStream out = new FileOutputStream(tempFile);

            try {
               IOUtils.copyLarge(inputFileStream, out);
               metaDomain.setProperty("shpt:LocalFilesystemPath", tempFile.getAbsolutePath());
            } catch (Throwable var15) {
               try {
                  out.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }

               throw var15;
            }

            out.close();
         } catch (Throwable var16) {
            if (inputFileStream != null) {
               try {
                  inputFileStream.close();
               } catch (Throwable var13) {
                  var16.addSuppressed(var13);
               }
            }

            throw var16;
         }

         if (inputFileStream != null) {
            inputFileStream.close();
         }

         UserConfigurationContainer.getInstance().getContextHolder().getFilesListToDownload().add(metaDomain);
         StopWatch stopwatch = new StopWatch();
         stopwatch.start();

         while(true) {
            if (stopwatch.getTime(TimeUnit.MINUTES) >= 15L) {
               LOG.warn("Max time for content indexer rached (15 minutes) for document:" + fileName + " continue analyze.");
               break;
            }

            if (UserConfigurationContainer.getInstance().getContextHolder().getFilesListToDownload().size() < 100) {
               break;
            }

            Thread.sleep(1000L);
         }

         stopwatch.stop();
      } catch (IOException var17) {
         LOG.error("IOException:", var17);
      } catch (InterruptedException var18) {
         LOG.error("InterruptedException:", var18);
      }

   }
}
