package muni.fi.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import muni.fi.bl.config.ApiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * This class is only used for openAPI documentation configuration
 */
@Configuration
public class OpenApiConfig {

    private final ApiConfigProperties apiConfigProperties;

    public OpenApiConfig(ApiConfigProperties apiConfigProperties) {
        this.apiConfigProperties = apiConfigProperties;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("spring_oauth", new SecurityScheme()
                                .type(SecurityScheme.Type.OAUTH2)
                                .description("Oauth2 flow")
                                .flows(new OAuthFlows()
                                        .authorizationCode(new OAuthFlow()
                                                .authorizationUrl(apiConfigProperties.getIssuerAuthorizeUri())
                                                .tokenUrl(apiConfigProperties.getIssuerTokenUri())
                                                .scopes(new Scopes()
                                                        .addString("openid", "openid")
                                                        .addString("profile", "profile")
                                                        .addString("email", "email")
                                                        .addString("offline_access", "offline_access")
                                                        .addString("eduperson_entitlement", "eduperson_entitlement")
                                                        .addString("user_identifiers", "user_identifiers")
                                                ))))
                )
                .security(List.of(
                        new SecurityRequirement().addList("spring_oauth")))
                .info(new Info()
                        .title("Opportunity and collaboration search API")
                        .description("This is a Spring Boot REST-ful service. You need to authenticate before using any endpoints")
                        .version("1.0")
                );
    }
}
