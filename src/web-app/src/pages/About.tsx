import React, { FC, useContext, useEffect, useState } from 'react';
import { Box, Grid, Typography } from '@mui/material';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';

import usePageTitle from '../hooks/usePageTitle';
import { useGetAdminEmails } from '../hooks/api/useUsersApi';
import { AppAlertTypes } from '../types/Alert.Types';
import { useAlert } from '../hooks/useAppAlert';

/**
 * About page
 * @constructor
 */
const About: FC = () => {
	// context
	usePageTitle('About');
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);

	// data
	const [adminEmails, setAdminEmails] = useState<string[]>([]);

	// api calls
	const adminEmailsResult = useGetAdminEmails(token);

	// effects
	useEffect(() => {
		setAdminEmails(adminEmailsResult.data ? adminEmailsResult.data.data : []);
	}, [adminEmailsResult.data]);

	useEffect(() => {
		if (adminEmailsResult.isError && token) {
			setAlertOptions({
				open: true,
				error: adminEmailsResult.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [adminEmailsResult.isError]);

	return (
		<Grid justifyContent="left">
			<Typography align="left" paragraph>
				This application was developed as a Master&apos;s thesis by Jonáš Sasín
				under the supervision of RNDr. Martin Komenda, Ph.D.
			</Typography>
			<Typography align="left" paragraph>
				The ESPO (Effective Search for Project Opportunities) web application is
				intended for internal project teams at Masaryk University. During the
				pilot run, the application provides information on submitted and
				implemented projects at Faculty of Medicine together with selected data
				from the Crowdhelix platform (
				<a href="https://crowdhelix.com/" target="_blank">
					https://crowdhelix.com/
				</a>
				). The primary goal of the application is to return the most relevant
				results to the user through several different search approaches, based
				on the similarity (using keywords) between the Crowdhelix project
				opportunities and Masaryk University projects. This will allow project
				teams to efficiently search for new domains to collaborate on new
				projects.
			</Typography>
			<Box minHeight={100} />
			{token && (
				<Grid justifyContent="left">
					<Typography align="left">
						You can contact the system administrators using one of the following
						addresses:
					</Typography>
					<ul>
						{adminEmails.map(mail => (
							<li key={mail}>
								<Typography key={mail}>{mail}</Typography>
							</li>
						))}
					</ul>
				</Grid>
			)}
		</Grid>
	);
};

export default About;
