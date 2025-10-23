/* Decompiler 7ms, total 134ms, lines 23 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.kadme.tool.log.Reporter;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.requests.extensions.IUserCollectionPage;
import java.util.List;

public class SharePointUserGetter {
   private final Reporter reporter;
   final SharePointAuthorizationProcessorBean sharePointAuthorisation;

   public SharePointUserGetter(Reporter reporter) {
      this.reporter = reporter;
      this.sharePointAuthorisation = (SharePointAuthorizationProcessorBean)SharePointAuthorizationProcessorBean.getBean(SharePointAuthorizationProcessorBean.class);
   }

   public List<User> getAllUsers() {
      IUserCollectionPage usersPage = this.sharePointAuthorisation.getGraphServiceCilent().users().buildRequest(new Option[0]).get();
      return usersPage.getCurrentPage();
   }
}
