/* Decompiler 12ms, total 421ms, lines 48 */
package com.kadme.tool.sharepoint.command.execute.rdfizer.imp;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.tool.sharepoint.command.execute.rdfizer.SharepointRdfizerBase;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.microsoft.graph.models.extensions.List;

public class GeneralListRdfizer extends SharepointRdfizerBase {
   public static MetaDomain createMetaDomain(SiteItemInternal site, List sharepointList) {
      MetaDomain metaDomain = new MetaDomain();
      String metaClassName = ((DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class)).generateListItemSchema("ListItems");
      metaDomain.setType(metaClassName);
      metaDomain.setProperty("shpt:Site", site.displayName);
      if (sharepointList.createdBy != null && sharepointList.createdBy.user != null) {
         metaDomain.setProperty("kmeta:Creator", sharepointList.createdBy.user.displayName);
      } else if (sharepointList.lastModifiedBy != null && sharepointList.lastModifiedBy.user != null) {
         metaDomain.setProperty("kmeta:Creator", sharepointList.lastModifiedBy.user.displayName);
      } else {
         metaDomain.setProperty("kmeta:Creator", "not defined");
      }

      metaDomain.setProperty("shpt:contentType", sharepointList.contentTypes);
      metaDomain.setProperty("kmeta:Created", sharepointList.createdDateTime);
      metaDomain.setProperty("kmeta:LastModified", sharepointList.lastModifiedDateTime);
      if (sharepointList.lastModifiedByUser != null) {
         metaDomain.setProperty("kmeta:LastEditor", sharepointList.lastModifiedByUser.displayName);
      } else if (sharepointList.lastModifiedBy != null && sharepointList.lastModifiedBy.user != null) {
         metaDomain.setProperty("kmeta:LastEditor", sharepointList.lastModifiedBy.user.displayName);
      }

      metaDomain.setProperty("kmeta:Datasource", "SHAREPOINT-LIST");
      metaDomain.setProperty("shpt:Description", sharepointList.description);
      metaDomain.setProperty("shpt:eTag", sharepointList.eTag);
      metaDomain.setProperty("shpt:ListName", sharepointList.name);
      metaDomain.setProperty("shpt:Url", sharepointList.webUrl);
      metaDomain.setProperty("kmeta:location", sharepointList.webUrl);
      if ((sharepointList.parentReference.path == null || sharepointList.parentReference.path.isEmpty()) && site.parentReference != null) {
         metaDomain.setProperty("shpt:ParentReferencePath", site.parentReference.path);
      } else {
         metaDomain.setProperty("shpt:ParentReferencePath", sharepointList.parentReference.path);
      }

      metaDomain.setProperty("kmeta:ResourcePath", site.webUrl);
      return metaDomain;
   }
}
