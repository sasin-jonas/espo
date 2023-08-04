package muni.fi.bl.component;

import co.elastic.clients.elasticsearch.core.search.Hit;
import muni.fi.dtos.OpportunityDto;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static muni.fi.bl.service.impl.ElasticSearchService.TITLE_FIELD;

/**
 * Used for aggregating and processing the ElasticSearch search results
 */
@Component
public class SearchResultProcessor {

    public static final int TITLE_SCORE_COEFFICIENT = 2;

    /**
     * Aggregates the results with same ids acquired by searches performed on doc and title field.
     * Results acquired from searches using the 'title' field have the 'title' key,
     * other key values are not checked.
     *
     * @param topResultsMap map of result lists acquired by searches
     * @return list of opportunities ranked by the aggregated score
     */
    public List<OpportunityDto> aggregateResultsByScore(Map<String, List<Hit<OpportunityDto>>> topResultsMap) {
        List<OpportunityDto> topDocs = getHitsWithScoreAndSource(topResultsMap);

        Set<OpportunityDto> topHitsAgg = new HashSet<>();
        for (var opportunity : topDocs) {
            var foundOpt = topHitsAgg.stream()
                    .filter(h ->
                            h.getId().equals(opportunity.getId()))
                    .findFirst();
            if (foundOpt.isPresent()) {
                var found = foundOpt.get();
                found.setScore(found.getScore() + opportunity.getScore());
                found.setHitSource("doc and title");
            } else {
                topHitsAgg.add(opportunity);
            }
        }
        return sortAndRank(topHitsAgg.stream().toList());
    }

    /**
     * Aggregates search results and recommendations with same ids. The recommendations (hints) can only 'boot' existing
     * search results, resulting in changing the final order, but will not appear as new ones.
     *
     * @param topResults  the list of search results
     * @param searchHints the list of recommendations (hints) to support result ordering
     * @return list of opportunities reordered and ranked based on the aggregated score
     */
    public List<OpportunityDto> aggregateResultsAndRecommendations(List<OpportunityDto> topResults, List<OpportunityDto> searchHints) {
        for (var result : topResults) {
            for (var recommendation : searchHints) {
                if (result.getId().equals(recommendation.getId())) {
                    result.setScore(result.getScore() + recommendation.getScore());
                    result.setHitSource(result.getHitSource() + " (+recommendation)");
                }
            }
        }
        return sortAndRank(topResults);
    }

    private List<OpportunityDto> getHitsWithScoreAndSource(Map<String, List<Hit<OpportunityDto>>> topResultsMap) {
        List<OpportunityDto> topHits = new ArrayList<>();
        for (var entry : topResultsMap.entrySet()) {
            for (var hit : entry.getValue()) {
                OpportunityDto doc = hit.source();
                if (doc != null && hit.score() != null) {
                    if (entry.getKey().equals(TITLE_FIELD)) {
                        doc.setScore(hit.score() * TITLE_SCORE_COEFFICIENT);
                    } else {
                        doc.setScore(hit.score());
                    }
                    doc.setHitSource(entry.getKey());
                    doc.setEsId(hit.id());
                }
                topHits.add(doc);
            }
        }
        return topHits;
    }

    private List<OpportunityDto> sortAndRank(List<OpportunityDto> topHitsAgg) {
        List<OpportunityDto> topResultsSorted = topHitsAgg.stream()
                .filter(h -> h.getScore() != null)
                .sorted((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()))
                .toList();
        for (int i = 0; i < topResultsSorted.size(); i++) {
            topResultsSorted.get(i).setRank(i + 1);
        }
        return topResultsSorted;
    }
}
