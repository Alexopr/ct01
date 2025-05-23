package alg.coyote001.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
 
@Configuration
@EnableJpaRepositories(basePackages = "alg.coyote001.repository")
public class DatabaseConfig {
    // Можно добавить кастомные настройки JPA/TransactionManager при необходимости
} 