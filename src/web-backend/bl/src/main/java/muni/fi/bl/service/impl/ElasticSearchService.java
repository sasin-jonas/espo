package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.component.QueryBuilder;
import muni.fi.bl.component.SearchResultProcessor;
import muni.fi.bl.component.TextNormalizer;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.service.ProjectService;
import muni.fi.bl.service.RecommendationService;
import muni.fi.bl.service.SearchService;
import muni.fi.dtos.OpportunityDto;
import muni.fi.dtos.ProjectDto;
import muni.fi.query.SearchInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;

@Service
@Slf4j
public class ElasticSearchService implements SearchService {

    public static final String CROWDHELIX_INDEX = "crowdhelix_data";
    public static final String MU_INDEX = "mu_data";
    public static final String DESCRIPTION_FIELD = "description";
    public static final String TITLE_FIELD = "title";
    public static final String HELIX_FIELD = "helix";
    public static final String ROLE_FIELD = "role";
    public static final String EXPERTISE_FIELD = "expertise";

    public static final int MAX_DOCS_SIZE = 100;
    public static final int DEFAULT_DOCS_SIZE = 20;

    // adjusting some MLT query defaults
    public static final int MAX_QUERY_TERMS = 50;
    public static final int MIN_DOC_FREQ = 1;
    public static final int MIN_TERM_FREQ = 1;
    public static final String MINIMUM_TERMS_MATCH = "10%";

    private final ElasticsearchClient elasticsearchClient;
    private final SearchResultProcessor<OpportunityDto> opportunityResultProcessor;
    private final SearchResultProcessor<ProjectDto> projectResultProcessor;
    private final ProjectService projectService;
    private final RecommendationService recommendationService;
    private final QueryBuilder queryBuilder;
    private final TextNormalizer textNormalizer;

    public ElasticSearchService(ElasticsearchClient elasticsearchClient,
                                SearchResultProcessor<OpportunityDto> opportunityResultProcessor,
                                SearchResultProcessor<ProjectDto> projectResultProcessor,
                                ProjectService projectService,
                                RecommendationService recommendationService,
                                QueryBuilder queryBuilder,
                                TextNormalizer textNormalizer) {
        this.elasticsearchClient = elasticsearchClient;
        this.opportunityResultProcessor = opportunityResultProcessor;
        this.projectResultProcessor = projectResultProcessor;
        this.projectService = projectService;
        this.recommendationService = recommendationService;
        this.queryBuilder = queryBuilder;
        this.textNormalizer = textNormalizer;
    }

    @Override
    public List<OpportunityDto> searchByProjects(SearchInfo info) {
        List<ProjectDto> projects = info.projIds().stream()
                .map(projectService::getById)
                .toList();

        List<OpportunityDto> topResultsForProjects = search(info, projects);
        if (info.personalized()) {
            List<OpportunityDto> recommendedForUsers = new ArrayList<>();
            Set<String> authorUcos = projects.stream()
                    .map(p -> p.getAuthor().getUco())
                    .collect(Collectors.toSet());
            for (var uco : authorUcos) {
                recommendedForUsers.addAll(recommendationService.recommendForAuthor(uco, info.projIds()));
            }
            topResultsForProjects = opportunityResultProcessor.aggregateResultsAndRecommendations(topResultsForProjects, recommendedForUsers);
        }
        return topResultsForProjects.stream()
                .limit(info.maxResults() == null ? DEFAULT_DOCS_SIZE : info.maxResults())
                .toList();
    }

    @Override
    public List<OpportunityDto> searchByAuthors(SearchInfo info) {
        List<ProjectDto> projects = info.ucoList().stream()
                .map(projectService::getByAuthorUco)
                .flatMap(Collection::stream)
                .toList();
        if (projects.isEmpty()) {
            String message = "No projects data found for the selected authors";
            log.warn(message);
            throw new AppException(message);
        }

        List<OpportunityDto> topResultsForAuthors = search(info, projects);
        if (!CollectionUtils.isEmpty(info.projIds())) {
            List<ProjectDto> additionalProjects = info.projIds().stream()
                    .map(projectService::getById)
                    .toList();
            List<OpportunityDto> topResultsForProjects = search(info, additionalProjects);
            topResultsForAuthors = opportunityResultProcessor.aggregateResultsAndRecommendations(topResultsForAuthors, topResultsForProjects);
        }
        return topResultsForAuthors.stream()
                .limit(info.maxResults() == null ? DEFAULT_DOCS_SIZE : info.maxResults())
                .toList();
    }

    @Override
    public List<OpportunityDto> searchByPhrase(SearchInfo info) {
        Query filterQuery = queryBuilder.getFilterQuery(info)._toQuery();
        Query multiMatchQuery = queryBuilder.getMultiMatchQuery(
                textNormalizer.normalize(info.phrase())
        )._toQuery();

        int maxResults = info.maxResults() == null ? DEFAULT_DOCS_SIZE : info.maxResults();
        SearchResponse<OpportunityDto> searchResponse = (SearchResponse<OpportunityDto>)
                getSearchResponse(filterQuery, multiMatchQuery, maxResults, 0, null, CROWDHELIX_INDEX, OpportunityDto.class);
        List<Hit<OpportunityDto>> hits = searchResponse.hits().hits();

        return getOpportunityDtosFromHits(hits);
    }

