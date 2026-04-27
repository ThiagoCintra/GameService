package com.gameservice.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.gameservice.infrastructure.persistence",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.gameservice\\.infrastructure\\.persistence\\.mongo\\..*"))
@EnableMongoRepositories(
        basePackages = "com.gameservice.infrastructure.persistence.mongo")
public class RepositoryConfig {
}
