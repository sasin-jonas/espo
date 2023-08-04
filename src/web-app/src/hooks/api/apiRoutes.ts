const { REACT_APP_API_URL, REACT_APP_API_URL_DEFAULT } = process.env;
const baseUrl =
	REACT_APP_API_URL === undefined || REACT_APP_API_URL.length === 0
		? REACT_APP_API_URL_DEFAULT
		: REACT_APP_API_URL;

export const apiRoutes = {
	usersUrl: `${baseUrl}/users`,
	projectsUrl: `${baseUrl}/projects`,
	opportunitiesUrl: `${baseUrl}/opportunities`,
	authorsUrl: `${baseUrl}/authors`,
	departmentsUrl: `${baseUrl}/departments`,
	searchUrl: `${baseUrl}/search`,
	searchAll: `${baseUrl}/search/all`,
	recommendUrl: `${baseUrl}/recommend`
};
