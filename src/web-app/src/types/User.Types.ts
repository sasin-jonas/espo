export type UserDto = {
	id: number;
	jwtIdentifier: string;
	uco: string;
	email: string;
	name: string;
	roles: RoleDto[];
};

export type RoleDto = {
	name: string;
};
