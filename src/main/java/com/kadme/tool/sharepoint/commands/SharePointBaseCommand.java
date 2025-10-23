/* Decompiler 2ms, total 109ms, lines 104 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.rest.webtool.tool.AbstractCommand;
import com.kadme.rest.webtool.tool.annotation.PropertyDefinition;
import com.kadme.tool.sharepoint.ToolSharePoint;

public abstract class SharePointBaseCommand extends AbstractCommand<ToolSharePoint> {
   @PropertyDefinition(
      propertyName = "sharepoint.clientId",
      required = true,
      exampleValue = "d85131e4-1763-42d6-b9c7-b6bad64b3a51",
      defaultValue = "",
      description = "Client ID (Application ID) of the application as registered in the application registration portal (portal.azure.com)",
      encrypted = true
   )
   protected String clientId;
   @PropertyDefinition(
      propertyName = "sharepoint.secret",
      required = true,
      exampleValue = "qWgdYAmab0YSkuL1qKv5bPX",
      defaultValue = "",
      description = "The client secret created for your application (portal.azure.com)",
      encrypted = true
   )
   protected String secret;
   @PropertyDefinition(
      propertyName = "sharepoint.tenantID",
      required = true,
      exampleValue = "a8990e1f-ff32-408a-9f8e-78d3b9139b95",
      defaultValue = "",
      description = "The directory tenant that granted your application the permissions that it requested, in GUID format. (portal.azure.com)",
      encrypted = true
   )
   protected String tenantID;
   @PropertyDefinition(
      propertyName = "content.indexing.url",
      required = true,
      description = "URL to the Whereoil Content Service"
   )
   protected String contentIndexingUrl;
   @PropertyDefinition(
      propertyName = "sharepoint.site.list",
      required = false,
      description = "Comma separated list of sites URL will be processed.",
      exampleValue = "https://kadme.sharepoint.com/sites/test1",
      delimiter = ","
   )
   protected String[] siteListToDownload;
   @PropertyDefinition(
      propertyName = "sharepoint.file.extensions",
      required = true,
      description = "Comma separated list of file extension analyzed by the sharepoint tool.",
      defaultValue = "doc,docx,ppt,pptx,xls,xlsx,pdf,tif,tiff,jpg,jpeg,png",
      delimiter = ","
   )
   protected String[] fileExtensionFilter;
   @PropertyDefinition(
      propertyName = "force.update.metadomain",
      required = true,
      defaultValue = "false",
      description = "If set to true, does not matter if data exists or not will be sent to WRS",
      acceptedValues = {"true", "false"}
   )
   protected boolean isForceUpdateEnabled = false;
   @PropertyDefinition(
      propertyName = "data.number.of.shards",
      required = true,
      exampleValue = "1",
      defaultValue = "1",
      description = "Elasticsearch shard size",
      regexValidator = "\\d+"
   )
   protected int numberOfShards = 1;
   @PropertyDefinition(
      propertyName = "data.number.of.replicas",
      required = true,
      exampleValue = "0",
      defaultValue = "0",
      description = "Elasticsearch number of replicas",
      regexValidator = "\\d+"
   )
   protected int numberOfReplicas = 0;
   @PropertyDefinition(
      propertyName = "sharepoint.filetitle.custom.column.name",
      required = false,
      exampleValue = "Title",
      defaultValue = "Title",
      description = "Sharepoint custom column name will be stored in kmeta_FileTitle. If empty the fileneme without ext will be saved other value from custom column if empty will be set to null."
   )
   protected String customFileTitleColumnName;
   @PropertyDefinition(
      propertyName = "sharepoint.enable.pwa.additional.columns",
      required = true,
      defaultValue = "false",
      description = "If set to true, we are mapping title,subsite and resource path to new columns:ProjectCode,Phase,VersionHlp,Process",
      acceptedValues = {"true", "false"}
   )
   protected boolean isUpdatingColumnEnabled = false;

   public SharePointBaseCommand(ToolSharePoint tool) {
      super(tool);
   }
}
