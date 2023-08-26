import { AuthorDto, ProjectDto } from './Project.Types';

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

export type OpportunitySearchResultDto = {
	authorDto: AuthorDto;
	relevantProjects: ProjectDto[];
	sumScore: number;
	averageScore: number;
	maxScore: number;
	rank?: number;
};

export type SearchByOpportunityDto = {
	opportunityId: string;
	maxResults: number;
	sortBy: string;
};
