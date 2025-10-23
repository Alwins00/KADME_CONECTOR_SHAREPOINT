/* Decompiler 6ms, total 140ms, lines 36 */
package com.kadme.tool.sharepoint.schema;

import java.util.HashMap;
import java.util.Map;

public class SchemaListItem {
   private static final Map<String, Class<?>> properties = new HashMap();

   public static Map<String, Class<?>> getMetaClassProperties() {
      return properties;
   }

   static {
      properties.put("shpt:Site", "shpt:Site".getClass());
      properties.put("shpt:SubSite", "shpt:SubSite".getClass());
      properties.put("kmeta:Creator", "kmeta:Creator".getClass());
      properties.put("shpt:contentType", "shpt:contentType".getClass());
      properties.put("kmeta:Created", "kmeta:Created".getClass());
      properties.put("kmeta:LastModified", "kmeta:LastModified".getClass());
      properties.put("kmeta:LastEditor", "kmeta:LastEditor".getClass());
      properties.put("kmeta:Datasource", "kmeta:Datasource".getClass());
      properties.put("shpt:Description", "shpt:Description".getClass());
      properties.put("shpt:eTag", "shpt:eTag".getClass());
      properties.put("shpt:ListName", "shpt:ListName".getClass());
      properties.put("shpt:Url", "shpt:Url".getClass());
      properties.put("kmeta:location", "kmeta:location".getClass());
      properties.put("shpt:ParentReferencePath", "shpt:ParentReferencePath".getClass());
      properties.put("shpt:ParentReferencePath", "shpt:ParentReferencePath".getClass());
      properties.put("kmeta:ResourcePath", "kmeta:ResourcePath".getClass());
      properties.put("shpt:ColumnName", "shpt:ColumnName".getClass());
      properties.put("shpt:ColumnValue", "shpt:ColumnValue".getClass());
      properties.put("shpt:RowNumber", "shpt:RowNumber".getClass());
      properties.put("shpt:Folder", "shpt:Folder".getClass());
   }
}
