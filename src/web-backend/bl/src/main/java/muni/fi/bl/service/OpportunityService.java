package muni.fi.bl.service;

public interface OpportunityService {

    /**
     * Deletes opportunity document from ElasticSearch index
     *
     * @param id The unique ElasticSearch identifier
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     * @throws muni.fi.bl.exceptions.NotFoundException   When document with id couldn't be found
     */
    void delete(String id);

    /**
     * Deletes ElasticSearch index containing all opportunities
     *
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    void deleteAll();

    /**
     * Loads opportunities to ElasticSearch index
     *
     * @param fileName Source file name
     * @param data     Source file data
     * @return Load response message
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    String load(String fileName, byte[] data);

    /**
     * Retrieves expected file format for opportunities loading
     *
     * @return An example CSV content as string
     */
    String getSampleCsvContent();
}
