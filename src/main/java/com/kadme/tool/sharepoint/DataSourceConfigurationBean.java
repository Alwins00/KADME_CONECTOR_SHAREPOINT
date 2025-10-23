/* Decompiler 10ms, total 341ms, lines 70 */
package com.kadme.tool.sharepoint;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Properties;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(
   basePackages = {"com.kadme.tool.sharepoint.repositories"}
)
public class DataSourceConfigurationBean {
   @Autowired
   Environment env;

   @Bean
   public DataSource dataSource() {
      HikariConfig config = new HikariConfig();
      config.setDriverClassName(this.env.getProperty("snapshotdb-driverClassName"));
      config.setJdbcUrl(this.env.getProperty("snapshotdb-url"));
      config.setUsername(this.env.getProperty("snapshotdb-username"));
      config.setPassword(this.env.getProperty("snapshotdb-password"));
      config.setMaximumPoolSize(5);
      config.setAutoCommit(true);
      if (this.env.getProperty("snapshotdb-hibernate.ddl-auto") != null) {
         config.addDataSourceProperty("hibernate.hbm2ddl.auto", this.env.getProperty("snapshotdb-hibernate.ddl-auto"));
      }

      if (this.env.getProperty("hibernate.show_sql") != null) {
         config.addDataSourceProperty("hibernate.show_sql", this.env.getProperty("hibernate.show_sql"));
      }

      return new HikariDataSource(config);
   }

   @Bean
   public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
      LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
      em.setDataSource(this.dataSource());
      em.setPackagesToScan(new String[]{"com.kadme.tool.sharepoint.entity"});
      em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
      em.setJpaProperties(this.additionalProperties());
      return em;
   }

   final Properties additionalProperties() {
      Properties hibernateProperties = new Properties();
      if (this.env.getProperty("snapshotdb-hibernate.ddl-auto") != null) {
         hibernateProperties.setProperty("hibernate.hbm2ddl.auto", this.env.getProperty("snapshotdb-hibernate.ddl-auto"));
      }

      if (this.env.getProperty("snapshotdb-driverClassName") != null) {
         hibernateProperties.setProperty("snapshotdb-driverClassName", this.env.getProperty("snapshotdb-driverClassName"));
      }

      if (this.env.getProperty("hibernate.show_sql") != null) {
         hibernateProperties.setProperty("hibernate.show_sql", this.env.getProperty("hibernate.show_sql"));
      }

      return hibernateProperties;
   }
}
