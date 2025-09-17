package com.simplon_project.skillhub.skillhub.course.config;

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

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.simplon_project.skillhub.skillhub.course",
        entityManagerFactoryRef = "courseEntityManager",
        transactionManagerRef = "courseTxManager"
)
public class CourseDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.course")
    public DataSourceProperties courseDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource courseDataSource() {
        return courseDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean courseEntityManager() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(courseDataSource());
        em.setPackagesToScan("com.simplon_project.skillhub.skillhub.course");
        em.setPersistenceUnitName("coursePU");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }

    @Bean
    public PlatformTransactionManager courseTxManager(
            @Qualifier("courseEntityManager") EntityManagerFactory courseEntityManager
    ) {
        return new JpaTransactionManager(courseEntityManager);
    }

    @Bean
    public SpringLiquibase courseLiquibase() {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(courseDataSource());
        liquibase.setChangeLog("classpath:db/changelog/course/changelog-course-master.yaml");
        return liquibase;
    }
}