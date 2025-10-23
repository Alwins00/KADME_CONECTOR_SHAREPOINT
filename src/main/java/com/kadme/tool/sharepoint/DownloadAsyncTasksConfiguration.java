/* Decompiler 4ms, total 121ms, lines 30 */
package com.kadme.tool.sharepoint;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class DownloadAsyncTasksConfiguration {
   public static final int MAXPOOLSIZE = 3;
   public static final String TASK_EXECUTOR_BEAN_NAME = "asyncSharepointDownloadExecutor";
   private ThreadPoolExecutor executor;

   @Bean(
      name = {"asyncSharepointDownloadExecutor"}
   )
   public Executor taskExecutor() {
      this.executor = new ThreadPoolExecutor(3, 3, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
      return this.executor;
   }

   public ThreadPoolExecutor getExecutor() {
      return this.executor;
   }
}
