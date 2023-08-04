package muni.fi.bl.service;

import muni.fi.dtos.OpportunityDto;

import java.util.List;

public interface RecommendationService {

    /**
     * Recommends opportunities for user. Exclude projects with ids specified by excludeProjIds from the search
     *
     * @param uco            UCO of the project author to recommend opportunities for
     * @param excludeProjIds List of author's projects to be excluded from the search
     * @return The list of opportunities recommended for the author
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    List<OpportunityDto> recommendForAuthor(String uco, List<Long> excludeProjIds);

    /**
     * Recommends similar opportunities for ElasticSearch opportunity with specified id
     *
     * @param id Unique ElasticSearch identifier
     * @return The list of similar opportunities recommended for the specified opportunity id
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    List<OpportunityDto> recommendMoreLikeThis(String id);
}
