package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.component.QueryBuilder;
import muni.fi.bl.component.SearchResultProcessor;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.service.RecommendationService;
import muni.fi.dal.repository.ProjectRepository;
import muni.fi.dtos.OpportunityDto;
import muni.fi.dtos.ProjectDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.DEFAULT_DOCS_SIZE;
import static muni.fi.bl.service.impl.ElasticSearchService.DESCRIPTION_FIELD;

@Slf4j
@Service
public class ElasticRecommendationService implements RecommendationService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper dtoMapper;
    private final ElasticsearchClient elasticsearchClient;
    private final SearchResultProcessor resultProcessor;
    private final QueryBuilder queryBuilder;

    public ElasticRecommendationService(ProjectRepository projectRepository,
                                        ProjectMapper dtoMapper,
                                        ElasticsearchClient elasticsearchClient,
                                        SearchResultProcessor resultProcessor,
                                        QueryBuilder queryBuilder) {
        this.projectRepository = projectRepository;
        this.dtoMapper = dtoMapper;
        this.elasticsearchClient = elasticsearchClient;
        this.resultProcessor = resultProcessor;
        this.queryBuilder = queryBuilder;
    }

    @Override
    public List<OpportunityDto> recommendForAuthor(String uco, List<Long> excludeProjIds) {
        List<ProjectDto> projects = projectRepository.findByAuthorUco(uco).stream()
                .filter(p -> !excludeProjIds.contains(p.getId()))
                .map(dtoMapper::toDto).toList();
        if (projects.isEmpty()) {
            return Collections.emptyList();
        }
        return searchForMoreLikeThis(queryBuilder.getMoreLikeThisQuery(
                projects.stream()
                        .map(ProjectDto::getProcessedAnnotation)
                        .toList()));
    }

    @Override
    public List<OpportunityDto> recommendMoreLikeThis(String id) {
        return searchForMoreLikeThis(queryBuilder.getMoreLikeThisQuery(id));
    }

    private List<OpportunityDto> searchForMoreLikeThis(MoreLikeThisQuery moreLikeThisQuery) {
        SearchResponse<OpportunityDto> searchResponse = getSearchResponse(moreLikeThisQuery._toQuery());
        var docHitList = searchResponse.hits().hits();
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(DESCRIPTION_FIELD, docHitList);

        return resultProcessor.aggregateResultsByScore(topResultsMap);
    }

    private SearchResponse<OpportunityDto> getSearchResponse(Query searchQuery) {
        try {
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(CROWDHELIX_INDEX)
                    .query(searchQuery)
                    .size(DEFAULT_DOCS_SIZE));
            return elasticsearchClient.search(searchRequest, OpportunityDto.class);
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        } catch (ElasticsearchException e) {
            String message = "Failed to perform search or no data was found";
            log.warn(message, e);
            throw new AppException(message, e);
        }
    }
}
