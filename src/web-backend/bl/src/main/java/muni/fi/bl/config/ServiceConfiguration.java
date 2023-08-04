package muni.fi.bl.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import muni.fi.bl.mappers.AuthorMapper;
import muni.fi.bl.mappers.DepartmentMapper;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.mappers.UserMapper;
import org.apache.http.HttpHost;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.elasticsearch.client.RestClient;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@ComponentScan
public class ServiceConfiguration {

    private final FilesConfigProperties filesConfigProperties;
    private final ApiConfigProperties apiConfigProperties;

    public ServiceConfiguration(FilesConfigProperties filesConfigProperties,
                                ApiConfigProperties apiConfigProperties) {
        this.filesConfigProperties = filesConfigProperties;
        this.apiConfigProperties = apiConfigProperties;
    }

    @Bean
    public ProjectMapper projectMapper() {
        return Mappers.getMapper(ProjectMapper.class);
    }

    @Bean
    public AuthorMapper authorMapper() {
        return Mappers.getMapper(AuthorMapper.class);
    }

    @Bean
    public DepartmentMapper departmentMapper() {
        return Mappers.getMapper(DepartmentMapper.class);
    }

    @Bean
    public UserMapper userMapper() {
        return Mappers.getMapper(UserMapper.class);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(apiConfigProperties.getElasticHostname(),
                        Integer.parseInt(apiConfigProperties.getElasticPort()))).build();
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    @Bean
    public Analyzer analyzer() {
        Resource resource = new ClassPathResource(filesConfigProperties.getStopWords());
        try {
            Reader inputStreamReader = new InputStreamReader(resource.getInputStream());
            return new StandardAnalyzer(inputStreamReader);
        } catch (IOException e) {
            throw new RuntimeException("Error while reading stopwords list", e);
        }
    }
}
