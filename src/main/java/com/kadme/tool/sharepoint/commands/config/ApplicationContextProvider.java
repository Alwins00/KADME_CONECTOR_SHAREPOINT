/* Decompiler 3ms, total 192ms, lines 20 */
package com.kadme.tool.sharepoint.commands.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextProvider implements ApplicationContextAware {
   private static ApplicationContext context;

   public static ApplicationContext getApplicationContext() {
      return context;
   }

   public void setApplicationContext(ApplicationContext ac) throws BeansException {
      context = ac;
   }
}
