/* Decompiler 197ms, total 338ms, lines 222 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.dataservice.query.Query;
import com.kadme.dataservice.query.SearchRequest;
import com.kadme.dataservice.query.Sort;
import com.kadme.dataservice.query.Pagination.Direction;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.DataServiceRemoteIterator;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.fs.DownloadServiceRemote;
import com.kadme.rest.security.AuthenticationServiceRemote;
import com.kadme.rest.utils.json.JsonUtil;
import com.kadme.rest.webtool.content.index.ContentIndexConfig;
import com.kadme.rest.webtool.content.index.ContentIndexFactory;
import com.kadme.rest.webtool.content.index.ContentIndexService;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.rest.webtool.tool.annotation.PropertyDefinition;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.ToolSharePoint;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.SharePointCommandExecute;
import com.kadme.tool.sharepoint.commands.execute.services.SendingToContentIndexerServiceBean;
import com.kadme.tool.sharepoint.util.OntologyCreateUtil;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharePointIndexCommand extends SharePointBaseCommand {
   private static final Log LOG = LogFactory.getLog(SharePointIndexCommand.class);
   @PropertyDefinition(
      propertyName = "content.indexing.max.thread.count",
      required = true,
      description = "Maximum number of threads for content indexing service",
      defaultValue = "10"
   )
   private int contentIndexingMaxThreadsCount;
   @PropertyDefinition(
      propertyName = "index.extension.max.size",
      required = false,
      exampleValue = "jpg=512,txt=256,las=4096",
      defaultValue = "txt=256,las=4096",
      description = "Map specifying the maximum acceptable file size for each extension.",
      regexValidator = "([^=]+=\\d+)(,([^=]+=\\d+))*"
   )
   protected Map<String, String> extensionMaxSize = new LinkedHashMap();
   @PropertyDefinition(
      propertyName = "index.file_types",
      required = false,
      description = "Comma separated list of file extensions to process by WCS (content indexer) (is blank, all types supported by WCS will be processed)",
      defaultValue = ""
   )
   private List<String> processExtensions;

   public SharePointIndexCommand(ToolSharePoint tool) {
      super(tool);
   }

   public String getDescription() {
      return "Content index files command";
   }

   public String getName() {
      return "shpt_content_indexing";
   }

   protected void execute(CommandProperties properties, Reporter reporter, ExecutionContext executionContext) throws Exception {
      LOG.info("Starting sending to content to WCS...");
      DownloadServiceRemote downloadServiceRemote = new DownloadServiceRemote(((ToolSharePoint)this.getTool()).getServicesUrl(), ((ToolSharePoint)this.getTool()).getUsername(), ((ToolSharePoint)this.getTool()).getPassword());
      ContentIndexConfig var10000 = (new ContentIndexConfig()).setContentIndexingUrl(this.contentIndexingUrl).setTicketSupplier(this::getTicket).setContentIndexingMaxThreadsCount(this.contentIndexingMaxThreadsCount).setContentStreamer(this::streamContent).setDomainsConsumer(this::saveToWrs);
      Objects.requireNonNull(executionContext);
      ContentIndexConfig contentIndexConfig = var10000.setProgressTracker(executionContext::setProgress).setDownloadService(downloadServiceRemote).setContentIndexingMaxThreadsCount(4);
      UserConfigurationContainer.initialize(this.clientId, this.secret, this.tenantID, new ContextHolder(properties, reporter, executionContext, contentIndexConfig, this.getBufferedDataService(), this.getDataService()));
      UserConfigurationContainer.getInstance().setSuffix(((ToolSharePoint)this.getTool()).getSchemaSuffix());
      UserConfigurationContainer.getInstance().setOntologyNamespace(((ToolSharePoint)this.getTool()).getOntologyNamespace());
      UserConfigurationContainer.getInstance().getContextHolder().getFilesListToDownload().clear();
      SearchRequest srDocuments = new SearchRequest();
      srDocuments.getQuery().clas(OntologyCreateUtil.prepareMetaClassName(((ToolSharePoint)this.getTool()).getOntologyNamespace(), "Documents"));
      Set<String> fileExtToProcess = new HashSet();
      ContentIndexService contentService = ContentIndexFactory.createContentIndexService(contentIndexConfig);
      if (this.processExtensions != null && !this.processExtensions.isEmpty()) {
         fileExtToProcess.addAll(this.processExtensions);
      } else {
         LOG.info("index.file_types not configurred getting extensions supported by WCS");
         Map<String, String> exrtReaderVersions = contentService.getExtensionToReaderVersions();
         LOG.info("Following file extensions will be processed:" + exrtReaderVersions.keySet());
         fileExtToProcess.addAll(exrtReaderVersions.keySet());
      }

      Iterator var17 = fileExtToProcess.iterator();

      while(var17.hasNext()) {
         String fileExt = (String)var17.next();
         srDocuments.getQuery().getOr().add((new Query()).like("shpt:FileExt", fileExt));
      }

      contentService.getExtensionToReaderVersions().forEach((ext, readerVersion) -> {
         Query extensionBasedQuery = new Query();
         extensionBasedQuery.eq("kmeta:FileExt", StringUtils.lowerCase(ext));
         extensionBasedQuery.or((new Query()).not((new Query()).eq("kmeta:sysContentReaderVersion", readerVersion)));
         extensionBasedQuery.or((new Query()).not((new Query()).eq("kmeta:sysContentProcessed", true)));
         srDocuments.getQuery().getOr().add(extensionBasedQuery);
      });
      srDocuments.getQuery().getAnd().add((new Query()).gt("kmeta:Size", 0));
      srDocuments.getPagination().getSorts().add(new Sort("kmeta:LastModified", Direction.DESC));
      LOG.info("Executing search query:" + JsonUtil.toJson(srDocuments));
      SendingToContentIndexerServiceBean sendingBean = (SendingToContentIndexerServiceBean)SendingToContentIndexerServiceBean.getBean(SendingToContentIndexerServiceBean.class);
      SharePointCommandExecute sharePointExecute = new SharePointCommandExecute();
      if (!sharePointExecute.executeAuthorisation()) {
         throw new WhereoilException("Couild not get authorisation token ");
      } else {
         sendingBean.setAllFilesDownloaded(false);

         try {
            DataServiceRemoteIterator it = new DataServiceRemoteIterator(this.getDataService(), srDocuments, 100, ((ToolSharePoint)this.getTool()).getOntologyNamespace());

            try {
               sendingBean.init(this.getBufferedDataService());
               LOG.info("Found " + it.getTotalLength() + " documents to process..");

               int count;
               MetaDomain metaDomain;
               for(count = 0; it.hasNext(); sendingBean.downloadFile(metaDomain)) {
                  metaDomain = it.next();
                  ++count;
                  if (count % 100 == 0) {
                     LOG.info("Processing [" + count + "/" + it.getTotalLength() + "] documents...");
                  }
               }

               LOG.info("Processed [" + count + "/" + it.getTotalLength() + "] documents.");
               sendingBean.setAllFilesDownloaded(true);
               sendingBean.waitForFinishCISendTasks();
            } catch (Throwable var15) {
               try {
                  it.close();
               } catch (Throwable var14) {
                  var15.addSuppressed(var14);
               }

               throw var15;
            }

            it.close();
         } catch (Exception var16) {
            sendingBean.stopAllTasks();
            throw new Exception(var16);
         }
      }
   }

   private boolean streamContent(InputStream inputStream, MetaDomain domain, long contentSize) throws WhereoilException {
      try {
         return this.getDataService().saveContent(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domain.getType(), domain.getUri(), inputStream);
      } catch (WhereoilException var6) {
         if (var6.getCode().equals("AUTH_3")) {
            this.refreshTicket();
            return this.getDataService().saveContent(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domain.getType(), domain.getUri(), inputStream);
         } else {
            throw var6;
         }
      }
   }

   private void saveToWrs(Set<MetaDomain> domains) throws WhereoilException {
      try {
         this.getDataService().registerEntities(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domains, true);
      } catch (WhereoilException var3) {
         if (!var3.getCode().equals("AUTH_3")) {
            throw var3;
         }

         this.refreshTicket();
         this.getDataService().registerEntities(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domains, true);
      }

   }

   private String getTicket(boolean refresh) {
      try {
         if (refresh) {
            this.refreshTicket();
         }
      } catch (WhereoilException var3) {
         LOG.error("Error getting ticket", var3);
      }

      return this.getDataService().getAuthenticationTicket();
   }

   private void refreshTicket() throws WhereoilException {
      LOG.debug("Refreshing the ticket");
      AuthenticationServiceRemote authenticationServiceRemote = new AuthenticationServiceRemote(((ToolSharePoint)this.getTool()).getServicesUrl());

      try {
         authenticationServiceRemote.setApplicationToken(((ToolSharePoint)this.getTool()).getApplicationToken());
         String ticket = authenticationServiceRemote.login(((ToolSharePoint)this.getTool()).getUsername(), ((ToolSharePoint)this.getTool()).getPassword());
         this.getDataService().setAuthenticationTicket(ticket);
         this.getSchemaService().setAuthenticationTicket(ticket);
      } catch (Throwable var5) {
         try {
            authenticationServiceRemote.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      authenticationServiceRemote.close();
   }
}
