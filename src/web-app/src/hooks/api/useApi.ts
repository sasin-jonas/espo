import axios from 'axios';

/**
 * Get objects from the API
 * @param url The url to get the objects from
 * @param token Bearer token to authenticate with
 */
export const getObjects = async <T>(url: string, token: string) =>
	axios.get<T>(url, { headers: { Authorization: `Bearer ${token}` } });

/**
 * Get blob from the API
 * @param url The url to get the blob from
 * @param token Bearer token to authenticate with
 */
export const getBlob = async <T>(url: string, token: string) =>
	axios.get<T>(url, {
		headers: { Authorization: `Bearer ${token}` },
		responseType: 'blob'
	});

/**
 * Get objects from the API with query params
 * @param url The url to get the objects from
 * @param token Bearer token to authenticate with
 * @param params The query params to send
 */
export const getObjectsQueryParams = async <T>(
	url: string,
	token: string,
	params: any
) =>
	axios.get<T>(url, {
		headers: { Authorization: `Bearer ${token}` },
		params
	});

/**
 * Post object to the API
 * @param url The url to post the object to
 * @param obj The object to post
 * @param token Bearer token to authenticate with
 */
export const postObject = async <Response, Request>(
	url: string,
	obj: Request,
	token: string
) => {
	const result = await axios.post<Response>(url, obj, {
		headers: { Authorization: `Bearer ${token}` }
	});
	return result.data;
};

/**
 * Post object to the API
 * @param url The url to post the object to
 * @param obj The object to post
 * @param token Bearer token to authenticate with
 * @param params Query parameters
 */
export const postObjectParams = async <Response, Request>(
	url: string,
	obj: Request,
	token: string,
	params: any
) => {
	console.log(params);
	const result = await axios.post<Response>(url, obj, {
		headers: { Authorization: `Bearer ${token}` },
		params
	});
	return result.data;
};

/**
 * Update object in the API
 * @param url The url to put the object to
 * @param token Bearer token to authenticate with
 * @param obj The object to put
 */
export const putObject = async <T, K>(url: string, token: string, obj: K) => {
	const result = await axios.put<T>(url, obj, {
		headers: { Authorization: `Bearer ${token}` }
	});
	return result.data;
};

/**
 * Delete object using the API
 * @param url The url to delete the object from
 * @param token Bearer token to authenticate with
 */
export const deleteObject = async <T>(url: string, token: string) => {
	const result = await axios.delete<T>(url, {
		headers: { Authorization: `Bearer ${token}` }
	});
	return result.data;
};
