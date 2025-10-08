package com.example.bankcards.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseConfig {
    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-master.xml");
        liquibase.setShouldRun(true);
        liquibase.setContexts("dev,production");
        liquibase.setDefaultSchema("public");
        liquibase.setLiquibaseSchema("public");
        liquibase.setLiquibaseTablespace("public");
        liquibase.setDatabaseChangeLogTable("DATABASECHANGELOG");
        liquibase.setDatabaseChangeLogLockTable("DATABASECHANGELOGLOCK");
        return liquibase;
    }
}