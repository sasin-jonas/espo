package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.DeleteRequest;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import muni.fi.bl.config.ApiConfigProperties;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.exceptions.NotFoundException;
import muni.fi.bl.service.ElasticLoaderAccessorService;
import muni.fi.bl.service.OpportunityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.OpportunityServiceImpl.EXAMPLE_CSV_URL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class OpportunityServiceImplTest {

    private static final String ID = "someId";
    private static final String LOADER_URL = "https://localhost";
    private static final String LOADER_PORT = "5000";

    @Mock
    private ElasticsearchClient elasticsearchClientMock;
    @Mock
    private RestTemplate restTemplateMock;
    @Mock
    private ApiConfigProperties apiConfigPropertiesMock;
    @Mock
    private ElasticLoaderAccessorService elasticLoaderAccessor;

    @Mock
    private ElasticsearchIndicesClient elasticsearchIndicesClientMock;

    @Captor
    private ArgumentCaptor<DeleteRequest> deleteRequestCaptor;
    @Captor
    private ArgumentCaptor<DeleteIndexRequest> deleteIndexRequestCaptor;

    // tested class
    private OpportunityService opportunityService;

    @BeforeEach
    void setUp() {
        openMocks(this);

        when(apiConfigPropertiesMock.getDataLoaderUrl()).thenReturn(LOADER_URL);
        when(apiConfigPropertiesMock.getDataLoaderPort()).thenReturn(LOADER_PORT);
        opportunityService = new OpportunityServiceImpl(elasticsearchClientMock, restTemplateMock, elasticLoaderAccessor, apiConfigPropertiesMock);
    }

    @Test
    void delete() throws IOException {
        // prepare
        DeleteResponse response = DeleteResponse.of(r -> r
                .id(ID)
                .index(CROWDHELIX_INDEX)
                .primaryTerm(1)
                .seqNo(1)
                .shards(s -> s.failed(0).successful(1).total(1))
                .version(1)
                .result(Result.Deleted));
        when(elasticsearchClientMock.delete(any(DeleteRequest.class))).thenReturn(response);

        // tested method
        opportunityService.delete(ID);

        // verify
        verify(elasticsearchClientMock).delete(deleteRequestCaptor.capture());
        assertThat(deleteRequestCaptor.getValue().id(), equalTo(ID));
        assertThat(deleteRequestCaptor.getValue().index(), equalTo(CROWDHELIX_INDEX));
        assertThat(deleteRequestCaptor.getValue().refresh(), equalTo(Refresh.True));
    }

    @Test
    void deleteFailDoesntExist() throws IOException {
        // prepare
        DeleteResponse response = DeleteResponse.of(r -> r
                .id(ID)
                .index(CROWDHELIX_INDEX)
                .primaryTerm(1)
                .seqNo(1)
                .shards(s -> s.failed(0).successful(1).total(1))
                .version(1)
                .result(Result.NotFound));
        when(elasticsearchClientMock.delete(any(DeleteRequest.class))).thenReturn(response);

        // tested method
        Throwable exception = assertThrows(NotFoundException.class, () -> opportunityService.delete(ID));

        // verify
        assertThat(exception.getMessage(), equalTo("Couldn't delete document with id 'someId'"));
    }

    @Test
    void deleteFailConnection() throws IOException {
        // prepare
        when(elasticsearchClientMock.delete(any(DeleteRequest.class))).thenThrow(new IOException());

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> opportunityService.delete(ID));

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo(ELASTIC_CONNECTION_ERROR));
    }

    @Test
    void deleteAll() throws IOException {
        // prepare
        when(elasticsearchClientMock.indices()).thenReturn(elasticsearchIndicesClientMock);
        when(elasticsearchClientMock.indices().delete(any(DeleteIndexRequest.class))).thenReturn(null);

        // tested method
        opportunityService.deleteAll();

        // verify
        verify(elasticsearchIndicesClientMock).delete(deleteIndexRequestCaptor.capture());
        assertThat(deleteIndexRequestCaptor.getValue().index().get(0), equalTo(CROWDHELIX_INDEX));
        assertThat(deleteIndexRequestCaptor.getValue().allowNoIndices(), is(true));
        assertThat(deleteIndexRequestCaptor.getValue().ignoreUnavailable(), is(true));
    }

    @Test
    void deleteAllConnectionFail() throws IOException {
        // prepare
        when(elasticsearchClientMock.indices()).thenReturn(elasticsearchIndicesClientMock);
        when(elasticsearchClientMock.indices().delete(any(DeleteIndexRequest.class))).thenThrow(new IOException());

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> opportunityService.deleteAll());

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo(ELASTIC_CONNECTION_ERROR));
    }

    @Test
    void getSampleCsvContent() {
        // prepare
        String example = "example";
        when(restTemplateMock.getForEntity(eq(LOADER_URL + ":" + LOADER_PORT + EXAMPLE_CSV_URL), eq(String.class))).thenReturn(
                ResponseEntity.of(Optional.of(example))
        );

        // tested method
        String response = opportunityService.getSampleCsvContent();

        // verify
        assertThat(response, equalTo(example));
        verify(restTemplateMock).getForEntity(eq(LOADER_URL + ":" + LOADER_PORT + EXAMPLE_CSV_URL), eq(String.class));
    }

    @Test
    void getSampleCsvContentConnectionFail() {
        // prepare
        when(restTemplateMock.getForEntity(eq(LOADER_URL + ":" + LOADER_PORT + EXAMPLE_CSV_URL), eq(String.class))).thenThrow(
                new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
        );

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> opportunityService.getSampleCsvContent());

        // verify
        assertThat(exception.getCause(), instanceOf(HttpClientErrorException.class));
        assertThat(exception.getMessage(), equalTo("Failed to download sample CSV"));
    }
}