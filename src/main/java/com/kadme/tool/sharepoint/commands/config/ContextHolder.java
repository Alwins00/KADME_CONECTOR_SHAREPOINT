/* Decompiler 16ms, total 123ms, lines 120 */
package com.kadme.tool.sharepoint.commands.config;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.BufferedDataService;
import com.kadme.rest.data.DataServiceRemote;
import com.kadme.rest.webtool.content.index.ContentIndexConfig;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.tool.log.Reporter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ContextHolder {
   private static final Log logger = LogFactory.getLog(ContextHolder.class);
   private final CommandProperties properties;
   private final Reporter reporter;
   private final ExecutionContext executionContext;
   private final ContentIndexConfig contentIndexServiceConfig;
   private final BufferedDataService bufferedDataService;
   private final DataServiceRemote dataServiceRemote;
   private Path tempDirectoryPath;
   private Set<MetaDomain> filesListToDownload = Collections.synchronizedSet(new HashSet());
   private Set<MetaDomain> processedMetadomain = Collections.synchronizedSet(new HashSet());
   private Map<String, String> ldapSidMap = new HashMap();

   public ContextHolder(CommandProperties properties, Reporter reporter, ExecutionContext executionContext, ContentIndexConfig contentIndexServiceConfig, BufferedDataService bufferedDataService, DataServiceRemote dataServiceRemote) {
      this.properties = properties;
      this.reporter = reporter;
      this.executionContext = executionContext;
      this.contentIndexServiceConfig = contentIndexServiceConfig;
      this.bufferedDataService = bufferedDataService;
      this.dataServiceRemote = dataServiceRemote;

      try {
         this.tempDirectoryPath = Files.createTempDirectory("sharepoint_tool");
      } catch (IOException var8) {
         logger.error(var8);
      }

   }

   public CommandProperties getProperties() {
      return this.properties;
   }

   public Reporter getReporter() {
      return this.reporter;
   }

   public ExecutionContext getExecutionContext() {
      return this.executionContext;
   }

   public ContentIndexConfig getContentIndexServiceConfig() {
      return this.contentIndexServiceConfig;
   }

   public String toString() {
      return this.reporter.toString();
   }

   public Path getTempDirectoryPath() {
      return this.tempDirectoryPath;
   }

   public Set<MetaDomain> getFilesListToDownload() {
      return this.filesListToDownload;
   }

   public void setFilesListToDownload(Set<MetaDomain> filesListToDownload) {
      this.filesListToDownload = filesListToDownload;
   }

   public synchronized Set<MetaDomain> getProcessedMetadomain() {
      return this.processedMetadomain;
   }

   public synchronized void cleanProcessedMetadomains() {
      this.getProcessedMetadomain().clear();
   }

   public Map<String, String> getLdapSidMap() {
      return this.ldapSidMap;
   }

   public void cleanup() {
      try {
         if (this.filesListToDownload.size() > 0) {
            logger.error("List of files to analyze by content indexer is not empty contains:" + this.filesListToDownload.size() + " elements.");
         }

         this.filesListToDownload.clear();
         this.processedMetadomain.clear();
         this.ldapSidMap.clear();
         FileUtils.deleteDirectory(new File(this.tempDirectoryPath.toString()));
      } catch (IOException var2) {
         logger.error(var2);
      }

   }

   public BufferedDataService getBufferedDataService() {
      return this.bufferedDataService;
   }

   public DataServiceRemote getDataServiceRemote() {
      return this.dataServiceRemote;
   }
}
