/* Decompiler 74ms, total 216ms, lines 139 */
package com.kadme.tool.sharepoint.util;

import com.kadme.ksearch.core.MetaClass;
import com.kadme.ksearch.core.MetaProperty;
import com.kadme.ksearch.core.Namespace;
import com.kadme.ksearch.kadme.schema.VocabularyKmeta.datatypes;
import com.kadme.ksearch.schema.vocabulary.DataType;
import com.kadme.tool.sharepoint.commands.config.UserConfigurationContainer;
import com.kadme.util.AccentUtil;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

public class OntologyCreateUtil {
   public static Namespace buildEmptyNamespace() {
      Namespace ns = new Namespace();
      ns.setName(UserConfigurationContainer.getInstance().getOntologyNamespace());
      ns.setLabel("en", "SharePoint/O365" + (StringUtils.isBlank(UserConfigurationContainer.getInstance().getSuffix()) ? "" : " " + UserConfigurationContainer.getInstance().getSuffix()));
      return ns;
   }

   public static MetaClass buildMetaClass(String metaClassNameShort, String metaClassName, Map<String, Class<?>> properties) {
      MetaClass mc = new MetaClass(metaClassNameShort);
      mc.setLabel("en", generateLabel(metaClassName));
      mc.setProperties(buildProperties(properties));
      return mc;
   }

   public static Map<String, MetaProperty> buildProperties(Map<String, Class<?>> properties) {
      Map<String, MetaProperty> metaProperties = new HashMap();
      properties.forEach((k, v) -> {
         DataType detectDatatype = detectDatatype(v);
         MetaProperty mp = new MetaProperty(k, detectDatatype);
         mp.setLabel("en", generateLabel(k));
         metaProperties.put(mp.getName(), mp);
      });
      return metaProperties;
   }

   private static String generateLabel(String name) {
      String label = name.contains(":") ? StringUtils.substringAfter(name, ":") : name;
      StringBuilder res = new StringBuilder();

      for(int i = 0; i < label.length(); ++i) {
         char ci = label.charAt(i);
         if (i == 0) {
            res.append(Character.toString(ci).toUpperCase());
         } else if (ci == '_') {
            res.append(" ");
         } else {
            res.append(ci);
            if (i + 1 < label.length()) {
               char ci1 = label.charAt(i + 1);
               if (ci >= 'a' && ci <= 'z' && ci1 >= 'A' && ci1 <= 'Z') {
                  res.append(" ");
               }
            }
         }
      }

      return res.toString();
   }

   public static String generatePropertyName(String name) {
      return UserConfigurationContainer.getInstance().getOntologyNamespace() + ":" + name;
   }

   public static String generatePropertyName(String name, DataType type) {
      return UserConfigurationContainer.getInstance().getOntologyNamespace() + ":" + name;
   }

   public static DataType detectDatatype(Class<?> obj) {
      if (obj.equals(String.class)) {
         return datatypes.xsd_string;
      } else if (obj.equals(Integer.class)) {
         return datatypes.xsd_int;
      } else if (obj.equals(Long.class)) {
         return datatypes.xsd_long;
      } else if (obj.equals(Float.class)) {
         return datatypes.xsd_float;
      } else if (obj.equals(Double.class)) {
         return datatypes.xsd_double;
      } else if (obj.equals(Boolean.class)) {
         return datatypes.xsd_boolean;
      } else {
         return obj.equals(Date.class) ? datatypes.xsd_date : datatypes.xsd_string;
      }
   }

   public static String getSuffix(DataType obj) {
      if (obj.equals(datatypes.xsd_string)) {
         return "STR";
      } else if (obj.equals(datatypes.xsd_int)) {
         return "INT";
      } else if (obj.equals(datatypes.xsd_long)) {
         return "LON";
      } else if (obj.equals(datatypes.xsd_float)) {
         return "FLT";
      } else if (obj.equals(datatypes.xsd_double)) {
         return "DBL";
      } else if (obj.equals(datatypes.xsd_boolean)) {
         return "BOL";
      } else {
         return obj.equals(datatypes.xsd_date) ? "DT" : "STR";
      }
   }

   public static Class<?> detectBinding(DataType obj) {
      if (obj.equals(datatypes.xsd_string)) {
         return String.class;
      } else if (obj.equals(datatypes.xsd_int)) {
         return Integer.class;
      } else if (obj.equals(datatypes.xsd_long)) {
         return Long.class;
      } else if (obj.equals(datatypes.xsd_float)) {
         return Float.class;
      } else if (obj.equals(datatypes.xsd_double)) {
         return Double.class;
      } else if (obj.equals(datatypes.xsd_boolean)) {
         return Boolean.class;
      } else {
         return obj.equals(datatypes.xsd_date) ? Date.class : String.class;
      }
   }

   public static String prepareMetaClassName(String suffix, String name) {
      name = name.trim();
      name = AccentUtil.removeAccents(name);
      name = name.replaceAll("[^\\w\\s]", "");
      name = WordUtils.capitalizeFully(name);
      name = name.replaceAll("\\s+", "");
      byte[] ptext = name.getBytes(Charset.forName("UTF-8"));
      return suffix + ":" + new String(ptext, Charset.forName("UTF-8"));
   }
}
