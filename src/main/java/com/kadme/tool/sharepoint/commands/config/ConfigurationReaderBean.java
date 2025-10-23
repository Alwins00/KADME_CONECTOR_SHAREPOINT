/* Decompiler 23ms, total 137ms, lines 47 */
package com.kadme.tool.sharepoint.commands.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component("ConfigurationReaderBean")
@Scope("prototype")
public class ConfigurationReaderBean extends ISharePointBaseBean<ConfigurationReaderBean> {
   @Autowired
   private Environment env;
   @Value("${sharepoint.graph.entrypoint}")
   private String graphEntryPointURL;
   @Value("${sharepoint.graph.loginroot}")
   private String graphLoginRoot;
   public static final String PROP_PHASE_STARTINGOPERATION = "sharepoint.phase.startingoperation";
   public static final String PROP_PHASE_AUTHORISATION = "sharepoint.phase.authorisation";
   public static final String PROP_PHASE_GETTINGFILELIST = "sharepoint.phase.gettingfilelist";
   public static final String PROP_INFO_OPERATIONTAKES = "sharepoint.info.opertiontake";

   public String getProperty(String propName) {
      return this.env.getProperty(propName);
   }

   public String getFromatedProperty(String propName, Object... args) {
      return String.format(this.env.getProperty(propName), args);
   }

   public String getGraphEntryPointURL() {
      return this.graphEntryPointURL;
   }

   public void setGraphEntryPointURL(String graphEntryPointURL) {
      this.graphEntryPointURL = graphEntryPointURL;
   }

   public String getGraphLoginRoot() {
      return this.graphLoginRoot;
   }

   public void setGraphLoginRoot(String graphLoginRoot) {
      this.graphLoginRoot = graphLoginRoot;
   }
}
