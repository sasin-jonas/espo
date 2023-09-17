import { FC, useCallback, useContext, useEffect, useState } from 'react';
import {
	Box,
	Grid,
	SelectChangeEvent,
	TextField,
	Tooltip
} from '@mui/material';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useQueryClient } from 'react-query';

import usePageTitle from '../hooks/usePageTitle';
import { OpportunityDto } from '../types/Opportunity.Types';
import AutocompleteTagSelect from '../components/controls/AutocompleteTagSelect';
import MaxResultsComboBox from '../components/controls/MaxResultsComboBox';
import SearchButton from '../components/controls/SearchButton';
import SearchResults from '../components/display/SearchResults';
import {
	useGetAllFilterValues,
	useSearchByPhrase
} from '../hooks/api/useSearchApi';
import { AppAlertTypes } from '../types/Alert.Types';
import { useAlert } from '../hooks/useAppAlert';
import { SearchProjectDto } from '../types/Search.Types';
import useField from '../hooks/useField';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

/**
 * Search by phrase page
 * @constructor
 */
const SearchByPhrasePage: FC = () => {
	// context
	usePageTitle('Search');
	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);

	// state
	const [filterValues, setFilterValues] = useState<{ [id: string]: string[] }>(
		{}
	);

	// filter values
	const [selectedHelixes, setSelectedHelixes] = useState<string[]>([]);
	const [selectedExpertise, setSelectedExpertise] = useState<string[]>([]);
	const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
	const [key, setKey] = useState<number>(1);
	// max search results
	const [maxResults, setMaxResults] = useState<number>(10);

	const [searchData, setSearchData] = useState<OpportunityDto[] | undefined>(
		undefined
	);

	// api calls
	const filtersResult = useGetAllFilterValues(token);
	const searchByPhraseCall = useSearchByPhrase(token, qc, setAlertOptions);

	// fields
	const [searchPhrase, searchFieldProps] = useField('Search phrase', true);

	// effects and handlers
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

	useEffect(() => {
		document.addEventListener('keydown', handleClearFilters);

		// Don't forget to clean up
		return function cleanup() {
			document.removeEventListener('keydown', handleClearFilters);
		};
	}, []);
	const handleClearFilters = (event: { keyCode: number }) => {
		if (event.keyCode === 113) {
			// F2 key
			setSelectedExpertise([]);
			setSelectedRoles([]);
			setSelectedHelixes([]);
			const rnd = Math.floor(Math.random() * (10000 + 1));
			setKey(rnd);
		}
	};

	const handleChangeMaxResults = useCallback((event: SelectChangeEvent) => {
		const value = Number(event.target.value);
		setMaxResults(isNaN(value) ? 10 : value);
	}, []);

	const handleKeyDown = async (event: { keyCode: number }) => {
		if (event.keyCode === 13) {
			await handleSearch();
		}
	};

	// Submit handler
	const handleSearch = async () => {
		if (searchPhrase.length === 0) {
			return;
		}
		const searchInfo: SearchProjectDto = {
			maxResults,
			personalized: false,
			projIds: [],
			ucoList: [],
			roles: selectedRoles,
			expertises: selectedExpertise,
			helixes: selectedHelixes,
			phrase: searchPhrase
		};
		console.log(searchInfo);
		let searchResult: OpportunityDto[];
		try {
			searchResult = await searchByPhraseCall.mutateAsync(searchInfo);
		} catch {
			console.error('Failed to perform opportunity search');
			return;
		}
		setSearchData(searchResult);
	};

	return (
		<>
			<Grid container spacing={1} sx={{ py: 1 }}>
				<Grid item xs={0.2}>
					<Tooltip title="Clear all search filters by pressing 'F2'">
						<QuestionMarkIcon fontSize="small" sx={{ maxHeight: 10 }} />
					</Tooltip>
				</Grid>
				<Grid item xs={3.9}>
					<AutocompleteTagSelect
						options={filterValues.helix ?? []}
						onChange={(_, value) => setSelectedHelixes(value)}
						label="Select preferred Helixes"
						placeHolder="Helixes"
						key={key}
					/>
				</Grid>
				<Grid item xs={3.9}>
					<AutocompleteTagSelect
						options={filterValues.expertise ?? []}
						onChange={(_, value) => setSelectedExpertise(value)}
						label="Select preferred expertise"
						placeHolder="Expertise"
						key={key}
					/>
				</Grid>
				<Grid item xs={3.9}>
					<AutocompleteTagSelect
						options={filterValues.role ?? []}
						onChange={(_, value) => setSelectedRoles(value)}
						label="Select preferred role"
						placeHolder="Roles"
						key={key}
					/>
				</Grid>
			</Grid>
			<Box sx={{ my: 2 }} />
			<Grid container justifyContent="center">
				<Grid item xs={6}>
					<TextField
						label="Search phrase"
						fullWidth
						{...searchFieldProps}
						sx={{ backgroundColor: '#e5e9ff' }}
						onKeyDown={handleKeyDown}
					/>
				</Grid>
			</Grid>
			<Box sx={{ my: 2 }} />
			<Grid container justifyContent="left" spacing={3}>
				<Grid item xs={3}>
					<MaxResultsComboBox
						onChange={handleChangeMaxResults}
						currentSize={maxResults?.toString()}
					/>
				</Grid>
			</Grid>
			<Grid item xs={3}>
				<SearchButton
					onClick={handleSearch}
					isDisabled={searchPhrase.length === 0}
				/>
			</Grid>
			{searchData && <SearchResults searchResult={searchData} />}
		</>
	);
};

export default SearchByPhrasePage;
