/* Decompiler 44ms, total 181ms, lines 98 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.rest.exception.WhereoilException;
import com.kadme.tool.log.Reporter;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.tool.sharepoint.commands.execute.processors.SharePointSiteListGenerator.SiteItemInternal;
import com.kadme.tool.sharepoint.schema.DynamicOntologySchemaServiceBean;
import com.microsoft.graph.models.extensions.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.MDC;

public class SharePointGetAllFileNamesProcessor implements ISharePointProcessorBase {
   private static final Log LOG = LogFactory.getLog(SharePointGetAllFileNamesProcessor.class);

   public void close() throws IOException {
   }

   public boolean execute() throws WhereoilException {
      boolean result = true;
      Reporter reporter = UserConfigurationContainer.getInstance().getContextHolder().getReporter();
      SharePointAuthorizationProcessorBean sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
      SharePointSiteGetter siteGetter = (SharePointSiteGetter)SharePointSiteGetter.getBean(SharePointSiteGetter.class);
      siteGetter.init();
      SharePointListGetter listGetter = new SharePointListGetter(reporter);
      SharePointUserGetter userGetter = new SharePointUserGetter(reporter);
      List<User> sharePointUsersLst = userGetter.getAllUsers();
      SharePointSiteListGenerator sharePointSiteListGenerator = new SharePointSiteListGenerator();
      sharePointSiteListGenerator.getAllSites(sharePointAuthorisation);
      List<SiteItemInternal> sitesInternalLst = sharePointSiteListGenerator.getRootSitesList();
      ((DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class)).generateFileItemSchema("Documents");
      ((DynamicOntologySchemaServiceBean)DynamicOntologySchemaServiceBean.getBean(DynamicOntologySchemaServiceBean.class)).generateListItemSchema("ListItems");
      int sitesNumber = sitesInternalLst.size();
      int counter = 1;
      List<CompletableFuture<Boolean>> futures = new ArrayList();
      String runId = MDC.get("runId");

      Iterator var14;
      SiteItemInternal siteItem;
      for(var14 = sitesInternalLst.iterator(); var14.hasNext(); ++counter) {
         siteItem = (SiteItemInternal)var14.next();
         reporter.reportInfo("Getting sites, processing... [" + counter + " of " + sitesNumber + "]");

         try {
            if (futures.size() >= 5) {
               while(true) {
                  boolean isTaskFinished = false;
                  Iterator iter = futures.iterator();

                  while(iter.hasNext()) {
                     CompletableFuture<Boolean> task = (CompletableFuture)iter.next();
                     if (task.isDone()) {
                        iter.remove();
                        isTaskFinished = true;
                     }
                  }

                  if (isTaskFinished) {
                     break;
                  }

                  Thread.sleep(30000L);
               }
            }

            futures.add(siteGetter.processFileItems(siteItem, runId));
         } catch (Exception var20) {
            LOG.warn("Exception occured during getting site documents content: " + siteItem.displayName, var20);
         }
      }

      counter = 1;

      for(var14 = sitesInternalLst.iterator(); var14.hasNext(); ++counter) {
         siteItem = (SiteItemInternal)var14.next();
         reporter.reportInfo("Getting lists, processing... [" + counter + " of " + sitesNumber + "]");

         try {
            listGetter.processList(siteItem, sharePointUsersLst);
         } catch (Exception var19) {
            LOG.warn("Exception occured during getting lists: " + siteItem.displayName, var19);
         }
      }

      CompletableFuture.allOf((CompletableFuture[])futures.toArray(new CompletableFuture[futures.size()])).join();
      return result;
   }

   public String getStatus() {
      return null;
   }
}
