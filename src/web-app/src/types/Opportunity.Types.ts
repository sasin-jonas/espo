export type OpportunityDto = {
	ID: number;
	institutionName: string;
	institutionUrl: string;
	appendixUrl: string;
	title: string;
	url: string;
	author: string;
	description: string;
	helix: string[];
	role: string[];
	expertise: string[];
	esId: string;
	//debug props:
	score: number;
	rank: number;
	hitSource: string;
};

export type OpportunityPageable = {
	totalElements: number;
	content: OpportunityDto[];
};
