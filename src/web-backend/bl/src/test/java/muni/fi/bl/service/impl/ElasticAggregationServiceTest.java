package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Buckets;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.service.AggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.EXPERTISE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.HELIX_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.ROLE_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ElasticAggregationServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClientMock;

    // tested class
    private AggregationService aggregationService;

    @BeforeEach
    void setUp() {
        openMocks(this);

        aggregationService = new ElasticAggregationService(elasticsearchClientMock);
    }

    @Test
    void searchUniqueAggAll() throws IOException {
        // prepare
        SearchResponse<Void> searchResponse = getTermsAggregationSearchResponse();
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(Void.class))).thenReturn(searchResponse);

        // tested method
        Map<String, List<String>> uniqueFilterValues = aggregationService.searchUniqueAggAll();

        // verify
        assertThat(uniqueFilterValues.size(), equalTo(3));
        assertThat(uniqueFilterValues.get(HELIX_FIELD), equalTo(List.of("health", "digital")));
        assertThat(uniqueFilterValues.get(ROLE_FIELD), equalTo(List.of("consortium partner")));
        assertThat(uniqueFilterValues.get(EXPERTISE_FIELD), equalTo(List.of("big data", "data analytics", "web development")));

        verify(elasticsearchClientMock, times(3)).search(any(SearchRequest.class), eq(Void.class));
    }

    @Test
    void searchAllFiltersElasticError() throws IOException {
        // prepare
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(Void.class))).thenThrow(new IOException());

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> aggregationService.searchUniqueAggAll());

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo(ELASTIC_CONNECTION_ERROR));
    }

    private SearchResponse<Void> getTermsAggregationSearchResponse() {
        Aggregate helixAggregate = Aggregate.of(a -> a.sterms(
                StringTermsAggregate.of(t -> t
                        .buckets(Buckets.of(b -> b
                                .array(List.of(
                                        StringTermsBucket.of(sb -> sb
                                                .key("health")
                                                .docCount(25)),
                                        StringTermsBucket.of(sb -> sb
                                                .key("digital")
                                                .docCount(10)))))))));
        Aggregate roleAggregate = Aggregate.of(a -> a.sterms(
                StringTermsAggregate.of(t -> t
                        .buckets(Buckets.of(b -> b
                                .array(List.of(
                                        StringTermsBucket.of(sb -> sb
                                                .key("consortium partner")
                                                .docCount(25)))))))));
        Aggregate expertiseAggregate = Aggregate.of(a -> a.sterms(
                StringTermsAggregate.of(t -> t
                        .buckets(Buckets.of(b -> b
                                .array(List.of(
                                        StringTermsBucket.of(sb -> sb
                                                .key("big data")
                                                .docCount(25)),
                                        StringTermsBucket.of(sb -> sb
                                                .key("data analytics")
                                                .docCount(10)),
                                        StringTermsBucket.of(sb -> sb
                                                .key("web development")
                                                .docCount(8)))))))));
        return SearchResponse.of(r -> r
                .shards(s -> s.failed(0).successful(1).total(1))
                .took(10)
                .timedOut(false)
                .aggregations("helix-agg", helixAggregate)
                .aggregations("role-agg", roleAggregate)
                .aggregations("expertise-agg", expertiseAggregate)
                .hits(h -> h.hits(List.of())));
    }
}