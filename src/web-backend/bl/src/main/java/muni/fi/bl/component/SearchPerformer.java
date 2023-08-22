package muni.fi.bl.component;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.exceptions.ConnectionException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.MAX_DOCS_SIZE;

@Component
@Slf4j
public class SearchPerformer<T> {

    private final ElasticsearchClient elasticsearchClient;

    public SearchPerformer(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public SearchResponse<T> getSearchResponse(Query filterQuery, Query searchQuery, String crowdhelixIndex, Class<T> documentClass) {
        return getSearchResponse(filterQuery, searchQuery, MAX_DOCS_SIZE, 0, null, crowdhelixIndex, documentClass);
    }

    public SearchResponse<T> getSearchResponse(Query filterQuery, Query searchQuery, int size, int page, SortOptions sortOptions, String crowdhelixIndex, Class<T> documentClass) {
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
}
