import { FC } from 'react';
import { Box, Grid, Typography } from '@mui/material';

import { OpportunityDto } from '../../types/Opportunity.Types';
import OpportunityPreview from '../opportunity/OpportunityPreview';
import MltButton from '../controls/MltButton';

import ColoredLine from './ColoredLine';

type Props = {
	searchResult: OpportunityDto[];
};

/**
 * Search results display component
 * @param searchResult Search results (opportunities) to display
 * @constructor
 */
const SearchResults: FC<Props> = ({ searchResult }) => (
	<>
		<Box height={10} id="search-results-reference" />
		<Grid container>
			<Typography variant="h5">Results:</Typography>
			<Grid item xs={12}>
				<ColoredLine color="grey" height={2} />
			</Grid>
			{searchResult && searchResult.length === 0 && (
				<Typography>No results, try different filtering please.</Typography>
			)}
			{searchResult && searchResult.length > 0 && (
				<Typography>Showing {searchResult.length} results</Typography>
			)}
		</Grid>
		<Grid container justifyContent="center" rowSpacing={3} columnSpacing={1}>
			{searchResult.map(result => (
				<Grid item container key={result.esId}>
					<Grid item xs={11}>
						<OpportunityPreview opportunity={result} />
					</Grid>
					<Grid item container justifyContent="right" xs={11} sx={{ px: 1 }}>
						<MltButton opportunity={result} />
					</Grid>
				</Grid>
			))}
		</Grid>
	</>
);

export default SearchResults;
