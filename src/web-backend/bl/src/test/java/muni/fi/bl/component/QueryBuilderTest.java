package muni.fi.bl.component;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchAllQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.WildcardQuery;
import muni.fi.bl.config.FilesConfigProperties;
import muni.fi.query.SearchInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static muni.fi.bl.service.impl.ElasticSearchService.CROWDHELIX_INDEX;
import static muni.fi.bl.service.impl.ElasticSearchService.DESCRIPTION_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.EXPERTISE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.HELIX_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.ROLE_FIELD;
import static muni.fi.bl.service.impl.ElasticSearchService.TITLE_FIELD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class QueryBuilderTest {

    @Mock
    private StopWordsReader stopWordsReaderMock;
    @Mock
    private FilesConfigProperties filesConfigPropertiesMock;

    // tested class
    private QueryBuilder queryBuilder;

    // shared testing properties
    private List<String> docs;

    @BeforeEach
    void setUp() throws IOException {
        openMocks(this);

        when(stopWordsReaderMock.loadStopWords(any())).thenReturn(Collections.emptyList());
        when(filesConfigPropertiesMock.getStopWords()).thenReturn("testStopwords.txt");

        queryBuilder = new QueryBuilder(stopWordsReaderMock, filesConfigPropertiesMock);

        docs = List.of("doc1", "doc2");
    }

    @Test
    void getMoreLikeThisQueryForDocList() {
        // tested method
        MoreLikeThisQuery moreLikeThisQuery = queryBuilder.getMoreLikeThisQuery(docs);

        // verify
        String likeDoc1 = "{\"description\":\"doc1\"}";
        String likeDoc2 = "{\"description\":\"doc2\"}";

        assertThat("Query has like docs", moreLikeThisQuery.like().size(), equalTo(2));
        assertThat("Query has correct field", moreLikeThisQuery.fields(), equalTo(List.of(DESCRIPTION_FIELD)));
        assertThat("LikeDocument1 has non-null data", moreLikeThisQuery.like().get(0).document().doc(), notNullValue());
        assertThat("LikeDocument2 has non-null data", moreLikeThisQuery.like().get(1).document().doc(), notNullValue());
        assertThat("LikeDocument1 json value", Objects.requireNonNull(moreLikeThisQuery.like().get(0).document().doc()).toJson().toString(), equalTo(likeDoc1));
        assertThat("LikeDocument2 json value", Objects.requireNonNull(moreLikeThisQuery.like().get(1).document().doc()).toJson().toString(), equalTo(likeDoc2));
    }

    @Test
    void getMoreLikeThisQueryForDocsAndFields() {
        // prepare
        List<String> fields = List.of(DESCRIPTION_FIELD, TITLE_FIELD);

        // tested method
        MoreLikeThisQuery moreLikeThisQuery = queryBuilder.getMoreLikeThisQuery(docs, fields);

        // verify
        String likeDoc1 = "{\"description\":\"doc1\"}";
        String likeDoc2 = "{\"description\":\"doc2\"}";

        assertThat("Query has like docs", moreLikeThisQuery.like().size(), equalTo(2));
        assertThat("Query has correct fields", moreLikeThisQuery.fields(), equalTo(List.of(DESCRIPTION_FIELD, TITLE_FIELD)));
        assertThat("LikeDocument1 has non-null data", moreLikeThisQuery.like().get(0).document().doc(), notNullValue());
        assertThat("LikeDocument2 has non-null data", moreLikeThisQuery.like().get(1).document().doc(), notNullValue());
        assertThat("LikeDocument1 json value", Objects.requireNonNull(moreLikeThisQuery.like().get(0).document().doc()).toJson().toString(), equalTo(likeDoc1));
        assertThat("LikeDocument2 json value", Objects.requireNonNull(moreLikeThisQuery.like().get(1).document().doc()).toJson().toString(), equalTo(likeDoc2));
    }

    @Test
    void getMoreLikeThisQueryForElasticDoc() {
        // prepare
        String elasticId = "someId";

        // tested method
        MoreLikeThisQuery moreLikeThisQuery = queryBuilder.getMoreLikeThisQuery(elasticId, CROWDHELIX_INDEX);

        // verify
        assertThat("Query has like docs", moreLikeThisQuery.like().size(), equalTo(1));
        assertThat("Query has correct fields", moreLikeThisQuery.fields(), equalTo(List.of(DESCRIPTION_FIELD, TITLE_FIELD)));
        assertThat("Query has correct index", moreLikeThisQuery.like().get(0).document().index(), equalTo(CROWDHELIX_INDEX));
        assertThat("LikeDocument has non-null id", moreLikeThisQuery.like().get(0).document().id(), notNullValue());
        assertThat("LikeDocument has non-null id", moreLikeThisQuery.like().get(0).document().id(), equalTo(elasticId));
    }

    @Test
    void getMultiMatchQuery() {
        // prepare
        String phrase = "somePhrase";

        // tested method
        MultiMatchQuery multiMatchQuery = queryBuilder.getMultiMatchQuery(phrase);

        // verify
        assertThat("Phrase match", multiMatchQuery.query(), equalTo(phrase));
        assertThat("Fields", multiMatchQuery.fields().size(), equalTo(2));
        assertThat("Boosted field", multiMatchQuery.fields().get(0), equalTo("title^2"));
        assertThat("Description field", multiMatchQuery.fields().get(1), equalTo(DESCRIPTION_FIELD));
    }

    @Test
    void getFilterQuery() {
        // prepare
        List<String> helixes = List.of("health", "digital");
        List<String> roles = List.of("expert");
        List<String> expertises = List.of("data analytics", "big data", "something");
        SearchInfo searchInfo = new SearchInfo(null,
                helixes, roles, expertises,
                null, null, false, null);

        // tested method
        BoolQuery boolQuery = queryBuilder.getFilterQuery(searchInfo);

        // verify
        assertThat("Filter has 'must' clauses", boolQuery.must().size(), equalTo(3));
        assertThat("Filter has 'terms' clause", boolQuery.must().get(0).isTerms(), is(true));
        assertThat("Filter has 'terms' clause", boolQuery.must().get(1).isTerms(), is(true));
        assertThat("Filter has 'terms' clause", boolQuery.must().get(2).isTerms(), is(true));

        // helper variables
        TermsQuery termsQuery1 = (TermsQuery) boolQuery.must().get(0)._get();
        TermsQuery termsQuery2 = (TermsQuery) boolQuery.must().get(1)._get();
        TermsQuery termsQuery3 = (TermsQuery) boolQuery.must().get(2)._get();

        assertThat("Filter1 field", termsQuery1.field(), equalTo(HELIX_FIELD));
        assertThat("Filter2 field", termsQuery2.field(), equalTo(ROLE_FIELD));
        assertThat("Filter3 field", termsQuery3.field(), equalTo(EXPERTISE_FIELD));

        assertThat("Filter1 has terms size", termsQuery1.terms().value().size(), equalTo(2));
        assertThat("Filter2 has terms size", termsQuery2.terms().value().size(), equalTo(1));
        assertThat("Filter3 has terms size", termsQuery3.terms().value().size(), equalTo(3));

        // termsQuery 1 term values
        assertThat(termsQuery1.terms().value().get(0).stringValue(), equalTo("health"));
        assertThat(termsQuery1.terms().value().get(1).stringValue(), equalTo("digital"));
        // termsQuery 2 term values
        assertThat(termsQuery2.terms().value().get(0).stringValue(), equalTo("expert"));
        // termsQuery 3 term values
        assertThat(termsQuery3.terms().value().get(0).stringValue(), equalTo("data analytics"));
        assertThat(termsQuery3.terms().value().get(1).stringValue(), equalTo("big data"));
        assertThat(termsQuery3.terms().value().get(2).stringValue(), equalTo("something"));
    }

    @Test
    void getEmptyFilterQuery() {
        // prepare
        List<String> helixes = List.of();
        SearchInfo searchInfo = new SearchInfo(null,
                helixes, null, null,
                null, null, false, null);

        // tested method
        BoolQuery boolQuery = queryBuilder.getFilterQuery(searchInfo);

        // verify
        assertThat("Filter is empty", boolQuery.must().size(), equalTo(0));
    }

    @Test
    void getWildcardFilterQuery() {
        // prepare
        String field = "title";
        String value = "value";

        // tested method
        WildcardQuery wildcardQuery = queryBuilder.getFilterQuery(field, value);

        // verify
        assertThat("Filter field", wildcardQuery.field(), equalTo(field));
        assertThat("Wildcard value", wildcardQuery.wildcard(), equalTo("*value*"));
        assertThat("Case sensitivity", wildcardQuery.caseInsensitive(), is(true));
    }

    @Test
    void getSearchAllQuery() {
        // tested method
        MatchAllQuery allQuery = queryBuilder.getSearchAllQuery();

        // verify
        assertThat(allQuery._queryKind(), equalTo(Query.Kind.MatchAll));
    }

    @Test
    void getMatchQuery() {
        String somePhrase = "somePhrase";

        // tested method
        MatchQuery matchQuery = queryBuilder.getMatchQuery(HELIX_FIELD, somePhrase);

        // verify
        assertThat(matchQuery._queryKind(), equalTo(Query.Kind.Match));
        assertThat(matchQuery.field(), equalTo(HELIX_FIELD));
        assertThat(matchQuery.query().stringValue(), equalTo(somePhrase));
    }
}
