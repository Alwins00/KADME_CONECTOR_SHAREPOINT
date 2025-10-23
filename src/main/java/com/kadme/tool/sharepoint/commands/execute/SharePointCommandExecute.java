/* Decompiler 10ms, total 180ms, lines 85 */
package com.kadme.tool.sharepoint.commands.execute;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.commands.config.ConfigurationReaderBean;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointAuthorizationProcessorBean;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointGetAllFileNamesProcessor;
import com.kadme.tool.sharepoint.database.DatabaseOperationsController;
import com.kadme.tool.sharepoint.database.DatabaseOperationsControllerBean;
import com.kadme.tool.sharepoint.database.CrudBaseRepository.RepositoryName;
import com.kadme.tool.sharepoint.entity.SnapshotsContainer;
import com.kadme.tool.sharepoint.repositories.SnapshotsContainerRepository;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SharePointCommandExecute {
   private static final Log LOG = LogFactory.getLog(SharePointCommandExecute.class);
   private String accessToken;
   private static final int NO_OF_HISTORICALSNAPSHOTS = 3;

   public boolean executeAuthorisation() throws Exception {
      boolean result = true;
      SharePointAuthorizationProcessorBean sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
      ConfigurationReaderBean confReaderBean = (ConfigurationReaderBean)ConfigurationReaderBean.getBean(ConfigurationReaderBean.class);
      UserConfigurationContainer confConteiner = UserConfigurationContainer.getInstance();
      Reporter reporter = confConteiner.getContextHolder().getReporter();
      reporter.reportInfo(confReaderBean.getProperty("sharepoint.phase.authorisation"));
      if (sharePointAuthorisation.execute()) {
         this.accessToken = sharePointAuthorisation.getAccessToken();

         try {
            DecodedJWT jwt = JWT.decode(this.accessToken);
            LOG.info("Checking access application rights based on access token...");
            if (jwt.getClaims().get("roles") == null) {
               LOG.error("Roles for Microsoft Graph is not defined please add access to AD Azure according to this description: https://docs.microsoft.com/en-us/graph/auth-register-app-v2?context=graph%2Fapi%2F1.0&view=graph-rest-1.0");
            } else {
               LOG.info("Following roles are defined for Microsft Graph (sharepoint-tool):" + ((Claim)jwt.getClaims().get("roles")).asList(String.class));
            }
         } catch (JWTDecodeException var7) {
            LOG.warn(var7);
         }
      } else {
         reporter.reportError(sharePointAuthorisation.getStatus());
         result = false;
      }

      return result;
   }

   public boolean getAllFiles() throws IOException, WhereoilException {
      SharePointGetAllFileNamesProcessor getAllFilesProc = new SharePointGetAllFileNamesProcessor();
      boolean result = getAllFilesProc.execute();
      if (result) {
         this.cleanupSnapshots();
      }

      getAllFilesProc.close();
      return result;
   }

   public String getAccessToken() {
      return this.accessToken;
   }

   private void cleanupSnapshots() {
      DatabaseOperationsController<SnapshotsContainer> dbOperations = ((DatabaseOperationsControllerBean)DatabaseOperationsControllerBean.getBean(DatabaseOperationsControllerBean.class)).getRepository(RepositoryName.SnapshotsContainerRepository);
      SnapshotsContainerRepository snapshotRepository = (SnapshotsContainerRepository)dbOperations.getGenericRepository();
      List<BigInteger> idsLst = snapshotRepository.getAllSnapshotsIDsOrderByDate();
      if (idsLst.size() > 3) {
         for(int itemNo = 3; itemNo != idsLst.size(); ++itemNo) {
            snapshotRepository.deleteById(((BigInteger)idsLst.get(itemNo)).longValue());
         }
      }

   }
}
