package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import muni.fi.bl.component.QueryBuilder;
import muni.fi.bl.component.SearchPerformer;
import muni.fi.bl.component.SearchResultProcessor;
import muni.fi.bl.component.TextNormalizer;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.mappers.AuthorMapper;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.service.ProjectService;
import muni.fi.bl.service.RecommendationService;
import muni.fi.bl.service.SearchService;
import muni.fi.dal.repository.AuthorRepository;
import muni.fi.dal.repository.ProjectRepository;
import muni.fi.dtos.AuthorDto;
import muni.fi.dtos.OpportunityDto;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectEsDto;
import muni.fi.query.SearchInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.DESCRIPTION_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.MAX_DOCS_SIZE;
import static muni.fi.bl.service.impl.ElasticSearchService.TITLE_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class ElasticSearchServiceTest {

    @Mock
    private ElasticsearchClient elasticsearchClientMock;
    @Mock
    private SearchResultProcessor<OpportunityDto> resultProcessorMock;
    @Mock
    private SearchResultProcessor<ProjectEsDto> projectResultProcessor;
    @Mock
    private ProjectService projectServiceMock;
    @Mock
    private RecommendationService recommendationServiceMock;
    @Mock
    private QueryBuilder queryBuilderMock;
    @Mock
    private TextNormalizer textNormalizer;
    @Mock
    private AuthorMapper authorMapper;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;

    @Captor
    private ArgumentCaptor<SearchInfo> infoCaptor;
    @Captor
    private ArgumentCaptor<SearchRequest> requestCaptor;

    // tested class
    private SearchService searchService;

    @BeforeEach
    void setUp() throws IOException {
        openMocks(this);

        SearchPerformer<OpportunityDto> opportunitySearchPerformer = new SearchPerformer<>(elasticsearchClientMock);
        SearchPerformer<ProjectEsDto> projectSearchPerformer = new SearchPerformer<>(elasticsearchClientMock);
        searchService = new ElasticSearchService(
                resultProcessorMock, projectResultProcessor, projectServiceMock,
                recommendationServiceMock, queryBuilderMock, textNormalizer, authorRepository, authorMapper,
                projectRepository, projectMapper, opportunitySearchPerformer, projectSearchPerformer);

        // setup mocks
        AuthorDto authorDto1 = new AuthorDto("John Doe", "123456", "student");
        AuthorDto authorDto2 = new AuthorDto("Jenna Doe", "654321", "employee");
        ProjectDto project1 = new ProjectDto();
        ProjectDto project2 = new ProjectDto();
        project1.setId(1L);
        project2.setId(2L);
        project1.setAuthor(authorDto1);
        project2.setAuthor(authorDto2);
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
                .shards(s -> s.failed(0).successful(2).total(2))
                .took(10)
                .timedOut(false)
                .hits(h -> h
                        .hits(List.of(hit1, hit2))));

        assert hit2.source() != null;
        assert hit1.source() != null;
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(DESCRIPTION_FIELD, List.of(hit1, hit2));

        MoreLikeThisQuery mltQuery = MoreLikeThisQuery.of(b -> b
                .like(l -> l
                        .document(d -> d
                                .index(someIndex)
                                .id("someId"))));

        when(projectServiceMock.getByAuthorUco(any())).thenReturn(List.of(project1, project2));
        when(projectServiceMock.getById(1L)).thenReturn(project1);
        when(projectServiceMock.getById(2L)).thenReturn(project2);

        when(queryBuilderMock.getMoreLikeThisQuery(anyList())).thenReturn(mltQuery);
        when(queryBuilderMock.getMoreLikeThisQuery(anyList(), anyList())).thenReturn(mltQuery);
        when(queryBuilderMock.getFilterQuery(any())).thenReturn(BoolQuery.of(b -> b));

        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class))).thenReturn(searchResponse);
        when(resultProcessorMock.aggregateResultsByScore(topResultsMap)).thenReturn(List.of(hit2.source(), hit1.source()));
        when(resultProcessorMock.aggregateResultsAndRecommendations(anyList(), anyList())).thenReturn(List.of(opportunity2, opportunity1));

        when(recommendationServiceMock.recommendForAuthor(any(), anyList())).thenReturn(Collections.emptyList());
    }

    @Test
    void searchByProjects() throws IOException {
        // prepare
        SearchInfo info = new SearchInfo(30, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, List.of(1L, 2L), true, null);

        // tested method
        List<OpportunityDto> opportunityDtos = searchService.searchByProjects(info);

        // verify
        assertThat(opportunityDtos.size(), equalTo(2));
        assertThat(opportunityDtos.get(0).getScore(), equalTo(20.0));
        assertThat(opportunityDtos.get(1).getScore(), equalTo(10.0));

        verify(projectServiceMock, times(2)).getById(any());
        verify(recommendationServiceMock, times(2)).recommendForAuthor(any(), eq(List.of(1L, 2L)));
        verify(resultProcessorMock, times(1)).aggregateResultsAndRecommendations(anyList(), anyList());
        verify(queryBuilderMock, times(1)).getMoreLikeThisQuery(anyList());
        verify(queryBuilderMock, times(1)).getMoreLikeThisQuery(anyList(), eq(List.of(DESCRIPTION_FIELD, TITLE_FIELD)));

        verify(elasticsearchClientMock, times(2)).search(requestCaptor.capture(), eq(OpportunityDto.class));
        assertThat(requestCaptor.getValue().size(), equalTo(MAX_DOCS_SIZE));
        assertThat(requestCaptor.getValue().from(), equalTo(0));
        assertThat(requestCaptor.getValue().sort(), empty());
        assertThat(requestCaptor.getValue().index().get(0), equalTo(CROWDHELIX_INDEX));

        verify(queryBuilderMock).getFilterQuery(infoCaptor.capture());
        assertThat(infoCaptor.getValue(), equalTo(info));
    }

    @Test
    void searchByAuthors() {
        // prepare
        SearchInfo info = new SearchInfo(30, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                List.of("uco1", "uco2"), List.of(1L), true, null);

        // tested method
        List<OpportunityDto> opportunityDtos = searchService.searchByAuthors(info);

        // verify
        assertThat(opportunityDtos.size(), equalTo(2));
        assertThat(opportunityDtos.get(0).getScore(), equalTo(20.0));
        assertThat(opportunityDtos.get(1).getScore(), equalTo(10.0));
        verify(projectServiceMock, times(1)).getById(any());
        verify(projectServiceMock, times(2)).getByAuthorUco(any());
        verify(resultProcessorMock, times(1)).aggregateResultsAndRecommendations(anyList(), anyList());
        verify(resultProcessorMock, times(2)).aggregateResultsByScore(any());

        verify(queryBuilderMock, times(2)).getMoreLikeThisQuery(anyList());
        verify(queryBuilderMock, times(2)).getMoreLikeThisQuery(anyList(), eq(List.of(DESCRIPTION_FIELD, TITLE_FIELD)));
        verify(queryBuilderMock, times(2)).getFilterQuery(infoCaptor.capture());
        assertThat(infoCaptor.getValue(), equalTo(info));
    }

    @Test
    void searchByPhrase() throws IOException {
        // prepare
        String phrase = "phrase";
        when(queryBuilderMock.getMultiMatchQuery(phrase)).thenReturn(MultiMatchQuery.of(m -> m.query(phrase)));
        when(textNormalizer.normalize(phrase)).thenReturn(phrase);
        SearchInfo info = new SearchInfo(30, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), false, phrase);

        // tested method
        searchService.searchByPhrase(info);

        // verify
        verify(textNormalizer).normalize(phrase);
        verify(queryBuilderMock).getMultiMatchQuery(phrase);
        verify(queryBuilderMock).getFilterQuery(info);
        verify(elasticsearchClientMock).search(requestCaptor.capture(), eq(OpportunityDto.class));
        assertThat(requestCaptor.getValue().size(), equalTo(30));
        assertThat(requestCaptor.getValue().from(), equalTo(0));
        assertThat(Objects.requireNonNull(requestCaptor.getValue().query()).isMultiMatch(), is(true));
    }

    @Test
    void searchForAll1() throws IOException {
        // prepare
        when(queryBuilderMock.getSearchAllQuery()).thenReturn(new MatchAllQuery.Builder().build());
        when(queryBuilderMock.getFilterQuery(eq("uco"), eq("someValue")))
                .thenReturn(new WildcardQuery.Builder()
                        .field("uco")
                        .wildcard("*uco*").build());

        // tested method
        Page<OpportunityDto> result = searchService.searchForAll(0, 10, "uco", true, "uco", "someValue");

        // verify
        assertThat(result.getTotalElements(), equalTo(2L));
        assertThat(result.stream().toList().size(), equalTo(2));

        verify(elasticsearchClientMock, times(1)).search(requestCaptor.capture(), eq(OpportunityDto.class));
        assertThat(requestCaptor.getValue().size(), equalTo(10));
        assertThat(requestCaptor.getValue().from(), equalTo(0));

        assertThat(requestCaptor.getValue().sort().get(0).field().field(), equalTo("uco"));
        assertThat(requestCaptor.getValue().sort().get(0).field().order(), equalTo(SortOrder.Desc));
        assertThat(requestCaptor.getValue().index().get(0), equalTo(CROWDHELIX_INDEX));

        assertThat(Objects.requireNonNull(requestCaptor.getValue().query()).isMatchAll(), is(true));
        assertThat(Objects.requireNonNull(requestCaptor.getValue().postFilter()).isWildcard(), is(true));
        WildcardQuery wcq = (WildcardQuery) requestCaptor.getValue().postFilter()._get();
        assertThat(wcq.wildcard(), equalTo("*uco*"));
    }

    @Test
    void searchForAll2() throws IOException {
        // prepare
        when(queryBuilderMock.getSearchAllQuery()).thenReturn(new MatchAllQuery.Builder().build());
        when(queryBuilderMock.getFilterQuery(eq(SearchInfo.empty()))).thenReturn(new BoolQuery.Builder().build());

        // tested method
        Page<OpportunityDto> result = searchService.searchForAll(1, 10, null, false, null, null);

        // verify
        assertThat(result.getTotalElements(), equalTo(12L));
        assertThat(result.stream().toList().size(), equalTo(2));

        verify(elasticsearchClientMock, times(1)).search(requestCaptor.capture(), eq(OpportunityDto.class));
        assertThat(requestCaptor.getValue().size(), equalTo(10));
        assertThat(requestCaptor.getValue().from(), equalTo(10));

        assertThat(requestCaptor.getValue().sort(), empty());
        assertThat(requestCaptor.getValue().index().get(0), equalTo(CROWDHELIX_INDEX));
        assertThat(Objects.requireNonNull(requestCaptor.getValue().query()).isMatchAll(), is(true));
        assertThat(Objects.requireNonNull(requestCaptor.getValue().postFilter()).isBool(), is(true));
    }

    @Test
    void emptyAuthorProjects() {
        // prepare
        SearchInfo info = new SearchInfo(30, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                List.of("uco1"), List.of(1L), true, null);
        when(projectServiceMock.getByAuthorUco("uco1")).thenReturn(Collections.emptyList());

        // tested method
        Throwable exception = assertThrows(AppException.class, () -> searchService.searchByAuthors(info));

        // verify
        assertThat(exception.getMessage(), equalTo("No projects data found for the selected authors"));
    }

    @Test
    void searchMatchAllConnectionError() throws IOException {
        // prepare
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class))).thenThrow(new IOException());
        when(queryBuilderMock.getSearchAllQuery()).thenReturn(new MatchAllQuery.Builder().build());
        when(queryBuilderMock.getFilterQuery(eq("uco"), eq("someValue")))
                .thenReturn(new WildcardQuery.Builder()
                        .field("uco")
                        .wildcard("*uco*").build());

        // tested method
        Throwable exception = assertThrows(ConnectionException.class, () ->
                searchService.searchForAll(0, 10, "uco", true, "uco", "someValue"));

        // verify
        assertThat(exception.getCause(), instanceOf(IOException.class));
        assertThat(exception.getMessage(), equalTo(ELASTIC_CONNECTION_ERROR));
    }

    @Test
    void searchMatchAllElasticError() throws IOException {
        // prepare
        ErrorCause cause = ErrorCause.of(c -> c
                .type("type")
                .reason("reason"));
        ErrorResponse response = ErrorResponse.of(e -> e
                .error(cause)
                .status(404));
        when(elasticsearchClientMock.search(any(SearchRequest.class), eq(OpportunityDto.class))).thenThrow(
                new ElasticsearchException("/search", response));
        when(queryBuilderMock.getSearchAllQuery()).thenReturn(new MatchAllQuery.Builder().build());
        when(queryBuilderMock.getFilterQuery(eq("uco"), eq("someValue")))
                .thenReturn(new WildcardQuery.Builder()
                        .field("uco")
                        .wildcard("*uco*").build());

        // tested method
        Throwable exception = assertThrows(AppException.class, () ->
                searchService.searchForAll(0, 10, "uco", true, "uco", "someValue"));

        // verify
        assertThat(exception.getCause(), instanceOf(ElasticsearchException.class));
        assertThat(exception.getMessage(), equalTo("Failed to perform search or no data was found"));
    }
}