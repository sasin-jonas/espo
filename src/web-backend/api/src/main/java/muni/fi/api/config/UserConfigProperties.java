package muni.fi.api.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@Getter
@Setter
@ConfigurationProperties(prefix = "user")
@ConfigurationPropertiesScan
public class UserConfigProperties {
    private String initialAdminId;
}
