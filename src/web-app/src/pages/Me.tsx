import { FC, useContext } from 'react';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { Box, Typography } from '@mui/material';

import usePageTitle from '../hooks/usePageTitle';
import useLoggedInUser from '../hooks/useLoggedInUser';

/**
 * Me page
 * @constructor
 */
const Me: FC = () => {
	// context
	usePageTitle('Me');
	const { idTokenData } = useContext<IAuthContext>(AuthContext);
	const user = useLoggedInUser();
	const roles = user?.roles.map(r => r.name.replace('ROLE_', '')).join(', ');

	return (
		<Box>
			<Typography variant="h6">{idTokenData?.name}</Typography>
			<ul>
				<li>Uco: {idTokenData?.preferred_username}</li>
				<li>E-mail: {idTokenData?.email}</li>
				<li>Roles: {roles}</li>
			</ul>
		</Box>
	);
};

export default Me;
