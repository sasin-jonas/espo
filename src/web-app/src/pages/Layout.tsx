import { FC } from 'react';
import { HashRouter, Route, Routes } from 'react-router-dom';
import { Container, CssBaseline } from '@mui/material';

import NavBar from '../components/navigation/NavBar';
import FooterBar from '../components/navigation/FooterBar';
import AppAlert from '../components/display/AppAlert';
import { useAlert } from '../hooks/useAppAlert';

import Home from './Home';
import Me from './Me';
import NotFound from './NotFound';
import About from './About';
import Search from './Search';
import SearchByProjectPage from './SearchByProjectPage';
import SearchByProjectAuthorsPage from './SearchByProjectAuthorsPage';
import UsersPage from './UsersPage';
import MoreLikeThisPage from './MoreLikeThisPage';
import ProjectsPage from './ProjectsPage';
import OpportunitiesPage from './OpportunitiesPage';
import SearchByPhrasePage from './SearchByPhrasePage';
import SearchByOpportunityPage from './SearchByOpportunityPage';

/**
 * Layout component
 * @constructor
 */
const Layout: FC = () => {
	const [alert, setAlertOptions] = useAlert();

	const mainStyle = {
		display: 'flex',
		flexDirection: 'column',
		justifyContent: 'center',
		alignItems: 'center',
		flexGrow: 1,
		gap: 2,
		py: 2
	};

	return (
		<HashRouter>
			<CssBaseline />
			<NavBar />
			<Container maxWidth="xl" component="main" sx={mainStyle}>
				<Routes>
					<Route path="/" element={<Home />} />
					<Route path="/search" element={<Search />} />
					<Route path="/projectSearch" element={<SearchByProjectPage />} />
					<Route
						path="/opportunitySearch"
						element={<SearchByOpportunityPage />}
					/>
					<Route
						path="/personSearch"
						element={<SearchByProjectAuthorsPage />}
					/>
					<Route path="/phraseSearch" element={<SearchByPhrasePage />} />
					<Route path="/about" element={<About />} />
					<Route path="/me" element={<Me />} />
					<Route path="/users" element={<UsersPage />} />
					<Route path="/projects" element={<ProjectsPage />} />
					<Route path="/opportunities" element={<OpportunitiesPage />} />
					<Route path="/more-like/:id" element={<MoreLikeThisPage />} />
					<Route path="*" element={<NotFound />} />
				</Routes>
				<AppAlert
					open={alert.open}
					setAppAlertOptions={setAlertOptions}
					successMessage={alert.successMessage}
					error={alert.error}
					severity={alert.severity}
				/>
			</Container>
			<FooterBar />
		</HashRouter>
	);
};

export default Layout;
