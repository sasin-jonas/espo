package muni.fi.bl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "api-route")
public class ApiConfigProperties {
    private String IssuerUserinfoUri;
    private String dataLoaderUrl;
    private String dataLoaderPort;
    private String elasticHostname;
    private String elasticPort;
    private String issuerJwkUri;
    private String issuerAuthorizeUri;
    private String issuerTokenUri;
}
