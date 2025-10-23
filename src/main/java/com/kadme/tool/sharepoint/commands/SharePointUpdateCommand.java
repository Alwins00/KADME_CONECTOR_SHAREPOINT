/* Decompiler 18ms, total 139ms, lines 53 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.webtool.content.index.ContentIndexConfig;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.ToolSharePoint;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.SharePointCommandExecute;
import com.kadme.tool.sharepoint.commands.update.UpdateDocumentsStatusProcessor;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharePointUpdateCommand extends SharePointBaseCommand {
   private static final Log LOG = LogFactory.getLog(SharePointUpdateCommand.class);

   public SharePointUpdateCommand(ToolSharePoint tool) {
      super(tool);
   }

   public String getDescription() {
      return "Update documents status based on change delta from previous run.";
   }

   public String getName() {
      return "shpt_update";
   }

   protected void execute(CommandProperties properties, Reporter reporter, ExecutionContext executionContext) throws Exception {
      LOG.info("Starting updating projects...");
      DynamicOntologySchemaServiceBean dynamicOntology = (DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class);
      dynamicOntology.init(this.getSchemaService(), ((ToolSharePoint)this.getTool()).getOntologyNamespace(), this.numberOfShards, this.numberOfReplicas);
      UserConfigurationContainer.initialize(this.clientId, this.secret, this.tenantID, new ContextHolder(properties, reporter, executionContext, (ContentIndexConfig)null, this.getBufferedDataService(), this.getDataService()));
      UserConfigurationContainer.getInstance().setSuffix(((ToolSharePoint)this.getTool()).getSchemaSuffix());
      UserConfigurationContainer.getInstance().setOntologyNamespace(((ToolSharePoint)this.getTool()).getOntologyNamespace());
      UserConfigurationContainer.getInstance().getContextHolder().getFilesListToDownload().clear();
      UserConfigurationContainer.getInstance().setSiteListToDownload(this.siteListToDownload);
      UserConfigurationContainer.getInstance().setFileExtensions(this.fileExtensionFilter);
      UserConfigurationContainer.getInstance().setForceUpdateEnabled(this.isForceUpdateEnabled);
      UserConfigurationContainer.getInstance().setCustomFileTitleColumnName(this.customFileTitleColumnName);
      SharePointCommandExecute sharePointExecute = new SharePointCommandExecute();
      if (!sharePointExecute.executeAuthorisation()) {
         throw new WhereoilException("Couild not get authorisation token");
      } else {
         UpdateDocumentsStatusProcessor updDocProcessor = new UpdateDocumentsStatusProcessor();
         updDocProcessor.startProcessing();
      }
   }
}
