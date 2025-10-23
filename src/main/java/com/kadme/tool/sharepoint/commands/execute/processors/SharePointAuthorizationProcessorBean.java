/* Decompiler 22ms, total 143ms, lines 110 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.tool.sharepoint.commands.config.ConfigurationReaderBean;
import com.kadme.tool.sharepoint.commands.config.ISharePointBaseBean;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCredential;
import com.microsoft.aad.msal4j.ConfidentialClientApplication.Builder;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component("SharePointAuthorizationProcessorBean")
@Scope("singleton")
public class SharePointAuthorizationProcessorBean extends ISharePointBaseBean<SharePointAuthorizationProcessorBean> implements ISharePointProcessorBase {
   private static final Log logger = LogFactory.getLog(SharePointAuthorizationProcessorBean.class);
   private String accessToken = null;
   private String status;
   private Date expiresTokenDate;
   private final int tokenTimeTolerance = 5;
   private IGraphServiceClient graphServiceClient = null;

   public boolean execute() {
      return this.generateToken();
   }

   public synchronized IGraphServiceClient getGraphServiceCilent() {
      if (!this.isTokenValid()) {
         this.generateToken();
      }

      if (this.graphServiceClient == null) {
         logger.info("Microsoft OAuth2 access token:" + this.getAccessToken());
         SharepointSimpleAuthProvider authProvider = new SharepointSimpleAuthProvider(this.getAccessToken());
         this.graphServiceClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
      }

      return this.graphServiceClient;
   }

   public boolean isTokenValid() {
      boolean isExpired = true;
      Calendar cal = Calendar.getInstance();
      cal.add(12, 5);
      if (this.accessToken != null && this.expiresTokenDate != null && cal.getTime().after(this.expiresTokenDate)) {
         isExpired = false;
         logger.info("Access token has expired. Generate new one.");
      }

      return isExpired;
   }

   private synchronized boolean generateToken() {
      boolean result = true;
      this.status = "";
      ConfigurationReaderBean configurationReaderBean = (ConfigurationReaderBean)ConfigurationReaderBean.getBean(ConfigurationReaderBean.class);
      UserConfigurationContainer userConfigurationContainer = UserConfigurationContainer.getInstance();
      IClientCredential clientSecret = ClientCredentialFactory.createFromSecret(userConfigurationContainer.getSecret());
      String baseURL = configurationReaderBean.getGraphLoginRoot() + "/" + userConfigurationContainer.getTenantID();
      ConfidentialClientApplication app = null;
      IAuthenticationResult authResult = null;

      try {
         app = ((Builder)ConfidentialClientApplication.builder(userConfigurationContainer.getClientId(), clientSecret).authority(baseURL)).build();
         authResult = (IAuthenticationResult)app.acquireToken(ClientCredentialParameters.builder(new HashSet(Arrays.asList(configurationReaderBean.getGraphEntryPointURL()))).build()).exceptionally((ex) -> {
            this.status = "Unable to authenticate to SharePoint- " + ex.getMessage();
            return null;
         }).join();
      } catch (MalformedURLException var9) {
         this.status = "Malformed URL has occurred. Either nolegal protocol could be found in a specification string or thestring could not be parsed.";
         this.status = this.status + " Base url:" + configurationReaderBean.getGraphLoginRoot();
      }

      if (!this.status.isEmpty()) {
         result = false;
         logger.error("Could not generate tokken error message: " + this.status);
      } else {
         this.graphServiceClient = null;
         this.expiresTokenDate = authResult.expiresOnDate();
         this.accessToken = authResult.accessToken();
      }

      return result;
   }

   public String getStatus() {
      return this.status;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   public void close() throws IOException {
      this.graphServiceClient = null;
   }
}
