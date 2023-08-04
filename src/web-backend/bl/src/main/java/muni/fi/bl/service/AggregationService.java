package muni.fi.bl.service;

import java.util.List;
import java.util.Map;

public interface AggregationService {

    /**
     * Searches for unique filter-terms in certain ElasticSearch index fields (helix, role, expertise)
     *
     * @return Map of the found unique terms sorted by descending number occurrences (e.g.: key-"helix", list-"health", "digital", "covid-19")
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    Map<String, List<String>> searchUniqueAggAll();
}
