/* Decompiler 3ms, total 138ms, lines 18 */
package com.kadme.tool.sharepoint.commands.execute.processors;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;

public class SharepointSimpleAuthProvider implements IAuthenticationProvider {
   private String accessToken = null;

   public SharepointSimpleAuthProvider(String accessToken) {
      this.accessToken = accessToken;
   }

   public void authenticateRequest(IHttpRequest request) {
      request.addHeader("Authorization", "Bearer " + this.accessToken);
      request.addHeader("Connection", "close");
   }
}
