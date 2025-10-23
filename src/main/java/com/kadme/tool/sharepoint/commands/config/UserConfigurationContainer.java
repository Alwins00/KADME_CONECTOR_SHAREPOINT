/* Decompiler 21ms, total 124ms, lines 129 */
package com.kadme.tool.sharepoint.commands.config;

import java.util.Arrays;

public class UserConfigurationContainer {
   private String clientId;
   private String secret;
   private String tenantID;
   private String[] fileExtensions;
   private String[] siteListToDownload;
   private ContextHolder contextHolder;
   private String ontologyNamespace;
   private String suffix;
   private String customFileTitleColumnName;
   private static UserConfigurationContainer configurationContainer = null;
   private boolean isForceUpdateEnabled = false;
   private boolean isUpdatingColumnEnabled = false;

   public static void initialize(String clientId, String secret, String tenantID, ContextHolder contextHolder) {
      if (configurationContainer == null) {
         configurationContainer = new UserConfigurationContainer();
      }

      configurationContainer.setClientId(clientId);
      configurationContainer.setSecret(secret);
      configurationContainer.setTenantID(tenantID);
      configurationContainer.setContextHolder(contextHolder);
   }

   public static UserConfigurationContainer getInstance() {
      return configurationContainer;
   }

   private UserConfigurationContainer() {
   }

   public String getClientId() {
      return this.clientId;
   }

   public void setClientId(String clientId) {
      this.clientId = clientId;
   }

   public String getSecret() {
      return this.secret;
   }

   public void setSecret(String secret) {
      this.secret = secret;
   }

   public String getTenantID() {
      return this.tenantID;
   }

   public void setTenantID(String tenantID) {
      this.tenantID = tenantID;
   }

   public ContextHolder getContextHolder() {
      return this.contextHolder;
   }

   public void setContextHolder(ContextHolder contextHolder) {
      this.contextHolder = contextHolder;
   }

   public String[] getFileExtensions() {
      return this.fileExtensions;
   }

   public void setFileExtensions(String[] fileExtensions) {
      this.fileExtensions = fileExtensions;
   }

   public String[] getSiteListToDownload() {
      return this.siteListToDownload;
   }

   public void setSiteListToDownload(String[] siteListToDownload) {
      this.siteListToDownload = siteListToDownload;
   }

   public String getOntologyNamespace() {
      return this.ontologyNamespace;
   }

   public void setOntologyNamespace(String namespace) {
      this.ontologyNamespace = namespace;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public void setSuffix(String suffix) {
      this.suffix = suffix;
   }

   public boolean isForceUpdateEnabled() {
      return this.isForceUpdateEnabled;
   }

   public void setForceUpdateEnabled(boolean isForceUpdateEnabled) {
      this.isForceUpdateEnabled = isForceUpdateEnabled;
   }

   public String getCustomFileTitleColumnName() {
      return this.customFileTitleColumnName;
   }

   public void setCustomFileTitleColumnName(String customFileTitleColumnName) {
      this.customFileTitleColumnName = customFileTitleColumnName;
   }

   public boolean isUpdatingColumnEnabled() {
      return this.isUpdatingColumnEnabled;
   }

   public void setUpdatingColumnEnabled(boolean isUpdatingColumnEnabled) {
      this.isUpdatingColumnEnabled = isUpdatingColumnEnabled;
   }

   public String toString() {
      return "UserConfigurationContainer [clientId=" + this.clientId + ", secret=" + this.secret + ", tenantID=" + this.tenantID + ", fileExtensions=" + Arrays.toString(this.fileExtensions) + ", siteListToDownload=" + Arrays.toString(this.siteListToDownload) + ", contextHolder=" + this.contextHolder + ", ontologyNamespace=" + this.ontologyNamespace + ", suffix=" + this.suffix + ", customFileTitleColumnName=" + this.customFileTitleColumnName + ", isForceUpdateEnabled=" + this.isForceUpdateEnabled + ", isUpdatingColumnEnabled=" + this.isUpdatingColumnEnabled + "]";
   }
}
