/* Decompiler 38ms, total 154ms, lines 228 */
package com.kadme.tool.sharepoint.commands;

import com.kadme.dataservice.query.Query;
import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.rest.security.AuthenticationServiceRemote;
import com.kadme.rest.utils.json.JsonUtil;
import com.kadme.rest.webtool.content.index.ContentIndexConfig;
import com.kadme.rest.webtool.tool.CommandProperties;
import com.kadme.rest.webtool.tool.ExecutionContext;
import com.kadme.rest.webtool.tool.annotation.PropertyDefinition;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.ToolSharePoint;
import com.kadme.tool.sharepoint.commands.config.ConfigurationReaderBean;
import com.kadme.tool.sharepoint.commands.config.ContextHolder;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.SharePointCommandExecute;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.kadme.tool.sharepoint.util.OntologyCreateUtil;
import com.kadme.tool.sharepoint.util.ldap.LdapUtilSharepoint;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ext.com.google.common.base.Stopwatch;

public class SharePointCommand extends SharePointBaseCommand {
   private static final Log LOG = LogFactory.getLog(SharePointCommand.class);
   @PropertyDefinition(
      propertyName = "ldap.url",
      required = false,
      exampleValue = "ldap://ldap.kadme.com/",
      defaultValue = "",
      description = "The url to the ldap service, currently ldap configuration is used to resolve SID to group names.",
      regexValidator = "ldap://.*"
   )
   protected String ldapUrl = "";
   @PropertyDefinition(
      propertyName = "ldap.security.principal",
      required = false,
      description = "The ldap security principal.",
      exampleValue = "uid=%s,ou=People,dc=company,dc=com"
   )
   protected String ldapSecurityPrincipal = "";
   @PropertyDefinition(
      propertyName = "ldap.password",
      required = false,
      encrypted = true,
      description = "The password to be used with the ldap security principal."
   )
   protected String ldapPassword = "";
   @PropertyDefinition(
      propertyName = "ldap.search.base",
      required = false,
      exampleValue = "ou=Support,dc=techrepublic,dc=com",
      description = "The base of the ldap search."
   )
   protected String ldapSearchBase = "";
   public static final int TIMEOUT_ATTEMPS = 5;
   public static final int TIMEOUT_WAIT_TIME = 10000;
   private static final String LDAP_USER_GROUP = "user";

   public SharePointCommand(ToolSharePoint tool) {
      super(tool);
   }

   public String getDescription() {
      return "Reads and process all document available in Microsoft SharePoint";
   }

   public String getName() {
      return "shpt_execute";
   }

   protected void execute(CommandProperties properties, Reporter reporter, ExecutionContext executionContext) throws Exception {
      Date startTime = new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(10L));
      LOG.info("Start modification time: " + startTime.toString());
      Stopwatch stopwatch = Stopwatch.createStarted();

