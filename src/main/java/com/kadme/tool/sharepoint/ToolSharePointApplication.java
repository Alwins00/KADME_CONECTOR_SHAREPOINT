/* Decompiler 17ms, total 155ms, lines 16 */
package com.kadme.tool.sharepoint;

import com.kadme.rest.webtool.AbstractToolApplication;
import com.kadme.rest.webtool.tool.Tool;
import org.springframework.boot.SpringApplication;

public class ToolSharePointApplication extends AbstractToolApplication {
   protected Tool getTool() {
      return new ToolSharePoint();
   }

   public static void main(String[] args) {
      SpringApplication.run(ToolSharePointApplication.class, args);
   }
}
