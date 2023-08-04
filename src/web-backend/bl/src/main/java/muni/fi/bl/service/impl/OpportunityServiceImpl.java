package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.exceptions.NotFoundException;
import muni.fi.bl.service.OpportunityService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;

@Slf4j
@Service
public class OpportunityServiceImpl implements OpportunityService {

    public static final String UPLOAD_URL = "/load";
    public static final String EXAMPLE_CSV_URL = "/example-csv";

    public static final String DATA_UPLOAD_ERROR = "Error while uploading data to Elasticsearch";

    private final ElasticsearchClient elasticsearchClient;
    private final RestTemplate restTemplate;

    private final String uploadOpportunitiesUrl;
    private final String exampleCsvUrl;

    public OpportunityServiceImpl(ElasticsearchClient elasticsearchClient,
                                  RestTemplate restTemplate,
                                  ApiConfigProperties apiConfigProperties) {
        this.elasticsearchClient = elasticsearchClient;
        this.restTemplate = restTemplate;

        uploadOpportunitiesUrl = String.format("%s:%s%s",
                apiConfigProperties.getDataLoaderUrl(), apiConfigProperties.getDataLoaderPort(), UPLOAD_URL);
        exampleCsvUrl = String.format("%s:%s%s",
                apiConfigProperties.getDataLoaderUrl(), apiConfigProperties.getDataLoaderPort(), EXAMPLE_CSV_URL);
    }

    @Override
    public void delete(String id) {
        DeleteRequest deleteRequest = DeleteRequest.of(b -> b
                .index(CROWDHELIX_INDEX)
                .id(id)
                .refresh(Refresh.True));
        try {
            DeleteResponse response = elasticsearchClient.delete(deleteRequest);
            if (response.result() != Result.Deleted) {
                String message = String.format("Couldn't delete document with id '%s'", id);
                log.warn(message);
                throw new NotFoundException(message);
            }
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        }
    }

    @Override
    public void deleteAll() {
        DeleteIndexRequest deleteRequest = DeleteIndexRequest.of(b -> b
                .index(CROWDHELIX_INDEX)
                .allowNoIndices(true)
                .ignoreUnavailable(true));
        try {
            elasticsearchClient.indices().delete(deleteRequest);
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        }
    }

    @Override
    public String load(String fileName, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(data) {
            @Override
            public String getFilename() {
                return fileName;
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(uploadOpportunitiesUrl, HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            log.error(DATA_UPLOAD_ERROR, e);
            throw new ConnectionException(DATA_UPLOAD_ERROR, e);
        } catch (HttpServerErrorException e) {
            log.error(DATA_UPLOAD_ERROR, e);
            throw new AppException(DATA_UPLOAD_ERROR + ". File format might be invalid", e);
        }

        return response.getBody();
    }

    @Override
    public String getSampleCsvContent() {
        ResponseEntity<String> responseEntity;
        try {
            responseEntity = restTemplate.getForEntity(
                    exampleCsvUrl, String.class);
        } catch (RuntimeException e) {
            String message = "Failed to download sample CSV";
            log.error(message, e);
            throw new ConnectionException(message, e);
        }
        return responseEntity.getBody();
    }
}
