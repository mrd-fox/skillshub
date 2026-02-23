package com.simplon_project.skillhub.skillhub.storage.config;

import jakarta.persistence.EntityManagerFactory;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
        basePackages = "com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence",
        entityManagerFactoryRef = "storageEntityManager",
        transactionManagerRef = "storageTxManager"
)
@ConditionalOnProperty(prefix = "storage", name = "enabled", havingValue = "true")
public class StorageDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.storage")
    public DataSourceProperties storageDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource storageDataSource() {
        return storageDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean storageEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(storageDataSource());
        em.setPackagesToScan("com.simplon_project.skillhub.skillhub.storage.adapter.out.persistence");
        em.setPersistenceUnitName("storagePU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean
    public PlatformTransactionManager storageTxManager(
            @Qualifier("storageEntityManager") EntityManagerFactory storageEntityManager
    ) {
        return new JpaTransactionManager(storageEntityManager);
    }

    @Bean
    public SpringLiquibase storageLiquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(storageDataSource());
        liquibase.setChangeLog("classpath:db/changelog/storage/changelog-storage-master.yaml");
        return liquibase;
    }
}