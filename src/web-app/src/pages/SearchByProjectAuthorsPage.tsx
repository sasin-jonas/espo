import {FC, useCallback, useContext, useEffect, useState} from 'react';
import {Box, Grid, SelectChangeEvent, Tooltip} from '@mui/material';
import {GridSelectionModel} from '@mui/x-data-grid';
import {AuthContext, IAuthContext} from 'react-oauth2-code-pkce';
import {useQueryClient} from 'react-query';
import {AxiosResponse} from 'axios';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

import usePageTitle from '../hooks/usePageTitle';
import {AuthorDto, ProjectDto} from '../types/Project.Types';
import {useGetAllAuthors, useGetProjectsByAuthor} from '../hooks/api/useProjectsApi';
import {OpportunityDto} from '../types/Opportunity.Types';
import AutocompleteTagSelect from '../components/controls/AutocompleteTagSelect';
import MaxResultsComboBox from '../components/controls/MaxResultsComboBox';
import CheckBoxWithLabel from '../components/controls/CheckBoxWithLabel';
import SearchButton from '../components/controls/SearchButton';
import SearchResults from '../components/display/SearchResults';
import AuthorsDataGrid from '../components/author/AuthorsDataGrid';
import ProjectsDataGrid from '../components/project/ProjectsDataGrid';
import {useGetAllFilterValues, useSearchByAuthors} from '../hooks/api/useSearchApi';
import {AppAlertTypes} from '../types/Alert.Types';
import {useAlert} from '../hooks/useAppAlert';
import {SearchProjectDto} from '../types/Search.Types';

/**
 * Search by project authors page
 * @constructor
 */
