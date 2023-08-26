package muni.fi.bl.service.impl;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import lombok.extern.slf4j.Slf4j;
import muni.fi.bl.component.QueryBuilder;
import muni.fi.bl.component.SearchPerformer;
import muni.fi.bl.component.SearchResultProcessor;
import muni.fi.bl.component.TextNormalizer;
import muni.fi.bl.exceptions.AppException;
import muni.fi.bl.mappers.AuthorMapper;
import muni.fi.bl.mappers.ProjectMapper;
import muni.fi.bl.service.ProjectService;
import muni.fi.bl.service.RecommendationService;
import muni.fi.bl.service.SearchService;
import muni.fi.bl.service.enums.AuthorProjectsSortType;
import muni.fi.dal.entity.Author;
import muni.fi.dal.entity.Project;
import muni.fi.dal.repository.AuthorRepository;
import muni.fi.dal.repository.ProjectRepository;
import muni.fi.dtos.BaseEsDto;
import muni.fi.dtos.OpportunityDto;
import muni.fi.dtos.OpportunitySearchResultDto;
import muni.fi.dtos.ProjectDto;
import muni.fi.dtos.ProjectEsDto;
import muni.fi.query.SearchInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final SearchResultProcessor<OpportunityDto> opportunityResultProcessor;
    private final SearchResultProcessor<ProjectEsDto> projectResultProcessor;
    private final ProjectService projectService;
    private final RecommendationService recommendationService;
    private final QueryBuilder queryBuilder;
    private final TextNormalizer textNormalizer;
    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;
    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final SearchPerformer<OpportunityDto> opportunitySearchPerformer;
    private final SearchPerformer<ProjectEsDto> projectSearchPerformer;

    public ElasticSearchService(SearchResultProcessor<OpportunityDto> opportunityResultProcessor,
                                SearchResultProcessor<ProjectEsDto> projectResultProcessor,
                                ProjectService projectService,
                                RecommendationService recommendationService,
                                QueryBuilder queryBuilder,
                                TextNormalizer textNormalizer,
                                AuthorRepository authorRepository,
                                AuthorMapper authorMapper,
                                ProjectRepository projectRepository,
                                ProjectMapper projectMapper,
                                SearchPerformer<OpportunityDto> opportunitySearchPerformer,
                                SearchPerformer<ProjectEsDto> projectSearchPerformer) {
        this.opportunityResultProcessor = opportunityResultProcessor;
        this.projectResultProcessor = projectResultProcessor;
        this.projectService = projectService;
        this.recommendationService = recommendationService;
        this.queryBuilder = queryBuilder;
        this.textNormalizer = textNormalizer;
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.opportunitySearchPerformer = opportunitySearchPerformer;
        this.projectSearchPerformer = projectSearchPerformer;
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
        SearchResponse<OpportunityDto> searchResponse = opportunitySearchPerformer.
                getSearchResponse(filterQuery, multiMatchQuery, maxResults, 0, null, CROWDHELIX_INDEX, OpportunityDto.class);
        List<Hit<OpportunityDto>> hits = searchResponse.hits().hits();

        return getOpportunityDtosFromHits(hits);
    }

    @Override
    public Page<OpportunityDto> searchForAll(int page, int pageSize, String sortField, boolean desc, String filterField, String filterValue) {
        Query allQuery = queryBuilder.getSearchAllQuery()._toQuery();
        SortOptions sortOptions = sortField != null ? getSortOptions(sortField, desc) : null;
        Query filterQuery = getFilterQuery(filterField, filterValue);
        SearchResponse<OpportunityDto> searchResponse = opportunitySearchPerformer.
                getSearchResponse(filterQuery, allQuery, pageSize, page, sortOptions, CROWDHELIX_INDEX, OpportunityDto.class);
        List<Hit<OpportunityDto>> hits = searchResponse.hits().hits();
        PageRequest pageable = PageRequest.of(page, pageSize);
        if (sortField != null) {
            pageable.withSort(Sort.by(desc ? Sort.Direction.DESC : Sort.Direction.ASC, sortField));
        }
        long total = searchResponse.hits().total() != null ? searchResponse.hits().total().value() : 0;
        return new PageImpl<>(getOpportunityDtosFromHits(hits), pageable, total);
    }

    @Override
    public List<OpportunitySearchResultDto> searchByOpportunity(String esId, int maxResultsCount, AuthorProjectsSortType sortBy) {
        List<ProjectEsDto> relevantProjects = searchByOpportunityForProjects(esId);

        Map<String, List<ProjectEsDto>> relevantProjectsByUcoMap = relevantProjects
                .stream()
                .collect(Collectors.groupingBy(ProjectEsDto::getUco));
        Map<String, Double> authorSumScoresMap = getAuthorSumScores(relevantProjectsByUcoMap);
        Map<String, Double> authorMaxScoresMap = getAuthorsMaxScores(relevantProjectsByUcoMap);
        Map<String, Double> authorAvgScoresMap = getAuthorsAvgScores(relevantProjectsByUcoMap);
        Map<String, Double> authorProjectCountMap = getAuthorsCountScores(relevantProjectsByUcoMap);

        // Sort the authorScores map by value (score) into a LinkedHashMap
        Map<String, Double> sortedAuthorScores = switch (sortBy) {
            case SUM -> sortAuthorsByScore(authorSumScoresMap, maxResultsCount);
            case MAX -> sortAuthorsByScore(authorMaxScoresMap, maxResultsCount);
            case AVG -> sortAuthorsByScore(authorAvgScoresMap, maxResultsCount);
            case COUNT -> sortAuthorsByScore(authorProjectCountMap, maxResultsCount);
        };
        return getOpportunitySearchResultDtos(relevantProjectsByUcoMap, sortedAuthorScores,
                authorSumScoresMap, authorMaxScoresMap, authorAvgScoresMap);
    }

    private Map<String, Double> getAuthorsCountScores(Map<String, List<ProjectEsDto>> relevantProjectsByUcoMap) {
        return relevantProjectsByUcoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> Double.valueOf(entry.getValue().size())
                ));
    }

    private Map<String, Double> getAuthorsAvgScores(Map<String, List<ProjectEsDto>> relevantProjectsByUcoMap) {
        return relevantProjectsByUcoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(ProjectEsDto::getScore)
                                .average().orElse(0.0)
                ));
    }

    private Map<String, Double> getAuthorsMaxScores(Map<String, List<ProjectEsDto>> relevantProjectsByUcoMap) {
        return relevantProjectsByUcoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(ProjectEsDto::getScore)
                                .max().orElse(0.0)
                ));
    }

    private static Map<String, Double> getAuthorSumScores(Map<String, List<ProjectEsDto>> relevantProjectsByUcoMap) {
        return relevantProjectsByUcoMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .mapToDouble(ProjectEsDto::getScore)
                                .sum()
                ));
    }

    private List<OpportunitySearchResultDto> getOpportunitySearchResultDtos(
            Map<String, List<ProjectEsDto>> relevantAuthorsByUcoMap,
            Map<String, Double> sortedAuthorScoresMap,
            Map<String, Double> authorSumScoresMap,
            Map<String, Double> authorMaxScoresMap,
            Map<String, Double> authorAvgScoresMap) {

        List<OpportunitySearchResultDto> sortedResults = new ArrayList<>();
        sortedAuthorScoresMap.forEach((authorUco, score) -> {
            List<ProjectEsDto> projectEsDtos = relevantAuthorsByUcoMap.get(authorUco);
            projectEsDtos.sort(Comparator.comparingDouble(BaseEsDto::getScore).reversed());
            List<ProjectDto> projects = new LinkedList<>();
            for (var proj : projectEsDtos) {
                List<Project> projectOptional = projectRepository.findByProjId(proj.getProjId());
                Optional<Project> first = projectOptional.stream().findFirst();
                first.ifPresent(p -> {
                    ProjectDto projectDto = projectMapper.toDto(p);
                    projectDto.setScore(proj.getScore());
                    projects.add(projectDto);
                });
            }
            Optional<Author> authorOptional = authorRepository.findByUco(authorUco);
            if (authorOptional.isEmpty()) {
                throw new RuntimeException(String.format("Author with uco %s is not stored in the database.", authorUco));
            }
            sortedResults.add(
                    new OpportunitySearchResultDto(
                            authorMapper.toDto(authorOptional.get()),
                            projects,
                            authorSumScoresMap.get(authorUco),
                            authorAvgScoresMap.get(authorUco),
                            authorMaxScoresMap.get(authorUco)));
        });
        return sortedResults;
    }

    private static Map<String, Double> sortAuthorsByScore(Map<String, Double> authorScoresMap, int maxResults) {
        return authorScoresMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(maxResults)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existingValue, newValue) -> newValue,
                        LinkedHashMap::new
                ));
    }

    private Query getFilterQuery(String filterField, String filterValue) {
        Query filterQuery = filterField != null && filterValue != null
                ? queryBuilder.getFilterQuery(filterField, filterValue)._toQuery()
                : queryBuilder.getFilterQuery(SearchInfo.empty())._toQuery();
        if (filterField != null && filterValue != null && filterField.equals(TITLE_FIELD)) {
            BoolQuery.Builder boolFilterQuery = new BoolQuery.Builder();
            Query matchQuery = queryBuilder.getMatchQuery(filterField, filterValue)._toQuery();
            boolFilterQuery.should(List.of(filterQuery, matchQuery));
            filterQuery = boolFilterQuery.build()._toQuery();
        }
        return filterQuery;
    }

    private List<ProjectEsDto> searchByOpportunityForProjects(String esId) {
        Query titleSearchQuery = queryBuilder.getMoreLikeThisQuery(
                esId,
                List.of(DESCRIPTION_FIELD, TITLE_FIELD), CROWDHELIX_INDEX)._toQuery();
        Query docSearchQuery = queryBuilder.getMoreLikeThisQuery(
                esId, CROWDHELIX_INDEX)._toQuery();

        SearchResponse<ProjectEsDto> titleResponse = projectSearchPerformer.
                getSearchResponse(null, titleSearchQuery, MU_INDEX, ProjectEsDto.class);
        SearchResponse<ProjectEsDto> docResponse = projectSearchPerformer.
                getSearchResponse(null, docSearchQuery, MU_INDEX, ProjectEsDto.class);
        Map<String, List<Hit<ProjectEsDto>>> topResultsMap = getProjTopResultsMap(titleResponse, docResponse);
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

        SearchResponse<OpportunityDto> titleResponse = opportunitySearchPerformer.
                getSearchResponse(filterQuery, titleSearchQuery, CROWDHELIX_INDEX, OpportunityDto.class);
        SearchResponse<OpportunityDto> docResponse = opportunitySearchPerformer.
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

    private Map<String, List<Hit<ProjectEsDto>>> getProjTopResultsMap(SearchResponse<ProjectEsDto> titleResponse, SearchResponse<ProjectEsDto> docResponse) {
        var titleHitList = titleResponse.hits().hits();
        var docHitList = docResponse.hits().hits();

        Map<String, List<Hit<ProjectEsDto>>> topResultsMap = new HashMap<>();
        topResultsMap.put(TITLE_FIELD, titleHitList);
        topResultsMap.put(DESCRIPTION_FIELD, docHitList);
        return topResultsMap;
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
