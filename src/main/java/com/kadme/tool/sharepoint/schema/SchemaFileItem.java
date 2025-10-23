/* Decompiler 9ms, total 137ms, lines 48 */
package com.kadme.tool.sharepoint.schema;

import java.util.HashMap;
import java.util.Map;

public class SchemaFileItem {
   private static final Map<String, Class<?>> properties = new HashMap();

   public static Map<String, Class<?>> getMetaClassProperties() {
      return properties;
   }

   static {
      properties.put("shpt:Site", "shpt:Site".getClass());
      properties.put("kmeta:Creator", "kmeta:Creator".getClass());
      properties.put("shpt:CreatorEmail", "shpt:CreatorEmail".getClass());
      properties.put("kmeta:Created", "kmeta:Created".getClass());
      properties.put("shpt:FileSystemInfoCreatedDateTime", "shpt:FileSystemInfoCreatedDateTime".getClass());
      properties.put("shpt:FileSystemlastModifiedDateTime", "shpt:FileSystemlastModifiedDateTime".getClass());
      properties.put("kmeta:LastModified", "kmeta:LastModified".getClass());
      properties.put("kmeta:FileLastModified", "kmeta:FileLastModified".getClass());
      properties.put("shpt:ModifedEmail", "shpt:ModifedEmail".getClass());
      properties.put("kmeta:LastEditor", "kmeta:LastEditor".getClass());
      properties.put("shpt:LocalFilesystemPath", "shpt:LocalFilesystemPath".getClass());
      properties.put("shpt:InternalSharepointItemID", "shpt:InternalSharepointItemID".getClass());
      properties.put("shpt:InternalSharepointSiteID", "shpt:InternalSharepointSiteID".getClass());
      properties.put("shpt:MimeType", "shpt:MimeType".getClass());
      properties.put("shpt:ParentReferencePath", "shpt:ParentReferencePath".getClass());
      properties.put("kmeta:ResourcePath", "kmeta:ResourcePath".getClass());
      properties.put("kmeta:Datasource", "kmeta:Datasource".getClass());
      properties.put("kmeta:FileName", "kmeta:FileName".getClass());
      properties.put("kmeta:FileTitle", "kmeta:FileTitle".getClass());
      properties.put("kmeta:Size", "kmeta:Size".getClass());
      properties.put("shpt:FileExt", "shpt:FileExt".getClass());
      properties.put("kmeta:Ent", "kmeta:Ent".getClass());
      properties.put("shpt:Folder", "shpt:Folder".getClass());
      properties.put("shpt:SubSite", "shpt:SubSite".getClass());
      properties.put("kmeta:Format", "kmeta:Format".getClass());
      properties.put("kmeta:sysContentProcessed", "kmeta:sysContentProcessed".getClass());
      properties.put("kmeta:sysContentReaderVersion", "kmeta:sysContentReaderVersion".getClass());
      properties.put("kmeta:QC", "kmeta:QC".getClass());
      properties.put("shpt:ProjectCode", "shpt:ProjectCode".getClass());
      properties.put("shpt:Phase", "shpt:Phase".getClass());
      properties.put("shpt:VersionHlp", "shpt:VersionHlp".getClass());
      properties.put("shpt:Process", "shpt:Process".getClass());
   }
}
