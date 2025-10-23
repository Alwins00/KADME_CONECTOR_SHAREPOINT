/* Decompiler 4ms, total 198ms, lines 32 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.dataservice.query.Query;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.ToolSharePoint;
import com.kadme.tool.sharepoint.database.DatabaseOperationsControllerBean;
import com.kadme.tool.sharepoint.database.CrudBaseRepository.RepositoryName;

public class CleanShpntCommand extends SharePointBaseCommand {
   public CleanShpntCommand(ToolSharePoint tool) {
      super(tool);
   }

   public String getDescription() {
      return "Cleans all data from shpt namespace and internal snapshot database";
   }

   public String getName() {
      return "shpt_clean";
   }

   protected void execute(CommandProperties cp, Reporter reporter, ExecutionContext ec) throws Exception {
      reporter.reportInfo("Cleaning existing data for " + ((ToolSharePoint)this.getTool()).getOntologyNamespace() + " namespace");
      ((DatabaseOperationsControllerBean)DatabaseOperationsControllerBean.getBean(DatabaseOperationsControllerBean.class)).getRepository(RepositoryName.SnapshotsContainerRepository).deleteAll();
      reporter.reportInfo("Cleaning database done");
      this.getDataService().deleteMetadataEntities(((ToolSharePoint)this.getTool()).getOntologyNamespace(), new Query(), true);
      reporter.reportInfo("Cleaning done");
   }
}
