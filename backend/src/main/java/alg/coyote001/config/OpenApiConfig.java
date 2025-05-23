package alg.coyote001.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("CRUD Admin API")
                .version("1.0")
                .description("API documentation for CRUD Admin application")
                .contact(new Contact().name("Developer").email("dev@example.com")))
            .externalDocs(new ExternalDocumentation()
                .description("Project Wiki")
                .url("https://example.com/wiki"));
    }
} 