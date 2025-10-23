/* Decompiler 6ms, total 208ms, lines 30 */
package com.kadme.tool.sharepoint;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class ContentIndexerAsyncTasksConfiguration {
   public static final int MAXPOOLSIZE = 1;
   public static final String TASK_EXECUTOR_BEAN_NAME = "asyncCITaskExecutor";
   private ThreadPoolExecutor executor;

   @Bean(
      name = {"asyncCITaskExecutor"}
   )
   public ExecutorService taskExecutor() {
      this.executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
      return this.executor;
   }

   public ThreadPoolExecutor getExecutor() {
      return this.executor;
   }
}
