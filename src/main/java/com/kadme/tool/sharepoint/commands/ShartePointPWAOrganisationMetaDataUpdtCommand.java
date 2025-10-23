/* Decompiler 25ms, total 143ms, lines 95 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.dataservice.query.SearchRequest;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.DataServiceRemoteIterator;
import com.kadme.rest.utils.json.JsonUtil;
import com.kadme.rest.webtool.content.index.ContentIndexConfig;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.ToolSharePoint;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.metadataupdate.UpdateMetadataDocOrganisationInfo;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.kadme.tool.sharepoint.util.OntologyCreateUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ShartePointPWAOrganisationMetaDataUpdtCommand extends SharePointBaseCommand {
   private static final Log LOG = LogFactory.getLog(ShartePointPWAOrganisationMetaDataUpdtCommand.class);

   public ShartePointPWAOrganisationMetaDataUpdtCommand(ToolSharePoint tool) {
      super(tool);
   }

   public String getDescription() {
      return "Updating exising data-extraction additional metadata from PWA";
   }

   public String getName() {
      return "shpt_update_metadata_PWA";
   }

   protected void execute(CommandProperties properties, Reporter reporter, ExecutionContext executionContext) throws Exception {
      LOG.info("Starting updating projects...");
      DynamicOntologySchemaServiceBean dynamicOntology = (DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class);
      dynamicOntology.init(this.getSchemaService(), ((ToolSharePoint)this.getTool()).getOntologyNamespace(), this.numberOfShards, this.numberOfReplicas);
      UserConfigurationContainer.initialize(this.clientId, this.secret, this.tenantID, new ContextHolder(properties, reporter, executionContext, (ContentIndexConfig)null, this.getBufferedDataService(), this.getDataService()));
      Map<String, Class<?>> propToAddMap = new HashMap();
      propToAddMap.put("shpt:ProjectCode", "shpt:ProjectCode".getClass());
      propToAddMap.put("shpt:Phase", "shpt:Phase".getClass());
      propToAddMap.put("shpt:VersionHlp", "shpt:VersionHlp".getClass());
      propToAddMap.put("shpt:Process", "shpt:Process".getClass());
      dynamicOntology.updateOnthology("Documents", propToAddMap);
      UserConfigurationContainer.getInstance().setSuffix(((ToolSharePoint)this.getTool()).getSchemaSuffix());
      UserConfigurationContainer.getInstance().setOntologyNamespace(((ToolSharePoint)this.getTool()).getOntologyNamespace());
      UserConfigurationContainer.getInstance().getContextHolder().getFilesListToDownload().clear();
      UserConfigurationContainer.getInstance().setCustomFileTitleColumnName(this.customFileTitleColumnName);
      SearchRequest srDocuments = new SearchRequest();
      srDocuments.getQuery().clas(OntologyCreateUtil.prepareMetaClassName(((ToolSharePoint)this.getTool()).getOntologyNamespace(), "Documents"));
      LOG.info("Search query:" + JsonUtil.toJson(srDocuments));
      List<MetaDomain> modMetadomainLst = new ArrayList();
      DataServiceRemoteIterator it = new DataServiceRemoteIterator(this.getDataService(), srDocuments, 100, ((ToolSharePoint)this.getTool()).getOntologyNamespace());

      try {
         UpdateMetadataDocOrganisationInfo mdUpdate = new UpdateMetadataDocOrganisationInfo();
         LOG.info("Found " + it.getTotalLength() + " documents to process..");
         int count = 0;

         while(it.hasNext()) {
            MetaDomain metaDomain = it.next();
            if (mdUpdate.updatePWAMetadomainInformation(metaDomain)) {
               modMetadomainLst.add(metaDomain);
            }

            ++count;
            if (count % 100 == 0) {
               LOG.info("Getting [" + count + "/" + it.getTotalLength() + "] documents...");
            }
         }
      } catch (Throwable var13) {
         try {
            it.close();
         } catch (Throwable var12) {
            var13.addSuppressed(var12);
         }

         throw var13;
      }

      it.close();
      if (modMetadomainLst.size() > 0) {
         LOG.info("Patching " + modMetadomainLst.size() + " documents.");
         this.getBufferedDataService().patchEntities(modMetadomainLst);
         this.getBufferedDataService().flush();
      }

   }
}
