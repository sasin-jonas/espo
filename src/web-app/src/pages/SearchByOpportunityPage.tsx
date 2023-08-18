import React, { FC, useCallback, useContext, useEffect, useState } from 'react';
import { Box, Grid, SelectChangeEvent } from '@mui/material';
import { GridSelectionModel } from '@mui/x-data-grid';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useQueryClient } from 'react-query';

import usePageTitle from '../hooks/usePageTitle';
import { OpportunityDto } from '../types/Opportunity.Types';
import MaxResultsComboBox from '../components/controls/MaxResultsComboBox';
import SearchButton from '../components/controls/SearchButton';
import SearchResults from '../components/display/SearchResults';
import { useAlert } from '../hooks/useAppAlert';
import OpportunitiesDataGrid from '../components/opportunity/OpportunitiesDataGrid';

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

	// max search results
	const [maxResults, setMaxResults] = useState<number>(10);

	const [searchData, setSearchData] = useState<OpportunityDto[] | undefined>(
		undefined
	);

	const [selectionModel, setSelectionModel] = useState<GridSelectionModel>([]);

	// api calls
	// const searchByOpportunityCall = useSearchByProjects(token, qc, setAlertOptions);

	// effect hooks and handlers
	useEffect(() => {
		const element = document.getElementById('search-results-reference');
		if (element) {
			element.scrollIntoView({ behavior: 'smooth' });
		}
	}, [searchData]);

	const handleChangeMaxResults = useCallback((event: SelectChangeEvent) => {
		const value = Number(event.target.value);
		setMaxResults(isNaN(value) ? 10 : value);
	}, []);

	const onGridSelectionChange = (selection: GridSelectionModel) => {
		if (selection.length > 1) {
			const result = selection.at(selection.length - 1);

			setSelectionModel([result ?? 0]);
		} else {
			setSelectionModel(selection);
		}
	};

	// Submit handler
	const handleSearch = async () => {};

	return (
		<>
			<OpportunitiesDataGrid
				selection
				onGridSelectionChange={onGridSelectionChange}
				selectionModel={selectionModel[0] ?? []}
			/>
			<Grid container justifyContent="left" spacing={3}>
				<Grid item xs={3}>
					<MaxResultsComboBox
						onChange={handleChangeMaxResults}
						currentSize={maxResults?.toString()}
					/>
				</Grid>
				<Box sx={{ flexGrow: 1 }} />
				<Grid item xs={3}>
					<SearchButton
						onClick={handleSearch}
						isDisabled={selectionModel.length === 0}
					/>
				</Grid>
			</Grid>
			{searchData && <SearchResults searchResult={searchData} />}
		</>
	);
};

export default SearchByProjectPage;
