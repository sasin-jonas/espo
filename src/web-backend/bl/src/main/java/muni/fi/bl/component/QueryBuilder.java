package muni.fi.bl.component;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Like;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import co.elastic.clients.json.JsonData;
import muni.fi.bl.config.FilesConfigProperties;
import muni.fi.query.SearchInfo;
import org.apache.commons.lang3.Validate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.DESCRIPTION_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.EXPERTISE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.HELIX_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.MAX_QUERY_TERMS;
import static muni.fi.bl.service.impl.ElasticSearchService.MINIMUM_TERMS_MATCH;
import static muni.fi.bl.service.impl.ElasticSearchService.MIN_DOC_FREQ;
import static muni.fi.bl.service.impl.ElasticSearchService.MIN_TERM_FREQ;
import static muni.fi.bl.service.impl.ElasticSearchService.ROLE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.TITLE_FIELD;

/**
 * Used for building the ElasticSearch queries based on some simple parameters
 */
@Component
public class QueryBuilder {

    private final List<String> stopWords;

    public QueryBuilder(StopWordsReader stopWordsReader,
                        FilesConfigProperties filesProperties) throws IOException {
        ClassPathResource resource = new ClassPathResource(filesProperties.getStopWords());
        InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
        this.stopWords = stopWordsReader.loadStopWords(inputStreamReader);
    }

    /**
     * Creates a moreLikeThisQuery with list of 'like' documents as parameter.
     * The query is performed among the 'description' field
     *
     * @param docs The documents which you want to find similar results for
     * @return The constructed instance of MoreLikeThis query
     */
    public MoreLikeThisQuery getMoreLikeThisQuery(List<String> docs) {
        return getMoreLikeThisQuery(docs, List.of(DESCRIPTION_FIELD));
    }

    /**
     * Creates a moreLikeThisQuery with list of 'like' documents as parameter searching among the specified fields
     *
     * @param docs   The documents which you want to find similar results for
     * @param fields The field of the index among which you want to perform the search
     * @return The constructed instance of MoreLikeThis query
     */
    public MoreLikeThisQuery getMoreLikeThisQuery(List<String> docs, List<String> fields) {
        Validate.isTrue(!CollectionUtils.isEmpty(docs), "Can't find results for empty query");
        return MoreLikeThisQuery.of(m -> m
                .fields(fields)
                .like(
                        getLikeDocs(docs)
                )
                .maxQueryTerms(MAX_QUERY_TERMS)
                .minDocFreq(MIN_DOC_FREQ)
                .minTermFreq(MIN_TERM_FREQ)
                .minimumShouldMatch(MINIMUM_TERMS_MATCH)
                .stopWords(stopWords));
    }

    /**
     * Creates a moreLikeThisQuery with an elastic document as parameter. The search is performed among 'title'
     * and 'description' fields
     *
     * @param id    The unique identifier of the document in ElasticSearch
     * @param index The name of the ElasticSearch index to search in
     * @return The constructed instance of MoreLikeThis query
     */
    public MoreLikeThisQuery getMoreLikeThisQuery(String id, String index) {
        return getMoreLikeThisQuery(id, List.of(DESCRIPTION_FIELD, TITLE_FIELD), index);
    }

    /**
     * Creates a moreLikeThisQuery with an elastic document as parameter. The search is performed among 'title'
     * and 'description' fields
     *
     * @param index The name of the ElasticSearch index to search in
     * @param id    The unique identifier of the document in ElasticSearch
     * @return The constructed instance of MoreLikeThis query
     */
    public MoreLikeThisQuery getMoreLikeThisQuery(String id, List<String> fields, String index) {
        return MoreLikeThisQuery.of(m -> m
                .fields(fields)
                .like(
                        l -> l.document(d -> d
                                .index(index)
                                .id(id))
                )
                .maxQueryTerms(MAX_QUERY_TERMS)
                .minDocFreq(MIN_DOC_FREQ)
                .minTermFreq(MIN_TERM_FREQ)
                .minimumShouldMatch(MINIMUM_TERMS_MATCH)
                .stopWords(stopWords));
    }

    /**
     * Created a multiMatch query for performing full-text search among both title and description fields.
     * The title field is boosted.
     *
     * @param matchPhrase The phrase to search for in the index
     * @return The constructed instance of MultiMatch query
     */
    public MultiMatchQuery getMultiMatchQuery(String matchPhrase) {
        return MultiMatchQuery.of(m -> m
                .fields(String.format("%s^2", TITLE_FIELD), DESCRIPTION_FIELD)
                .query(matchPhrase));
    }

    /**
     * Creates a boolQuery that serves as a filter query for 'helix', 'role', and 'expertise' fields.
     * For each of the fields, response must contain at least one of the specified terms
     *
     * @param info SearchInfo instance. Only 'helixes', 'roles', and 'expertises' properties are relevant
     * @return A constructed BoolQuery instance usable for filtering the search results
     */
    public BoolQuery getFilterQuery(SearchInfo info) {
        BoolQuery.Builder filterQuery = new BoolQuery.Builder();
        List<Query> filterQueries = new ArrayList<>();
        if (!CollectionUtils.isEmpty(info.helixes())) {
            filterQueries.add(buildTermsQueryForField(HELIX_FIELD, info.helixes())._toQuery());
        }
        if (!CollectionUtils.isEmpty(info.roles())) {
            filterQueries.add(buildTermsQueryForField(ROLE_FIELD, info.roles())._toQuery());
        }
        if (!CollectionUtils.isEmpty(info.expertises())) {
            filterQueries.add(buildTermsQueryForField(EXPERTISE_FIELD, info.expertises())._toQuery());
        }
        if (!filterQueries.isEmpty()) {
            filterQuery.must(filterQueries);
        }
        return filterQuery.build();
    }

    /**
     * Creates a wildCardQuery which matches all documents containing the phrase 'searchValue' in their 'searchField'
     *
     * @param searchField The field which value has to contain the substring 'searchValue'
     * @param searchValue The substring you are looking for in the 'searchField'
     * @return The constructed WildcardQuery instance usable for filtering the search results
     */
    public WildcardQuery getFilterQuery(String searchField, String searchValue) {
        return WildcardQuery.of(w -> w
                .field(searchField)
                .wildcard(String.format("*%s*", searchValue))
                .caseInsensitive(true));
    }

    /**
     * Builds a query that matches all the documents in the index
     *
     * @return The constructed MatchAllQuery instance
     */
    public MatchAllQuery getSearchAllQuery() {
        return MatchAllQuery.of(q -> q);
    }

    private TermsQuery buildTermsQueryForField(String field, List<String> filterKeywords) {
        TermsQuery.Builder termsQueryBuilder = new TermsQuery.Builder();
        if (filterKeywords != null) {
            TermsQueryField termsQueryField = TermsQueryField.of(t -> t
                    .value(filterKeywords
                            .stream()
                            .map(FieldValue::of)
                            .toList()));
            termsQueryBuilder
                    .field(field)
                    .terms(termsQueryField);
        }
        return termsQueryBuilder.build();
    }

    private List<Like> getLikeDocs(List<String> docs) {
        List<Like> likeDocs = new ArrayList<>();

        for (var doc : docs) {
            var like = Like.of(l -> l
                    .document(ld -> ld
                            .index(CROWDHELIX_INDEX)
                            .doc(JsonData.fromJson("""
                                    {
                                        "%s": "%s"
                                    }
                                    """.formatted(DESCRIPTION_FIELD, doc.replaceAll("\\s+", " "))))
                    ));
            likeDocs.add(like);
        }

        return likeDocs;
    }
}
