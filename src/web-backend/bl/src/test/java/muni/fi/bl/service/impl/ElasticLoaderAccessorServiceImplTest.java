package muni.fi.bl.service.impl;

import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.service.ElasticLoaderAccessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

import static muni.fi.bl.service.impl.ElasticLoaderAccessorServiceImpl.DATA_UPLOAD_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

class ElasticLoaderAccessorServiceImplTest {

    private static final String FILE_NAME = "file.csv";
    private static final byte[] DATA = "DATA".getBytes(StandardCharsets.UTF_8);
    private static final String LOADER_URL = "https://localhost";
    private static final String LOADER_PORT = "5000";
    private static final String UPLOAD_ENDPOINT = "/any";

    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private ApiConfigProperties apiConfigPropertiesMock;

    @Captor
    private ArgumentCaptor<HttpEntity<MultiValueMap<String, Object>>> httpRequestCaptor;

    // tested class
    private ElasticLoaderAccessorService elasticLoaderAccessor;

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(apiConfigPropertiesMock.getDataLoaderUrl()).thenReturn(LOADER_URL);
        when(apiConfigPropertiesMock.getDataLoaderPort()).thenReturn(LOADER_PORT);
        elasticLoaderAccessor = new ElasticLoaderAccessorServiceImpl(restTemplateMock, apiConfigPropertiesMock);
    }

    @Test
    void sendDataToElasticLoader() {
        // prepare
        String successMessage = "success!";
        when(restTemplateMock.exchange(eq(LOADER_URL + ":" + LOADER_PORT + UPLOAD_ENDPOINT), eq(HttpMethod.POST), any(), eq(String.class))).thenReturn(
                ResponseEntity.of(Optional.of(successMessage))
        );

        // tested method
        String response = elasticLoaderAccessor.sendDataToElasticLoader(FILE_NAME, DATA, UPLOAD_ENDPOINT);

        // verify
        assertThat(response, equalTo(successMessage));

        verify(restTemplateMock).exchange(eq(LOADER_URL + ":" + LOADER_PORT + UPLOAD_ENDPOINT), eq(HttpMethod.POST), httpRequestCaptor.capture(), eq(String.class));
        assertThat(httpRequestCaptor.getValue().getHeaders().getContentType(), equalTo(MULTIPART_FORM_DATA));

        MultiValueMap<String, Object> requestBody = Objects.requireNonNull(httpRequestCaptor.getValue().getBody());
        assertThat(requestBody.get("file").size(), equalTo(1));

        ByteArrayResource file = (ByteArrayResource) requestBody.get("file").get(0);
        assertThat(file.getFilename(), equalTo(FILE_NAME));
        assertThat(file.getByteArray(), equalTo(DATA));
    }

    @Test
    void sendDataToElasticLoaderConnectionError() {
        // prepare
        when(restTemplateMock.exchange(eq(LOADER_URL + ":" + LOADER_PORT + UPLOAD_ENDPOINT), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> elasticLoaderAccessor.sendDataToElasticLoader(FILE_NAME, DATA, UPLOAD_ENDPOINT));

        // verify
        assertThat(exception.getCause(), instanceOf(HttpClientErrorException.class));
        assertThat(exception.getMessage(), equalTo(DATA_UPLOAD_ERROR));
    }

    @Test
    void sendDataToElasticLoaderServerError() {
        // prepare
        when(restTemplateMock.exchange(eq(LOADER_URL + ":" + LOADER_PORT + UPLOAD_ENDPOINT), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        // tested method
        Throwable exception = assertThrows(AppException.class, () -> elasticLoaderAccessor.sendDataToElasticLoader(FILE_NAME, DATA, UPLOAD_ENDPOINT));

        // verify
        assertThat(exception.getCause(), instanceOf(HttpServerErrorException.class));
        assertThat(exception.getMessage(), equalTo(DATA_UPLOAD_ERROR + ". File format might be invalid"));
    }
}