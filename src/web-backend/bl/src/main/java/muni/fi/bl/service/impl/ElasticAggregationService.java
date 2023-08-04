package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.exceptions.ConnectionException;
import muni.fi.bl.service.AggregationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static muni.fi.bl.exceptions.ConnectionException.ELASTIC_CONNECTION_ERROR;
import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.EXPERTISE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.HELIX_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.ROLE_FIELD;

@Slf4j
@Service
public class ElasticAggregationService implements AggregationService {

    public static final int MAX_AGG_SIZE = 500;

    private final ElasticsearchClient elasticsearchClient;

    public ElasticAggregationService(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public Map<String, List<String>> searchUniqueAggAll() {
        Map<String, List<String>> filterTerms = new HashMap<>();

        filterTerms.put(ROLE_FIELD, searchUniqueAgg(ROLE_FIELD).keySet().stream().toList());
        filterTerms.put(HELIX_FIELD, searchUniqueAgg(HELIX_FIELD).keySet().stream().toList());
        filterTerms.put(EXPERTISE_FIELD, searchUniqueAgg(EXPERTISE_FIELD).keySet().stream().toList());

        return filterTerms;
    }

    private Map<String, Long> searchUniqueAgg(String field) {

        Map<String, Long> result = new LinkedHashMap<>();
        SearchResponse<Void> response;
        try {
            Aggregation agg = Aggregation.of(a -> a
                    .terms(t -> t
                            .field(field)
                            .size(MAX_AGG_SIZE)));
            SearchRequest searchRequest = SearchRequest.of(s -> s
                    .index(CROWDHELIX_INDEX)
                    .size(0)
                    .aggregations(String.format("%s-agg", field), agg
                    ));
            response = elasticsearchClient.search(searchRequest, Void.class);
        } catch (IOException e) {
            log.error(ELASTIC_CONNECTION_ERROR, e);
            throw new ConnectionException(ELASTIC_CONNECTION_ERROR, e);
        }
        List<StringTermsBucket> buckets = response.aggregations()
                .get(String.format("%s-agg", field))
                .sterms()
                .buckets().array();
        for (StringTermsBucket bucket : buckets) {
            if (!StringUtils.isBlank(bucket.key().stringValue())) {
                result.put(bucket.key().stringValue(), bucket.docCount());
            }
        }
        return result;
    }
}
