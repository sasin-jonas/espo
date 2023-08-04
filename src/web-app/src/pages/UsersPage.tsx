import React, { FC, useCallback, useContext, useEffect, useState } from 'react';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { Box, Typography } from '@mui/material';

import usePageTitle from '../hooks/usePageTitle';
import useLoggedInUser from '../hooks/useLoggedInUser';
import UsersDataGrid from '../components/user/UsersDataGrid';
import { UserDto } from '../types/User.Types';
import { useGetAllUsers } from '../hooks/api/useUsersApi';
import { AppAlertTypes } from '../types/Alert.Types';
import { useAlert } from '../hooks/useAppAlert';

/**
 * Users management page
 * @constructor
 */
const UsersPage: FC = () => {
	// context
	usePageTitle('Manage users');
	const { token } = useContext<IAuthContext>(AuthContext);
	const user = useLoggedInUser();
	const [, setAlertOptions] = useAlert();

	// data
	const [userRows, setUserRows] = useState<UserDto[]>([]);
	const [pageSize, setPageSize] = useState<number>(5);

	// api calls
	const userDataResult = useGetAllUsers(token);

	const isAdmin = useCallback(
		() => user?.roles.map(r => r.name).includes('ROLE_ADMIN'),
		[user]
	);

	useEffect(() => {
		setUserRows(
			userDataResult.data
				? userDataResult.data.data.filter(
						d => d.jwtIdentifier !== user?.jwtIdentifier
				  )
				: []
		);
	}, [userDataResult.data]);

	useEffect(() => {
		if (userDataResult.isError) {
			setAlertOptions({
				open: true,
				error: userDataResult.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [userDataResult.isError]);

	return (
		<>
			<Box
				sx={{
					backgroundColor: '#E8E8EEFF',
					borderRadius: 2,
					px: 2,
					my: 1
				}}
			>
				<Typography variant="h5">Manage application users</Typography>
			</Box>
			{isAdmin() && (
				<UsersDataGrid
					rows={userRows}
					pageSize={pageSize}
					onPageSizeChange={newSize => setPageSize(newSize)}
					loading={userDataResult.isLoading}
				/>
			)}
			{!isAdmin() && (
				<Typography>You are unauthorized for the management section</Typography>
			)}
		</>
	);
};

export default UsersPage;
