/* Decompiler 11ms, total 139ms, lines 53 */
package com.kadme.tool.sharepoint.util.ldap;

import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class SharePointToLdapUserMaping {
   private static final int ACCEPTABLE_RATIO = 80;

   public static Set<String> mapLDAPPermissionsUsers(Set<String> shptUserList) {
      Map<String, String> ldapUserMap = UserConfigurationContainer.getInstance().getContextHolder().getLdapSidMap();
      Set<String> resUserSet = new HashSet();
      if (!ldapUserMap.isEmpty()) {
         Iterator var3 = shptUserList.iterator();

         while(var3.hasNext()) {
            String shptUser = (String)var3.next();
            String bestMatchName = findBestUserMatch(ldapUserMap.values(), shptUser);
            if (bestMatchName != null) {
               resUserSet.add(bestMatchName);
            }
         }
      }

      return (Set)(!resUserSet.isEmpty() ? resUserSet : shptUserList);
   }

   private static String findBestUserMatch(Collection<String> ldapUsers, String shptUser) {
      int ratio = 0;
      String bestMatchName = null;
      Iterator var4 = ldapUsers.iterator();

      while(var4.hasNext()) {
         String ldpUser = (String)var4.next();
         int tmpRatio = FuzzySearch.tokenSortRatio(ldpUser, shptUser);
         if (tmpRatio > ratio) {
            ratio = tmpRatio;
            bestMatchName = ldpUser;
         }
      }

      if (ratio >= 80) {
         return bestMatchName;
      } else {
         return null;
      }
   }
}