    @Override
    public Page<OpportunityDto> searchForAll(int page, int pageSize, String sortField, boolean desc, String filterField, String filterValue) {
        Query allQuery = queryBuilder.getSearchAllQuery()._toQuery();
        SortOptions sortOptions = sortField != null ? getSortOptions(sortField, desc) : null;
        Query filterQuery = filterField != null && filterValue != null
                ? queryBuilder.getFilterQuery(filterField, filterValue)._toQuery()
                : queryBuilder.getFilterQuery(SearchInfo.empty())._toQuery();
        SearchResponse<OpportunityDto> searchResponse = (SearchResponse<OpportunityDto>)
                getSearchResponse(filterQuery, allQuery, pageSize, page, sortOptions, CROWDHELIX_INDEX, OpportunityDto.class);
        List<Hit<OpportunityDto>> hits = searchResponse.hits().hits();
        PageRequest pageable = PageRequest.of(page, pageSize);
        if (sortField != null) {
            pageable.withSort(Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, sortField));
        }
        long total = searchResponse.hits().total() != null ? searchResponse.hits().total().value() : 0;
        return new PageImpl<>(getOpportunityDtosFromHits(hits), pageable, total);
    }

    private List<ProjectDto> searchByOpportunity(String esId) {
        Query titleSearchQuery = queryBuilder.getMoreLikeThisQuery(
                esId,
                List.of(DESCRIPTION_FIELD, TITLE_FIELD), MU_INDEX)._toQuery();
        Query docSearchQuery = queryBuilder.getMoreLikeThisQuery(
                esId, MU_INDEX)._toQuery();

        SearchResponse<ProjectDto> titleResponse = (SearchResponse<ProjectDto>)
                getSearchResponse(null, titleSearchQuery, MU_INDEX, ProjectDto.class);
        SearchResponse<ProjectDto> docResponse = (SearchResponse<ProjectDto>)
                getSearchResponse(null, docSearchQuery, MU_INDEX, ProjectDto.class);
        Map<String, List<Hit<ProjectDto>>> topResultsMap = getProjTopResultsMap(titleResponse, docResponse);
        return projectResultProcessor.aggregateResultsByScore(topResultsMap);
    }

    private List<OpportunityDto> search(SearchInfo filterInfo, List<ProjectDto> projects) {
        Query filterQuery = queryBuilder.getFilterQuery(filterInfo)._toQuery();
        Query titleSearchQuery = queryBuilder.getMoreLikeThisQuery(
                projects.stream()
                        .map(ProjectDto::getTitle)
                        .toList(),
                List.of(DESCRIPTION_FIELD, TITLE_FIELD))._toQuery();
        Query docSearchQuery = queryBuilder.getMoreLikeThisQuery(
                projects.stream()
                        .map(ProjectDto::getProcessedAnnotation)
                        .toList())._toQuery();

        SearchResponse<OpportunityDto> titleResponse = (SearchResponse<OpportunityDto>)
                getSearchResponse(filterQuery, titleSearchQuery, CROWDHELIX_INDEX, OpportunityDto.class);
        SearchResponse<OpportunityDto> docResponse = (SearchResponse<OpportunityDto>)
                getSearchResponse(filterQuery, docSearchQuery, CROWDHELIX_INDEX, OpportunityDto.class);
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = getOppTopResultsMap(titleResponse, docResponse);

        return opportunityResultProcessor.aggregateResultsByScore(topResultsMap);
    }

    private Map<String, List<Hit<OpportunityDto>>> getOppTopResultsMap(SearchResponse<OpportunityDto> titleResponse, SearchResponse<OpportunityDto> docResponse) {
        var titleHitList = titleResponse.hits().hits();
        var docHitList = docResponse.hits().hits();

        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(TITLE_FIELD, titleHitList);
        topResultsMap.put(DESCRIPTION_FIELD, docHitList);
        return topResultsMap;
    }

    private Map<String, List<Hit<ProjectDto>>> getProjTopResultsMap(SearchResponse<ProjectDto> titleResponse, SearchResponse<ProjectDto> docResponse) {
        var titleHitList = titleResponse.hits().hits();
        var docHitList = docResponse.hits().hits();

        Map<String, List<Hit<ProjectDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(TITLE_FIELD, titleHitList);
        topResultsMap.put(DESCRIPTION_FIELD, docHitList);
        return topResultsMap;
    }

    private SearchResponse<?> getSearchResponse(Query filterQuery, Query searchQuery, String crowdhelixIndex, Class<?> documentClass) {
        return getSearchResponse(filterQuery, searchQuery, MAX_DOCS_SIZE, 0, null, crowdhelixIndex, documentClass);
    }

    private SearchResponse<?> getSearchResponse(Query filterQuery, Query searchQuery, int size, int page, SortOptions sortOptions, String crowdhelixIndex, Class<?> documentClass) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(crowdhelixIndex)
                    .query(searchQuery)
                    .size(size)
                    .from(size * page)
                    .sort(sortOptions != null ? List.of(sortOptions) : Collections.emptyList())
                    .postFilter(filterQuery));
            return elasticsearchClient.search(searchRequest, documentClass);
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        } catch (ElasticsearchException e) {
            String message = "Failed to perform search or no data was found";
            log.warn(message, e);
            throw new AppException(message, e);
        }
    }

    private SortOptions getSortOptions(String sortField, boolean desc) {
        return new SortOptions.Builder()
                .field(b -> b
                        .field(sortField)
                        .order(desc ? SortOrder.Desc : SortOrder.Asc))
                .build();
    }

    private List<OpportunityDto> getOpportunityDtosFromHits(List<Hit<OpportunityDto>> hits) {
        List<OpportunityDto> opportunityDtos = new ArrayList<>();
        hits.forEach(h -> {
            OpportunityDto source = h.source();
            if (source != null) {
                source.setEsId(h.id());
                source.setScore(h.score());
                opportunityDtos.add(source);
            }
        });
        return opportunityDtos;
    }
}
