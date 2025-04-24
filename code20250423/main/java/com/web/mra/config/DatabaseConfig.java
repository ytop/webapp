package com.web.mra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
