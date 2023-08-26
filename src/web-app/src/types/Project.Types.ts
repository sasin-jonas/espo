export type ProjectDto = {
	id: number;
	projId: string;
	regCode: string;
	title: string;
	author?: AuthorDto;
	state: string;
	dateBegin: string;
	dateEnd: string;
	muniRole: string;
	investor: string;
	department?: DepartmentDto;
	annotation: string;
	score?: string;
};

export type ProjectPageable = {
	totalElements: number;
	content: ProjectDto[];
};

export type DepartmentDto = {
	id: number;
	orgUnit: string;
	departmentName: string;
};

export type AuthorDto = {
	id: number;
	uco: string;
	name: string;
};