      try {
         DynamicOntologySchemaServiceBean dynamicOntology = (DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class);
         dynamicOntology.init(this.getSchemaService(), ((ToolSharePoint)this.getTool()).getOntologyNamespace(), this.numberOfShards, this.numberOfReplicas);
         ContentIndexConfig contentIndexConfig = (new ContentIndexConfig()).setContentIndexingUrl(this.contentIndexingUrl).setTicketSupplier(this::getTicket).setContentStreamer(this::streamContent);
         UserConfigurationContainer.initialize(this.clientId, this.secret, this.tenantID, new ContextHolder(properties, reporter, executionContext, contentIndexConfig, this.getBufferedDataService(), this.getDataService()));
         UserConfigurationContainer.getInstance().setSuffix(((ToolSharePoint)this.getTool()).getSchemaSuffix());
         UserConfigurationContainer.getInstance().setOntologyNamespace(((ToolSharePoint)this.getTool()).getOntologyNamespace());
         UserConfigurationContainer.getInstance().setFileExtensions(this.fileExtensionFilter);
         UserConfigurationContainer.getInstance().setSiteListToDownload(this.siteListToDownload);
         UserConfigurationContainer.getInstance().setForceUpdateEnabled(this.isForceUpdateEnabled);
         UserConfigurationContainer.getInstance().setCustomFileTitleColumnName(this.customFileTitleColumnName);
         UserConfigurationContainer.getInstance().setUpdatingColumnEnabled(this.isUpdatingColumnEnabled);
         LOG.info("Command started at:" + (new SimpleDateFormat("MM/dd/yyyy HH:mm:ss")).format(startTime));
         this.initializeLDAPUserMap();
         SharePointCommandExecute sharePointExecute = new SharePointCommandExecute();
         sharePointExecute.executeAuthorisation();
         sharePointExecute.getAllFiles();
         stopwatch.stop();
         UserConfigurationContainer.getInstance().getContextHolder().cleanup();
         dynamicOntology.finalize();
         reporter.reportInfo(((ConfigurationReaderBean)ConfigurationReaderBean.getBean(ConfigurationReaderBean.class)).getFromatedProperty("sharepoint.info.opertiontake", new Object[]{stopwatch.elapsed(TimeUnit.SECONDS)}));
         if (this.isForceUpdateEnabled) {
            this.cleanUpOldDocuments(startTime);
            this.cleanUpOldListsItems(startTime);
         }

      } catch (Exception var12) {
         throw new Exception(var12);
      } finally {
         ;
      }
   }

   private void cleanUpOldDocuments(Date startTime) {
      LOG.info("Starting clean up deleted documents...");
      Query query = new Query();
      query.setClassname(OntologyCreateUtil.prepareMetaClassName(((ToolSharePoint)this.getTool()).getOntologyNamespace(), "Documents"));
      query.lt("kmeta:updateTime", startTime);
      LOG.info("Executing delete query:" + JsonUtil.toJson(query));

      try {
         this.getDataService().deleteMetadataEntities(((ToolSharePoint)this.getTool()).getOntologyNamespace(), query, true);
      } catch (WhereoilException var4) {
         LOG.error("WhereoilException catch", var4);
      }

      LOG.info("Cleanup finished..");
   }

   private void cleanUpOldListsItems(Date startTime) {
      LOG.info("Starting clean up deleted list items...");
      Query query = new Query();
      query.setClassname(OntologyCreateUtil.prepareMetaClassName(((ToolSharePoint)this.getTool()).getOntologyNamespace(), "ListItems"));
      query.lt("kmeta:updateTime", startTime);
      LOG.info("Executing delete query:" + JsonUtil.toJson(query));

      try {
         this.getDataService().deleteMetadataEntities(((ToolSharePoint)this.getTool()).getOntologyNamespace(), query, true);
      } catch (WhereoilException var4) {
         LOG.error("WhereoilException catch", var4);
      }

      LOG.info("Cleanup finished..");
   }

   private boolean streamContent(InputStream inputStream, MetaDomain domain, long contentSize) throws WhereoilException {
      try {
         return this.getDataService().saveContent(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domain.getType(), domain.getUri(), inputStream);
      } catch (WhereoilException var6) {
         if (var6.getCode().equals("AUTH_3")) {
            this.refreshTicket();
            return this.getDataService().saveContent(((ToolSharePoint)this.getTool()).getOntologyNamespace(), domain.getType(), domain.getUri(), inputStream);
         } else {
            throw var6;
         }
      }
   }

   private void initializeLDAPUserMap() throws Exception {
      this.checkConfiguration();
      if (StringUtils.isNotBlank(this.ldapUrl)) {
         try {
            LOG.info("Getting LDAP users database ...");
            UserConfigurationContainer.getInstance().getContextHolder().getLdapSidMap().clear();
            UserConfigurationContainer.getInstance().getContextHolder().getLdapSidMap().putAll(LdapUtilSharepoint.getGroupMapOfAMAccountNames(this.ldapUrl, this.ldapSecurityPrincipal, this.ldapPassword, this.ldapSearchBase, "user"));
            LOG.info("Received " + UserConfigurationContainer.getInstance().getContextHolder().getLdapSidMap().size() + " users for mapping");
         } catch (Exception var2) {
            LOG.error("Unable to retrieve the ldap sid map.", var2);
            throw var2;
         }
      }

   }

   private void checkConfiguration() {
      if (StringUtils.isNotBlank(this.ldapUrl)) {
         if (StringUtils.isBlank(this.ldapSecurityPrincipal)) {
            throw new NullPointerException("Ldap url has been defined but configuration is missing the ldap security principal.");
         }

         if (StringUtils.isBlank(this.ldapPassword)) {
            throw new NullPointerException("Ldap url has been defined but configuration is missing the ldap password.");
         }

         if (StringUtils.isBlank(this.ldapSearchBase)) {
            throw new NullPointerException("Ldap url has been defined but configuration is missing the ldap search base.");
         }
      }

   }

   private String getTicket(boolean refresh) {
      try {
         if (refresh) {
            this.refreshTicket();
         }
      } catch (WhereoilException var3) {
         LOG.error("Error getting ticket", var3);
      }

      return this.getDataService().getAuthenticationTicket();
   }

   private void refreshTicket() throws WhereoilException {
      LOG.debug("Refreshing the ticket");
      AuthenticationServiceRemote authenticationServiceRemote = new AuthenticationServiceRemote(((ToolSharePoint)this.getTool()).getServicesUrl());

      try {
         authenticationServiceRemote.setApplicationToken(((ToolSharePoint)this.getTool()).getApplicationToken());
         String ticket = authenticationServiceRemote.login(((ToolSharePoint)this.getTool()).getUsername(), ((ToolSharePoint)this.getTool()).getPassword());
         this.getDataService().setAuthenticationTicket(ticket);
         this.getSchemaService().setAuthenticationTicket(ticket);
      } catch (Throwable var5) {
         try {
            authenticationServiceRemote.close();
         } catch (Throwable var4) {
            var5.addSuppressed(var4);
         }

         throw var5;
      }

      authenticationServiceRemote.close();
   }
}