const SearchByProjectAuthorsPage: FC = () => {
	// context
	usePageTitle('Search');

	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);

	// state
	const [authorPageSize, setAuthorPageSize] = useState<number>(10);
	const [projectPageSize, setProjectPageSize] = useState<number>(20);
	const [authorRows, setAuthorRows] = useState<AuthorDto[]>([]);
	const [projectRows, setProjectRows] = useState<ProjectDto[]>([]);
	const [filterValues, setFilterValues] = useState<{ [id: string]: string[] }>(
		{}
	);

	// filter values
	const [selectedHelixes, setSelectedHelixes] = useState<string[]>([]);
	const [selectedExpertise, setSelectedExpertise] = useState<string[]>([]);
	const [selectedRoles, setSelectedRoles] = useState<string[]>([]);
	const [key, setKey] = useState<number>(1);
	// selected authors
	const [selectedAuthorRows, setSelectedAuthorRows] = useState<AuthorDto[]>([]);
	// selected projects
	const [selectedProjectRows, setSelectedProjectRows] = useState<number[]>([]);
	// max search results
	const [maxResults, setMaxResults] = useState<number>(10);
	// enable influence of author preferences
	const [selectProjectsEnabled, setSelectProjectsEnabled] =
		useState<boolean>(false);

	const [searchData, setSearchData] = useState<OpportunityDto[] | undefined>(
		undefined
	);

	// api calls
	const authorRowsResult = useGetAllAuthors(token);
	const filtersResult = useGetAllFilterValues(token);
	const searchByAuthorsCall = useSearchByAuthors(token, qc, setAlertOptions);
	const getProjectsByAuthorCall = useGetProjectsByAuthor(
		token,
		qc,
		setAlertOptions
	);

	// effects and handlers
	useEffect(() => {
		setAuthorRows(authorRowsResult.data ? authorRowsResult.data.data : []);
	}, [authorRowsResult.data]);
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
		if (authorRowsResult.isError) {
			setAlertOptions({
				open: true,
				error: authorRowsResult.error,
				severity: AppAlertTypes.Error
			});
		} else if (filtersResult.isError) {
			setAlertOptions({
				open: true,
				error: filtersResult.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [authorRowsResult.isError, filtersResult.isError]);

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

	const onAuthorGridSelectionChange = async (ids: GridSelectionModel) => {
		const selectedIDs = new Set(ids);
		const selectedRowData = authorRows.filter(row => selectedIDs.has(row.id));

		const oldSelectedIds = selectedAuthorRows.map(a => a.id);
		const newSelects: AuthorDto[] = selectedRowData.filter(
			r => !oldSelectedIds.includes(r.id)
		);
		const newUnselects = oldSelectedIds.filter(
			oldId => !selectedIDs.has(oldId)
		);
		let newProjectRows = [...projectRows];
		// remove projects from unselected authors
		newUnselects.forEach(newlyUnselected => {
			newProjectRows = projectRows.filter(
				p => newlyUnselected !== p?.author?.id
			);
		});
		// add projects from newly selected authors
		for (const newlySelected of newSelects) {
			let authorProjectsResult: AxiosResponse<ProjectDto[]>;
			try {
				authorProjectsResult = await getProjectsByAuthorCall.mutateAsync(
					newlySelected.uco
				);
			} catch {
				console.error("Couldn't fetch author's projects");
				return;
			}
			const authorProjects = authorProjectsResult.data;
			newProjectRows = newProjectRows.concat(authorProjects ?? []);
		}
		setProjectRows([...newProjectRows]);
		setSelectedAuthorRows(selectedRowData);
	};

	const onProjectGridSelectionChange = (ids: GridSelectionModel) => {
		const selectedIDs = ids.map(id => Number(id)).filter(id => !isNaN(id));
		setSelectedProjectRows(selectedIDs);
	};

	// Submit handler
	const handleSearch = async () => {
		if (selectedAuthorRows.length === 0) {
			return;
		}
		const searchInfo: SearchProjectDto = {
			maxResults,
			personalized: selectProjectsEnabled,
			projIds: selectedProjectRows,
			ucoList: selectedAuthorRows.map(a => a.uco),
			roles: selectedRoles,
			expertises: selectedExpertise,
			helixes: selectedHelixes
		};
		let searchResult: OpportunityDto[];
		try {
			searchResult = await searchByAuthorsCall.mutateAsync(searchInfo);
		} catch {
			console.error('Failed to perform projects search');
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
			<Box sx={{ width: '100%' }}>
				<AuthorsDataGrid
					rows={[...authorRows].sort((a, b) => a.name.localeCompare(b.name))}
					pageSize={authorPageSize}
					onGridSelectionChange={ids => onAuthorGridSelectionChange(ids)}
					onPageSizeChange={newSize => setAuthorPageSize(newSize)}
					loading={authorRowsResult.isLoading}
					selectionModel={selectedAuthorRows.map(a => a.id)}
				/>
			</Box>
			<Grid container justifyContent="left" spacing={3}>
				<Grid xs={12} item container spacing={1} alignItems="center">
					<Grid item>
						<CheckBoxWithLabel
							onChange={(_, checked) => setSelectProjectsEnabled(checked)}
							defValue={selectProjectsEnabled}
							label="Select particular projects of interest"
						/>
					</Grid>
					<Grid item>
						<Tooltip
							title={
								'Select specific projects of the selected authors. ' +
								'The opportunities relevant for these projects will be boosted in the search results.'
							}
						>
							<QuestionMarkIcon fontSize="small" sx={{ maxHeight: 15 }} />
						</Tooltip>
					</Grid>
				</Grid>
				{selectProjectsEnabled && (
					<Grid item xs={12}>
						<ProjectsDataGrid
							rows={projectRows}
							pageSize={projectPageSize}
							loading={getProjectsByAuthorCall.isLoading}
							onGridSelectionChange={ids => onProjectGridSelectionChange(ids)}
							onPageSizeChange={newSize => setProjectPageSize(newSize)}
							selection
							serverSide={false}
							administrate={false}
							selectionModel={selectedProjectRows}
						/>
					</Grid>
				)}
				<Grid item xs={3}>
					<MaxResultsComboBox
						onChange={handleChangeMaxResults}
						currentSize={maxResults?.toString()}
					/>
				</Grid>
				<Grid item xs={6} />
				<Grid item xs={3}>
					<SearchButton
						onClick={handleSearch}
						isDisabled={selectedAuthorRows.length === 0}
					/>
				</Grid>
			</Grid>
			{searchData && <SearchResults searchResult={searchData} />}
		</>
	);
};

export default SearchByProjectAuthorsPage;
