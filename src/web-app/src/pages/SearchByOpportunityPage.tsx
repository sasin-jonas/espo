import React, { FC, useCallback, useContext, useEffect, useState } from 'react';
import { Box, Grid, SelectChangeEvent } from '@mui/material';
import { GridSelectionModel } from '@mui/x-data-grid';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useQueryClient } from 'react-query';

import usePageTitle from '../hooks/usePageTitle';
import MaxResultsComboBox from '../components/controls/MaxResultsComboBox';
import SearchButton from '../components/controls/SearchButton';
import { useAlert } from '../hooks/useAppAlert';
import OpportunitiesDataGrid from '../components/opportunity/OpportunitiesDataGrid';
import { OpportunitySearchResultDto } from '../types/Search.Types';
import OpportunitySearchResults from '../components/display/OpportunitySearchResults';
import { useSearchByOpportunity } from '../hooks/api/useSearchApi';

/**
 * Search by project page
 * @constructor
 */
const SearchByOpportunityPage: FC = () => {
	// context
	usePageTitle('Search');
	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);

	// max search results
	const [maxResults, setMaxResults] = useState<number>(10);

	const [searchData, setSearchData] = useState<
		OpportunitySearchResultDto[] | undefined
	>(undefined);

	const [selectionModel, setSelectionModel] = useState<GridSelectionModel>([]);

	// api calls
	const searchByOpportunityCall = useSearchByOpportunity(
		token,
		qc,
		setAlertOptions
	);

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
	const handleSearch = async () => {
		if (selectionModel.length === 0) {
			return;
		}
		let searchResult: OpportunitySearchResultDto[];
		try {
			searchResult = await searchByOpportunityCall.mutateAsync(
				selectionModel[0] as string
			);
		} catch {
			console.error('Failed to perform opportunity search');
			return;
		}
		setSearchData(searchResult);
	};

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
			{searchData && <OpportunitySearchResults searchResult={searchData} />}
		</>
	);
};

export default SearchByOpportunityPage;
