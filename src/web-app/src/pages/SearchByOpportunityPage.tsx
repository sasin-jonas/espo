import React, { FC, useCallback, useContext, useEffect, useState } from 'react';
import {
	Box,
	FormControl,
	Grid,
	InputLabel,
	MenuItem,
	Select,
	SelectChangeEvent
} from '@mui/material';
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
	// sort results by
	const [sortValue, setSortValue] = useState<string>('MAX');

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

	const handleChangeSortBy = useCallback((event: SelectChangeEvent) => {
		const value = event.target.value;
		setSortValue(value);
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
			searchResult = await searchByOpportunityCall.mutateAsync({
				opportunityId: selectionModel[0] as string,
				maxResults: maxResults,
				sortBy: sortValue
			});
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
				<Grid item xs={3}>
					<FormControl fullWidth>
						<InputLabel id="paging-label">Sort results by</InputLabel>
						<Select
							labelId="sortBy-info-label"
							id="sortBy-select"
							value={sortValue ?? 'max'}
							label="Sort by"
							onChange={handleChangeSortBy}
						>
							<MenuItem key={'max'} value={'MAX'}>
								Highest project score
							</MenuItem>
							<MenuItem key={'sum'} value={'SUM'}>
								Total project score
							</MenuItem>
							<MenuItem key={'avg'} value={'AVG'}>
								Average project score
							</MenuItem>
							<MenuItem key={'count'} value={'COUNT'}>
								Relevant projects count
							</MenuItem>
						</Select>
					</FormControl>
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
