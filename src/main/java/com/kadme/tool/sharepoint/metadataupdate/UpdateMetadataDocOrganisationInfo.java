/* Decompiler 33ms, total 219ms, lines 59 */
package com.kadme.tool.sharepoint.metadataupdate;

import com.kadme.ksearch.core.MetaDomain;
import com.kadme.rest.data.DataServiceRemote;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UpdateMetadataDocOrganisationInfo {
   private static final Log LOG = LogFactory.getLog(UpdateMetadataDocOrganisationInfo.class);
   private static final Pattern PTRN_PROJECT_CODE = Pattern.compile("/[\\w]+", 258);
   private static final Pattern PTRN_PHASE = Pattern.compile("Fase\\s[\\w]+|PLANEACION|Planeación", 258);
   private static final Pattern PTRN_PROCESS = Pattern.compile("(Fase\\s[\\w]+|PLANEACION|Planeación)(.+[\\w\\s.])", 258);
   final DataServiceRemote dataServiceRemote = UserConfigurationContainer.getInstance().getContextHolder().getDataServiceRemote();

   public boolean updatePWAMetadomainInformation(MetaDomain md) {
      String subSite = md.getStringProperty("shpt:SubSite");
      String resPath = md.getStringProperty("shpt:LocalFilesystemPath");
      boolean isModified = false;
      Matcher matcher;
      String process;
      if (!StringUtils.isBlank(subSite)) {
         matcher = PTRN_PROJECT_CODE.matcher(subSite);
         if (matcher.find()) {
            process = matcher.group(0);
            process = process.replace("/", "");
            md.getProperties().put("shpt:ProjectCode", process);
            isModified = true;
         }
      }

      if (!StringUtils.isBlank(resPath)) {
         matcher = PTRN_PHASE.matcher(resPath);
         if (matcher.find()) {
            process = matcher.group(0);
            md.getProperties().put("shpt:Phase", process);
            isModified = true;
         }
      }

      if (!StringUtils.isBlank(resPath)) {
         matcher = PTRN_PROCESS.matcher(resPath);
         if (matcher.find() && matcher.groupCount() > 1) {
            process = matcher.group(2);
            if (!StringUtils.isBlank(process)) {
               process = process.replace("/", "");
               md.getProperties().put("shpt:Process", process);
               isModified = true;
            }
         }
      }

      return isModified;
   }
}
