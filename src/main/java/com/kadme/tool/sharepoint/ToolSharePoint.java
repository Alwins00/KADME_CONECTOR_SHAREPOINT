/* Decompiler 19ms, total 126ms, lines 53 */
package com.kadme.tool.sharepoint;

import com.kadme.rest.webtool.tool.AbstractTool;
import com.kadme.rest.webtool.tool.annotation.PropertyDefinition;
import com.kadme.tool.sharepoint.commands.CleanShpntCommand;
import com.kadme.tool.sharepoint.commands.SharePointCommand;
import com.kadme.tool.sharepoint.commands.SharePointIndexCommand;
import com.kadme.tool.sharepoint.commands.SharePointUpdateCommand;
import com.kadme.tool.sharepoint.commands.ShartePointPWAOrganisationMetaDataUpdtCommand;
import com.kadme.tool.sharepoint.ontology.VocabularySharepoint.namespaces;
import org.apache.commons.lang3.StringUtils;

public class ToolSharePoint extends AbstractTool {
   private static final String SHPT_ONTOLOGY_NAMESPACE;
   @PropertyDefinition(
      propertyName = "schema.suffix",
      required = false,
      description = "Schema suffix when several namespace instances are required",
      exampleValue = "UK"
   )
   String schemaSuffix;

   public String getOntologyNamespace() {
      return SHPT_ONTOLOGY_NAMESPACE + (StringUtils.isBlank(this.schemaSuffix) ? "" : "_" + this.schemaSuffix).toLowerCase();
   }

   public String getSchemaSuffix() {
      return this.schemaSuffix;
   }

   protected void defineCommands() {
      this.addCommand(() -> {
         return new SharePointCommand(this);
      });
      this.addCommand(() -> {
         return new CleanShpntCommand(this);
      });
      this.addCommand(() -> {
         return new SharePointIndexCommand(this);
      });
      this.addCommand(() -> {
         return new SharePointUpdateCommand(this);
      });
      this.addCommand(() -> {
         return new ShartePointPWAOrganisationMetaDataUpdtCommand(this);
      });
   }

   static {
      SHPT_ONTOLOGY_NAMESPACE = namespaces.shpt.getPrefix();
   }
}
