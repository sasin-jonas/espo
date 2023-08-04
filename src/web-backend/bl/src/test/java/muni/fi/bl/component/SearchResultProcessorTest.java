package muni.fi.bl.component;

import co.elastic.clients.elasticsearch.core.search.Hit;
import muni.fi.dtos.OpportunityDto;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static muni.fi.bl.component.SearchResultProcessor.TITLE_SCORE_COEFFICIENT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

class SearchResultProcessorTest {

    // tested class
    private final SearchResultProcessor resultProcessor = new SearchResultProcessor();

    @Test
    void aggregateResultsByScoreEmpty() {
        // prepare
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();

        // tested method
        var results = resultProcessor.aggregateResultsByScore(topResultsMap);

        // verify
        assertThat(results, empty());
    }

    @Test
    void aggregateResultsByScore() {
        // prepare
        Map<String, List<Hit<OpportunityDto>>> topResultsMap = new HashMap<>();
        List<Hit<OpportunityDto>> docHitList = new ArrayList<>();
        List<Hit<OpportunityDto>> titleHitList = new ArrayList<>();

        var doc1 = new OpportunityDto();
        var doc2 = new OpportunityDto();
        var title1 = new OpportunityDto();
        var title2 = new OpportunityDto();
        doc1.setId(1);
        doc2.setId(2);
        title1.setId(1);
        title2.setId(2);

        var docHit1 = new Hit.Builder<OpportunityDto>().source(doc1).score(13.0).index("any").id("any").build();
        var docHit2 = new Hit.Builder<OpportunityDto>().source(doc2).score(10.0).index("any").id("any").build();
        var titleHit1 = new Hit.Builder<OpportunityDto>().source(title1).score(2.0).index("any").id("any").build();
        var titleHit2 = new Hit.Builder<OpportunityDto>().source(title2).score(5.0).index("any").id("any").build();
        docHitList.add(docHit1);
        docHitList.add(docHit2);
        titleHitList.add(titleHit1);
        titleHitList.add(titleHit2);

        topResultsMap.put("doc", docHitList);
        topResultsMap.put("title", titleHitList);

        // tested method
        var results = resultProcessor.aggregateResultsByScore(topResultsMap);

        double resultScore1 = 13.0 + TITLE_SCORE_COEFFICIENT * 2.0;
        double resultScore2 = 10.0 + TITLE_SCORE_COEFFICIENT * 5.0;
        // verify
        assertThat("List size", results.size(), equalTo(2));
        assertThat("Order", results.get(0).getId(), equalTo(2));
        assertThat("Order", results.get(1).getId(), equalTo(1));
        assertThat("Best Score", results.get(0).getScore(), equalTo(resultScore2));
        assertThat("Second Score", results.get(1).getScore(), equalTo(resultScore1));
    }

    @Test
    void aggregateResultsAndRecommendationsEmpty() {
        // prepare
        List<OpportunityDto> searchResults = new ArrayList<>();
        List<OpportunityDto> recommendations = new ArrayList<>();

        // tested method
        var results = resultProcessor.aggregateResultsAndRecommendations(searchResults, recommendations);

        // verify
        assertThat(results, empty());
    }

    @Test
    void aggregateResultsAndRecommendations() {
        // prepare
        List<OpportunityDto> searchResults = new ArrayList<>();
        List<OpportunityDto> recommendations = new ArrayList<>();

        var doc1 = new OpportunityDto();
        var doc2 = new OpportunityDto();
        var doc3 = new OpportunityDto();
        var recommendation1 = new OpportunityDto();
        var recommendation2 = new OpportunityDto();
        var recommendation3 = new OpportunityDto();

        doc1.setId(1);
        doc1.setScore(10.0);
        doc1.setHitSource("any");
        doc2.setId(2);
        doc2.setScore(8.0);
        doc2.setHitSource("any");
        doc3.setId(3);
        doc3.setScore(7.0);
        doc3.setHitSource("any");
        recommendation1.setId(3);
        recommendation1.setScore(6.0);
        recommendation2.setId(1);
        recommendation2.setScore(2.0);
        recommendation3.setId(4);
        recommendation3.setScore(8.0);

        searchResults.add(doc1);
        searchResults.add(doc2);
        searchResults.add(doc3);
        recommendations.add(recommendation1);
        recommendations.add(recommendation2);
        recommendations.add(recommendation3);

        // tested method
        var results = resultProcessor.aggregateResultsAndRecommendations(searchResults, recommendations);

        // verify
        assertThat("List size", results.size(), equalTo(3));
        assertThat("Order", results.get(0).getId(), equalTo(3));
        assertThat("Order", results.get(1).getId(), equalTo(1));
        assertThat("Order", results.get(2).getId(), equalTo(2));
        assertThat("Best Score", results.get(0).getScore(), equalTo(13.0));
        assertThat("Second Score", results.get(1).getScore(), equalTo(12.0));
        assertThat("Third Score", results.get(2).getScore(), equalTo(8.0));
    }
}