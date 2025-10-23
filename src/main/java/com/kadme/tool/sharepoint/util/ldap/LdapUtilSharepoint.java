/* Decompiler 144ms, total 276ms, lines 162 */
package com.kadme.tool.sharepoint.util.ldap;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LdapUtilSharepoint {
   private static final Logger LOG = LoggerFactory.getLogger(LdapUtilSharepoint.class);

   private LdapUtilSharepoint() {
   }

   private static Hashtable<String, String> createLdapEnv(String url, String securityPrincipal, String password, boolean followReferrals, int referralLimit, boolean useSsl) {
      Hashtable<String, String> env = new Hashtable();
      env.put("java.naming.factory.initial", "com.sun.jndi.ldap.LdapCtxFactory");
      env.put("java.naming.provider.url", url);
      if (useSsl) {
         env.put("java.naming.security.protocol", "ssl");
      } else {
         env.put("java.naming.security.authentication", "simple");
      }

      env.put("java.naming.security.principal", securityPrincipal);
      env.put("java.naming.security.credentials", password);
      env.put("java.naming.referral", followReferrals ? "follow" : "ignore");
      env.put("java.naming.ldap.referral.limit", referralLimit + "");
      return env;
   }

   private static Hashtable<String, String> createLdapEnv(String url, String securityPrincipal, String password, boolean followReferrals, boolean useSsl) {
      return createLdapEnv(url, securityPrincipal, password, followReferrals, 10, useSsl);
   }

   public static LdapContext createLdapContext(String url, String securityPrincipal, String password) throws NamingException {
      return createLdapContext(url, securityPrincipal, password, true);
   }

   public static LdapContext createLdapContext(String url, String securityPrincipal, String password, boolean followReferrals) throws NamingException {
      return new InitialLdapContext(createLdapEnv(url, securityPrincipal, password, followReferrals, false), (Control[])null);
   }

   public static Map<String, String> getGroupMapOfAMAccountNames(String url, String securityPrincipal, String password, String searchBase, String objectClass) throws Exception {
      Hashtable<String, String> env = createLdapEnv(url, securityPrincipal, password, false, false);
      env.put("java.naming.ldap.attributes.binary", "objectSid");
      LdapContext ldapContext = new InitialLdapContext(env, (Control[])null);
      int pageSize = 100;
      byte[] cookie = null;
      int page = 1;
      ldapContext.setRequestControls(new Control[]{new PagedResultsControl(pageSize, true)});
      Map<String, String> groupMap = new HashMap();
      LOG.info("Starting to query for groups using pageSize " + pageSize);

      do {
         SearchControls groupSearchControls = new SearchControls();
         groupSearchControls.setSearchScope(2);
         String[] groupReturnAttributes = new String[]{"sAMAccountName", "objectSid"};
         groupSearchControls.setReturningAttributes(groupReturnAttributes);
         String groupFilter = "(objectClass=group)";
         if (StringUtils.isNotBlank(objectClass)) {
            groupFilter = "(objectClass=" + objectClass + ")";
         }

         NamingEnumeration<SearchResult> groups = ldapContext.search(searchBase, groupFilter, groupSearchControls);
         LOG.info("Crawling groups for page " + page);

         while(groups.hasMoreElements()) {
            SearchResult group = (SearchResult)groups.nextElement();
            Attributes groupAttributes = group.getAttributes();
            String groupName = groupAttributes.get("sAMAccountName").get().toString();
            if (null != groupAttributes) {
               byte[] SID = (byte[])groupAttributes.get("objectSid").get();
               String strObjectSid = decodeSID(SID);
               groupMap.put(strObjectSid, groupName);
            }
         }

         Control[] controls = ldapContext.getResponseControls();
         if (controls != null) {
            for(int i = 0; i < controls.length; ++i) {
               if (controls[i] instanceof PagedResultsResponseControl) {
                  PagedResultsResponseControl prrc = (PagedResultsResponseControl)controls[i];
                  cookie = prrc.getCookie();
               }
            }
         } else {
            LOG.info("No controls were sent from the server");
         }

         ldapContext.setRequestControls(new Control[]{new PagedResultsControl(pageSize, cookie, true)});
         ++page;
      } while(cookie != null);

      ldapContext.close();
      LOG.info("Extracted total of " + groupMap.size() + " SIDs/groups");
      return groupMap;
   }

   private static String decodeSID(byte[] sid) {
      StringBuilder strSid = new StringBuilder("S-");
      int revision = sid[0];
      strSid.append(Integer.toString(revision));
      int countSubAuths = sid[1] & 255;
      long authority = 0L;

      int offset;
      for(offset = 2; offset <= 7; ++offset) {
         authority |= (long)sid[offset] << 8 * (5 - (offset - 2));
      }

      strSid.append("-");
      strSid.append(Long.toHexString(authority));
      offset = 8;
      int size = 4;

      for(int j = 0; j < countSubAuths; ++j) {
         long subAuthority = 0L;

         for(int k = 0; k < size; ++k) {
            subAuthority |= (long)(sid[offset + k] & 255) << 8 * k;
         }

         strSid.append("-");
         strSid.append(subAuthority);
         offset += size;
      }

      return strSid.toString();
   }

   public static String findGroupBySID(DirContext ctx, String ldapSearchBase, String sid) throws NamingException {
      String searchFilter = "(&(objectClass=group)(objectSid=" + sid + "))";
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(2);
      NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
      if (results.hasMoreElements()) {
         SearchResult searchResult = (SearchResult)results.nextElement();
         if (results.hasMoreElements()) {
            System.err.println("Matched multiple groups for the group with SID: " + sid);
            return null;
         } else {
            return (String)searchResult.getAttributes().get("sAMAccountName").get();
         }
      } else {
         return null;
      }
   }
}
