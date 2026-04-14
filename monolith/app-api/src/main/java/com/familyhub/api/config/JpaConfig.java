package com.familyhub.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.familyhub.db")
@EntityScan(basePackages = "com.familyhub.domain")
public class JpaConfig {}
