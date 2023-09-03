package muni.fi.dtos;

/**
 * Used for error response body. Filled with exception info. Only used for translating custom exceptions
 *
 * @param status  status code
 * @param error   error message (e.g. 'bad request')
 * @param message detail message (e.g. 'ElasticSearch connection error')
 */
public record ErrorResponseDetail(int status, String error, String message) {

}
