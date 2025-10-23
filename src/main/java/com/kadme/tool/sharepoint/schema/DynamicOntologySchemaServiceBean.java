/* Decompiler 55ms, total 257ms, lines 111 */
package com.kadme.tool.sharepoint.schema;

import com.kadme.ksearch.core.MetaClass;
import com.kadme.ksearch.core.Namespace;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.schema.SchemaServiceRemote;
import com.kadme.tool.sharepoint.commands.config.ISharePointBaseBean;
import com.kadme.tool.sharepoint.util.OntologyCreateUtil;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("DynamicOntologySchemaServiceBean")
@Scope("singleton")
public class DynamicOntologySchemaServiceBean extends ISharePointBaseBean<DynamicOntologySchemaServiceBean> {
   private SchemaServiceRemote schemaServiceRemote;
   private static final Log LOG = LogFactory.getLog(DynamicOntologySchemaServiceBean.class);
   private Set<String> metaDataSet = new HashSet();
   private boolean isNamespaceExists = false;
   private String ontologyNamespace;
   private int numberOfShards = 1;
   private int numberOfReplicas = 0;

   public void init(SchemaServiceRemote schemaServiceRemote, String ontologyNamespace, int numberOfShards, int numberOfReplicas) {
      this.schemaServiceRemote = schemaServiceRemote;
      this.ontologyNamespace = ontologyNamespace;
      this.numberOfShards = numberOfShards;
      this.numberOfReplicas = numberOfReplicas;
      this.metaDataSet.clear();

      try {
         Map<String, Integer> namespaces = schemaServiceRemote.getNamespaces();
         if (namespaces.containsKey(ontologyNamespace)) {
            Namespace availableNS = schemaServiceRemote.getNamespace(ontologyNamespace);
            availableNS.getClasses().forEach((e) -> {
               this.metaDataSet.add(e.getName());
            });
            this.isNamespaceExists = true;
         }
      } catch (WhereoilException var7) {
         LOG.error(var7, var7.fillInStackTrace());
      }

   }

   public void updateOnthology(String clasName, Map<String, Class<?>> properties) {
      String metaClassName = OntologyCreateUtil.prepareMetaClassName(this.ontologyNamespace, clasName);
      Map metaProperties = OntologyCreateUtil.buildProperties(properties);

      try {
         Namespace availableNS = this.schemaServiceRemote.getNamespace(this.ontologyNamespace);
         availableNS.getClasses().forEach((item) -> {
            if (metaClassName.equals(item.getName())) {
               item.getPropertiesMap().putAll(metaProperties);
            }

         });
         this.schemaServiceRemote.createSchema(availableNS, this.numberOfShards, this.numberOfReplicas, false);
      } catch (WhereoilException var6) {
         LOG.error(var6, var6.fillInStackTrace());
      }

   }

   public String generateFileItemSchema(String siteName) {
      return this.generateSchema(siteName, "shpt:FileItem", "shpt:FI", "kmeta:File", SchemaFileItem.getMetaClassProperties());
   }

   public String generateListItemSchema(String siteName) {
      return this.generateSchema(siteName, "shpt:ListItem", "shpt:LI", "kmeta:BaseClass", SchemaListItem.getMetaClassProperties());
   }

   private String generateSchema(String siteName, String className, String shortclassName, String parentClass, Map<String, Class<?>> properties) {
      String metaClassName = OntologyCreateUtil.prepareMetaClassName(this.ontologyNamespace, siteName);
      MetaClass metaclass = OntologyCreateUtil.buildMetaClass(metaClassName, metaClassName, properties);
      if (!this.metaDataSet.contains(metaclass.getName())) {
         try {
            Namespace availableNS = null;
            if (this.isNamespaceExists) {
               availableNS = this.schemaServiceRemote.getNamespace(this.ontologyNamespace);
            }

            Namespace ns = OntologyCreateUtil.buildEmptyNamespace();
            metaclass.addParentClass(parentClass);
            Set<MetaClass> classes = new HashSet();
            if (this.isNamespaceExists) {
               classes.addAll(availableNS.getClasses());
            }

            classes.add(metaclass);
            ns.setClasses(classes);
            this.schemaServiceRemote.createSchema(ns, this.numberOfShards, this.numberOfReplicas, true);
            this.isNamespaceExists = true;
            this.metaDataSet.add(metaclass.getName());
         } catch (WhereoilException var11) {
            LOG.error(var11);
         }
      }

      return metaclass.getName();
   }

   public void finalize() {
      this.schemaServiceRemote = null;
   }
}
