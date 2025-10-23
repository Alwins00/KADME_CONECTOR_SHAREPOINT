/* Decompiler 6ms, total 127ms, lines 29 */
package com.kadme.tool.sharepoint;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class TaskGetterAsyncConfiguration {
   public static final int MAXPOOLSIZE = 5;
   public static final String TASK_GETTER_BEAN_NAME = "asyncSharepointGetterExecutor";

   @Bean(
      name = {"asyncSharepointGetterExecutor"}
   )
   public Executor taskExecutor() {
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setMaxPoolSize(5);
      executor.setCorePoolSize(1);
      executor.setQueueCapacity(1);
      executor.setThreadNamePrefix("GetterThread-");
      executor.setKeepAliveSeconds(86400000);
      executor.initialize();
      return executor;
   }
}
