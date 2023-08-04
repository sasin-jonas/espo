import { FC, useContext, useEffect } from 'react';
import { Box, Button, CircularProgress, Typography } from '@mui/material';
import { Link } from 'react-router-dom';
import SearchIcon from '@mui/icons-material/Search';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useQueryClient } from 'react-query';
import { AxiosResponse } from 'axios';

import usePageTitle from '../hooks/usePageTitle';
import { useUserInfo } from '../hooks/useLoggedInUser';
import { useGetMyUserInfo } from '../hooks/api/useUsersApi';
import { useAlert } from '../hooks/useAppAlert';
import { UserDto } from '../types/User.Types';

/**
 * Home page
 * @constructor
 */
const Home: FC = () => {
	// context
	usePageTitle('Home');
	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const [user, setUser] = useUserInfo();

	// state
	const { login, token, loginInProgress } =
		useContext<IAuthContext>(AuthContext);

	// api calls
	const userInfoCall = useGetMyUserInfo(token, qc, setAlertOptions);

	// handlers
	const loginHandler = () => () => {
		login();
	};

	useEffect(() => {
		if (token) {
			let userInfo: Promise<AxiosResponse<UserDto>>;
			try {
				userInfo = userInfoCall.mutateAsync('');
			} catch {
				console.error('Failed to fetch user info');
				return;
			}
			userInfo.then(response => {
				setUser(response.data);
			});
		}
	}, [token]);

	return (
		<>
			{!token && !loginInProgress && (
				<Button
					style={{ display: 'flex', flexDirection: 'column' }}
					startIcon={
						<img
							src={require('../resources/login/light_en.png')}
							alt="stock_image"
						/>
					}
					variant="text"
					onClick={loginHandler()}
				/>
			)}
			{!token && loginInProgress && <CircularProgress />}
			{token && user && user.roles.length !== 0 && (
				<Button
					component={Link}
					to="/search"
					sx={{
						backgroundColor: '#0000DC',
						color: '#ffffff',
						minHeight: 100,
						width: 0.25,
						borderRadius: 3
					}}
					variant="contained"
				>
					<Typography fontSize={20} fontWeight={20}>
						Let&apos;s search
					</Typography>
					<Box sx={{ flexGrow: 0.5 }} />
					<SearchIcon fontSize="large" />
				</Button>
			)}
			{token && user && user.roles.length === 0 && (
				<Typography fontSize={20} fontWeight={20}>
					You have insufficient rights to use this application. Please contact
					the administrator.
				</Typography>
			)}
		</>
	);
};

export default Home;
