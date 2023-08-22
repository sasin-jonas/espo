import { FC, useCallback, useContext, useEffect, useState } from 'react';
import { Box, Grid, SelectChangeEvent, Tooltip } from '@mui/material';
import { GridSelectionModel } from '@mui/x-data-grid';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useQueryClient } from 'react-query';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

import usePageTitle from '../hooks/usePageTitle';
import { OpportunityDto } from '../types/Opportunity.Types';
import AutocompleteTagSelect from '../components/controls/AutocompleteTagSelect';
import ProjectsDataGrid from '../components/project/ProjectsDataGrid';
import MaxResultsComboBox from '../components/controls/MaxResultsComboBox';
import CheckBoxWithLabel from '../components/controls/CheckBoxWithLabel';
import SearchButton from '../components/controls/SearchButton';
import SearchResults from '../components/display/SearchResults';
import {
	useGetAllFilterValues,
	useSearchByProjects
} from '../hooks/api/useSearchApi';
import { AppAlertTypes } from '../types/Alert.Types';
import { useAlert } from '../hooks/useAppAlert';
import { SearchProjectDto } from '../types/Search.Types';

/**
 * Search by project page
 * @constructor
 */
const SearchByProjectPage: FC = () => {
	// context
	usePageTitle('Search');
	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);

	const [filterValues, setFilterValues] = useState<{ [id: string]: string[] }>(
		{}
	);

	// filter values
	const [selectedHelixes, setSelectedHelixes] = useState<string[]>([]);
	const [selectedExpertise, setSelectedExpertise] = useState<string[]>([]);
	const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
	// selected projects
	const [selectedRows, setSelectedRows] = useState<number[]>([]);
	// max search results
	const [maxResults, setMaxResults] = useState<number>(10);
	// enable influence of author preferences
	const [influenceEnabled, setInfluenceEnabled] = useState<boolean>(false);

	const [searchData, setSearchData] = useState<OpportunityDto[] | undefined>(
		undefined
	);

	// api calls
	const filtersResult = useGetAllFilterValues(token);
	const searchByProjectCall = useSearchByProjects(token, qc, setAlertOptions);

	// effect hooks and handlers
	useEffect(() => {
		setFilterValues(filtersResult.data ? filtersResult.data.data : {});
	}, [filtersResult.data]);
	useEffect(() => {
		const element = document.getElementById('search-results-reference');
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}, [searchData]);
	useEffect(() => {
		if (filtersResult.isError) {
			setAlertOptions({
				open: true,
				error: filtersResult.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [filtersResult.isError]);

	const handleChangeMaxResults = useCallback((event: SelectChangeEvent) => {
		const value = Number(event.target.value);
		setMaxResults(isNaN(value) ? 10 : value);
	}, []);

	const onGridSelectionChange = (ids: GridSelectionModel) => {
		const selectedIDs = ids.map(id => Number(id)).filter(id => !isNaN(id));
		setSelectedRows(selectedIDs);
	};

	// Submit handler
	const handleSearch = async () => {
		if (selectedRows.length === 0) {
			return;
		}
		const searchInfo: SearchProjectDto = {
			maxResults,
			personalized: influenceEnabled,
			projIds: selectedRows,
			ucoList: [],
			roles: selectedRoles,
			expertises: selectedExpertise,
			helixes: selectedHelixes
		};
		let searchResult: OpportunityDto[];
		try {
			searchResult = await searchByProjectCall.mutateAsync(searchInfo);
		} catch {
			console.error('Failed to perform projects search');
			return;
		}
		setSearchData(searchResult);
	};

	return (
		<>
			<Grid container spacing={1} sx={{ py: 1 }}>
				<Grid item xs={4}>
					<AutocompleteTagSelect
						options={filterValues.helix ?? []}
						onChange={(_, value) => setSelectedHelixes(value)}
						label="Select preferred Helixes"
						placeHolder="Helixes"
					/>
				</Grid>
				<Grid item xs={4}>
					<AutocompleteTagSelect
						options={filterValues.expertise ?? []}
						onChange={(_, value) => setSelectedExpertise(value)}
						label="Select preferred expertise"
						placeHolder="Expertise"
					/>
				</Grid>
				<Grid item xs={4}>
					<AutocompleteTagSelect
						options={filterValues.role ?? []}
						onChange={(_, value) => setSelectedRoles(value)}
						label="Select preferred role"
						placeHolder="Roles"
					/>
				</Grid>
			</Grid>
			<Box sx={{ width: '100%' }}>
				<ProjectsDataGrid
					onGridSelectionChange={onGridSelectionChange}
					selection
					serverSide
					administrate={false}
				/>
			</Box>
			<Grid container justifyContent="left" spacing={3}>
				<Grid item xs={3}>
					<MaxResultsComboBox
						onChange={handleChangeMaxResults}
						currentSize={maxResults?.toString()}
					/>
				</Grid>
				<Grid xs={6} item container spacing={1} alignItems="center">
					<Grid item>
						<CheckBoxWithLabel
							onChange={(_, checked) => setInfluenceEnabled(checked)}
							defValue={influenceEnabled}
							label="Enable the influence of project author recommendations"
						/>
					</Grid>
					<Grid item>
						<Tooltip
							title={
								'If enabled, the search algorithm will consider the opportunity recommendations of the project authors.' +
								' These opportunities will be boosted in the search results.'
							}
						>
							<QuestionMarkIcon fontSize="small" sx={{ maxHeight: 15 }} />
						</Tooltip>
					</Grid>
				</Grid>
				<Grid item xs={3}>
					<SearchButton
						onClick={handleSearch}
						isDisabled={selectedRows.length === 0}
					/>
				</Grid>
			</Grid>
			{searchData && <SearchResults searchResult={searchData} />}
		</>
	);
};

export default SearchByProjectPage;
