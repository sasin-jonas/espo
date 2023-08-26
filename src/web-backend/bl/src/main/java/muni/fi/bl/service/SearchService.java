package muni.fi.bl.service;

import muni.fi.bl.service.enums.AuthorProjectsSortType;
import muni.fi.dtos.OpportunityDto;
import muni.fi.dtos.OpportunitySearchResultDto;
import muni.fi.query.SearchInfo;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SearchService {

    /**
     * Search for opportunities based on the specified projects
     *
     * @param info The search information. The 'ucoList' property is ignored
     * @return List of opportunities found and filtered based on the search info
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    List<OpportunityDto> searchByProjects(SearchInfo info);

    /**
     * Search for the opportunities based on the specified project authors
     *
     * @param info The search information.
     * @return List of opportunities found and filtered based on the search info
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    List<OpportunityDto> searchByAuthors(SearchInfo info);

    /**
     * Search for opportunities based on a phrase. The search is performed among the title and description fields
     *
     * @param info The search information. Only maxResults, helixes, roles, expertise and phrase parameters are taken into account
     * @return List of opportunities found and filtered based on the search info and search phrase
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    List<OpportunityDto> searchByPhrase(SearchInfo info);

    /**
     * Search by opportunity for relevant authors and their projects
     */
    List<OpportunitySearchResultDto> searchByOpportunity(String opportunityId, int maxResults, AuthorProjectsSortType sortBy);

    /**
     * Searches for opportunities based on input params. Is used for filtering, paging and sorting
     *
     * @param page        Page number (starting from 0)
     * @param pageSize    Page size
     * @param sortField   Elastic field to sort by
     * @param desc        If true, sorts by 'sortField' in descending order, otherwise sorts ascending
     * @param filterField Elastic field to filter by
     * @param filterValue The value that the 'filterField' needs to contain
     * @return The Page of opportunities based on the input parameters
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    Page<OpportunityDto> searchForAll(int page, int pageSize, String sortField, boolean desc, String filterField, String filterValue);
}
