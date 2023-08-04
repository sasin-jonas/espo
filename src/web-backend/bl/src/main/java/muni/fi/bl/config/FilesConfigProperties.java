package muni.fi.bl.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "files")
public class FilesConfigProperties {
    private String stopWords;
}
