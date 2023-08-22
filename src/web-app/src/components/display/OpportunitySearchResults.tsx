import { FC } from 'react';
import { Box, Grid, Typography } from '@mui/material';

import ColoredLine from './ColoredLine';
import { OpportunitySearchResultDto } from '../../types/Search.Types';
import AuthorPreview from '../opportunity/AuthorPreview';

type Props = {
	searchResult: OpportunitySearchResultDto[];
};

/**
 * Search results display component
 * @param searchResult Search results (opportunities) to display
 * @constructor
 */
const OpportunitySearchResults: FC<Props> = ({ searchResult }) => (
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
				<Grid item container key={result.authorDto.uco}>
					<Grid item xs={11}>
						<AuthorPreview
							authorDto={result.authorDto}
							relevantProjects={result.relevantProjects}
							aggregateScore={result.aggregateScore}
						></AuthorPreview>
					</Grid>
				</Grid>
			))}
		</Grid>
	</>
);

export default OpportunitySearchResults;
