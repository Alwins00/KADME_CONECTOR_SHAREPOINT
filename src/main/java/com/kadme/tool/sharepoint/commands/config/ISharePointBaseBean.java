/* Decompiler 4ms, total 117ms, lines 8 */
package com.kadme.tool.sharepoint.commands.config;

public abstract class ISharePointBaseBean<T> {
   public static <T> T getBean(Class<T> obj) {
      return ApplicationContextProvider.getApplicationContext().getBean(obj.getSimpleName(), obj);
   }
}
