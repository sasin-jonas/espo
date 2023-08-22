package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import muni.fi.bl.component.QueryBuilder;
import muni.fi.bl.component.SearchResultProcessor;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.service.RecommendationService;
import muni.fi.dal.entity.Project;
import muni.fi.dal.repository.ProjectRepository;
import muni.fi.dtos.OpportunityDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.DEFAULT_DOCS_SIZE;
import static muni.fi.bl.service.impl.ElasticSearchService.DESCRIPTION_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ElasticRecommendationServiceTest {

    @Mock
    private ProjectRepository projectRepositoryMock;
    @Mock
    private ElasticsearchClient elasticsearchClientMock;
    @Mock
    private SearchResultProcessor<OpportunityDto> resultProcessorMock;
    @Mock
    private QueryBuilder queryBuilderMock;

    @Captor
    private ArgumentCaptor<String> ucoCaptor;
    @Captor
    private ArgumentCaptor<SearchRequest> requestCaptor;

    // tested class
    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() throws IOException {
        openMocks(this);

        // mappers are not mocked as the implementation is not unit-tested because it is automatically generated by mapStruct
        ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);
        recommendationService = new ElasticRecommendationService(projectRepositoryMock, mapper, elasticsearchClientMock, resultProcessorMock, queryBuilderMock);

        // setup mocks
        Project project1 = new Project();
        Project project2 = new Project();
        project1.setId(1L);
        project2.setId(2L);
        String someIndex = "someIndex";

        OpportunityDto opportunity1 = new OpportunityDto();
        OpportunityDto opportunity2 = new OpportunityDto();
        opportunity1.setScore(10.0);
        opportunity2.setScore(20.0);
        Hit<OpportunityDto> hit1 = Hit.of(h -> h
                .id("abcd")
                .index(someIndex)
                .score(10.0)
                .source(opportunity1));
        Hit<OpportunityDto> hit2 = Hit.of(h -> h
                .id("bdca")
                .index(someIndex)
                .score(20.0)
                .source(opportunity2));

        SearchResponse<OpportunityDto> searchResponse = SearchResponse.of(r -> r
                .shards(s -> s.failed(0).successful(1).total(1))
                .took(10)
                .timedOut(false)
                .hits(h -> h
                        .hits(List.of(hit1, hit2))));

        assert hit2.source() != null;
        assert hit1.source() != null;
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(DESCRIPTION_FIELD, List.of(hit1, hit2));

        when(projectRepositoryMock.findByAuthorUco(any())).thenReturn(List.of(project1, project2));
        when(queryBuilderMock.getMoreLikeThisQuery(anyList())).thenReturn(MoreLikeThisQuery.of(b -> b
                .like(l -> l
                        .document(d -> d
                                .index(someIndex)
                                .id("someId")))));
        when(queryBuilderMock.getMoreLikeThisQuery(anyString(), eq(CROWDHELIX_INDEX))).thenReturn(MoreLikeThisQuery.of(b -> b
                .like(l -> l
                        .document(d -> d
                                .index(someIndex)
                                .id("someId")))));
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class)))
                .thenReturn(searchResponse);
        when(resultProcessorMock.aggregateResultsByScore(topResultsMap)).thenReturn(List.of(hit2.source(), hit1.source()));
    }

    @Test
    void recommendForUser() throws IOException {
        // prepare
        String uco = "123456";

        // tested method
        List<OpportunityDto> opportunityDtos = recommendationService.recommendForAuthor(uco, Collections.emptyList());

        // verify
        assertThat(opportunityDtos.size(), equalTo(2));
        assertThat(opportunityDtos.get(0).getScore(), equalTo(20.0));
        assertThat(opportunityDtos.get(1).getScore(), equalTo(10.0));

        verify(projectRepositoryMock).findByAuthorUco(ucoCaptor.capture());
        assertThat(ucoCaptor.getValue(), equalTo(uco));
        verify(elasticsearchClientMock).search(requestCaptor.capture(), eq(OpportunityDto.class));
        assertThat(requestCaptor.getValue().size(), equalTo(DEFAULT_DOCS_SIZE));
        assertThat(requestCaptor.getValue().index().get(0), equalTo(CROWDHELIX_INDEX));

        verify(queryBuilderMock, times(1)).getMoreLikeThisQuery(anyList());
        verify(elasticsearchClientMock, times(1))
                .search(any(SearchRequest.class), eq(OpportunityDto.class));
        verify(resultProcessorMock, times(1)).aggregateResultsByScore(any());
    }

    @Test
    void recommendForUserEmpty() throws IOException {
        // prepare
        String uco = "123456";

        // tested method
        List<OpportunityDto> opportunityDtos = recommendationService.recommendForAuthor(uco, List.of(1L, 2L));

        // verify
        assertThat(opportunityDtos, empty());

        verify(projectRepositoryMock).findByAuthorUco(ucoCaptor.capture());
        assertThat(ucoCaptor.getValue(), equalTo(uco));

        verify(queryBuilderMock, times(0)).getMoreLikeThisQuery(anyList());
        verify(elasticsearchClientMock, times(0))
                .search(any(SearchRequest.class), eq(OpportunityDto.class));
        verify(resultProcessorMock, times(0)).aggregateResultsByScore(any());
    }

    @Test
    void recommendMoreLikeThis() throws IOException {
        // prepare
        String someId = "someId";

        // tested method
        List<OpportunityDto> opportunityDtos = recommendationService.recommendMoreLikeThis(someId);

        // verify
        assertThat(opportunityDtos.size(), equalTo(2));
        assertThat(opportunityDtos.get(0).getScore(), equalTo(20.0));
        assertThat(opportunityDtos.get(1).getScore(), equalTo(10.0));

        verify(queryBuilderMock, times(1)).getMoreLikeThisQuery(someId, CROWDHELIX_INDEX);
        verify(elasticsearchClientMock, times(1))
                .search(any(SearchRequest.class), eq(OpportunityDto.class));
        verify(resultProcessorMock, times(1)).aggregateResultsByScore(any());
    }

    @Test
    void elasticClientSearchException() throws IOException {
        // prepare
        ErrorCause cause = ErrorCause.of(c -> c
                .type("type")
                .reason("reason"));
        ErrorResponse response = ErrorResponse.of(e -> e
                .error(cause)
                .status(404));
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class)))
                .thenThrow(new ElasticsearchException("/search", response));
        String someId = "someId";

        // tested method
        Throwable exception = assertThrows(AppException.class, () -> recommendationService.recommendMoreLikeThis(someId));

        // verify
        assertThat(exception.getCause(), instanceOf(ElasticsearchException.class));
        assertThat(exception.getMessage(), equalTo("Failed to perform search or no data was found"));
    }

    @Test
    void elasticClientConnectionException() throws IOException {
        // prepare
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class)))
                .thenThrow(new IOException());
        String someId = "someId";

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () -> recommendationService.recommendMoreLikeThis(someId));

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo(ELASTIC_CONNECTION_ERROR));
    }
}