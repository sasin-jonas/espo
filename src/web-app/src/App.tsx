import React from 'react';
import { ThemeProvider } from '@mui/material';
import { QueryClient, QueryClientProvider } from 'react-query';
import { ReactQueryDevtools } from 'react-query/devtools';
import {
	AuthProvider,
	TAuthConfig,
	TRefreshTokenExpiredEvent
} from 'react-oauth2-code-pkce';

import theme from './utils/theme';
import Layout from './pages/Layout';
import { UserInfoProvider } from './hooks/useLoggedInUser';
import { AlertProvider } from './hooks/useAppAlert';

/**
 * Query client for react-query
 */
const queryClient = new QueryClient({
	defaultOptions: {
		queries: {
			retry: 0,
			refetchOnWindowFocus: false,
			staleTime: 3600000
		}
	}
});
/**
 * Auth config for OIDC
 */
const authConfig: TAuthConfig = {
	clientId: '8ea6b22e-7c97-4211-8fe6-9dc434e6b90e',
	authorizationEndpoint: 'https://oidc.muni.cz/oidc/authorize',
	logoutEndpoint: 'https://oidc.muni.cz/oidc/endsession',
	tokenEndpoint: 'https://oidc.muni.cz/oidc/token',
	redirectUri: `${window.location.origin}`,
	logoutRedirect: `${window.location.origin}`,
	scope:
		'openid profile email offline_access eduperson_entitlement user_identifiers',
	autoLogin: false,
	onRefreshTokenExpire: (event: TRefreshTokenExpiredEvent) => event.login()
};

const App = () => (
	<AuthProvider authConfig={authConfig}>
		<UserInfoProvider>
			<AlertProvider>
				<QueryClientProvider client={queryClient}>
					<ThemeProvider theme={theme}>
						<Layout />
						<ReactQueryDevtools />
					</ThemeProvider>
				</QueryClientProvider>
			</AlertProvider>
		</UserInfoProvider>
	</AuthProvider>
);

export default App;
