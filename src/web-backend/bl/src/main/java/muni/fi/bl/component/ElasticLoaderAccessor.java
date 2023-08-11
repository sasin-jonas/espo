package muni.fi.bl.component;

import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class ElasticLoaderAccessor {

    public static final String DATA_UPLOAD_ERROR = "Error while uploading data to Elasticsearch";

    private final RestTemplate restTemplate;

    private final String dataLoaderUrl;

    public ElasticLoaderAccessor(RestTemplate restTemplate,
                                 ApiConfigProperties apiConfigProperties) {
        this.restTemplate = restTemplate;
        dataLoaderUrl = String.format("%s:%s",
                apiConfigProperties.getDataLoaderUrl(), apiConfigProperties.getDataLoaderPort());
    }

    /**
     * Loads opportunities to ElasticSearch index
     *
     * @param fileName    Source file name
     * @param data        Source file data
     * @param endpointUri Loader endpoint to send the data to (e.g. "/load")
     * @return Load response message
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    public String sendDataToElasticLoader(String fileName, byte[] data, String endpointUri) {
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
            response = restTemplate.exchange(String.format("%s%s", dataLoaderUrl, endpointUri), HttpMethod.POST, requestEntity, String.class);
        } catch (HttpClientErrorException e) {
            log.error(DATA_UPLOAD_ERROR, e);
            throw new ConnectionException(DATA_UPLOAD_ERROR, e);
        } catch (HttpServerErrorException e) {
            log.error(DATA_UPLOAD_ERROR, e);
            throw new AppException(DATA_UPLOAD_ERROR + ". File format might be invalid", e);
        }

        return response.getBody();
    }
}
