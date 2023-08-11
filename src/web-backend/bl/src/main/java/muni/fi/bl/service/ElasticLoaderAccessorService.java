package muni.fi.bl.service;

public interface ElasticLoaderAccessorService {

    /**
     * Loads opportunities to ElasticSearch index
     *
     * @param fileName    Source file name
     * @param data        Source file data
     * @param endpointUri Loader endpoint to send the data to (e.g. "/load")
     * @return Load response message
     * @throws muni.fi.bl.exceptions.ConnectionException When connection with Elastic fails
     */
    String sendDataToElasticLoader(String fileName, byte[] data, String endpointUri);
}
