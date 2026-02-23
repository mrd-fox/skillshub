package com.simplon_project.skillhub.skillhub.user.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.simplon_project.skillhub.skillhub.user.adapter.out.percistence",
        entityManagerFactoryRef = "userEntityManager",
        transactionManagerRef = "userTxManager"
)
public class UserDbConfig {
    @Bean
    @ConfigurationProperties("spring.datasource.user")
    public DataSourceProperties userDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource userDataSource() {
        return userDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean userEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(userDataSource());
        em.setPackagesToScan("com.simplon_project.skillhub.skillhub.user.adapter.out.percistence");
        em.setPersistenceUnitName("userPU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean
    public PlatformTransactionManager userTxManager(
            @Qualifier("userEntityManager") EntityManagerFactory userEntityManager
    ) {
        return new JpaTransactionManager(userEntityManager);
    }

    @Bean
    public SpringLiquibase userLiquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(userDataSource());
        liquibase.setChangeLog("classpath:db/changelog/user/changelog-user-master.yaml");
        liquibase.setDefaultSchema("public"); // ou le schema que tu veux
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
