export type SearchProjectDto = {
	projIds: number[];
	ucoList: string[];
	helixes: string[];
	expertises: string[];
	roles: string[];
	maxResults: number;
	personalized: boolean;
	phrase?: string;
};
