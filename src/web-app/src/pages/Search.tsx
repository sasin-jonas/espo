import { FC } from 'react';
import { Grid } from '@mui/material';
import PeopleIcon from '@mui/icons-material/People';
import TipsAndUpdatesIcon from '@mui/icons-material/TipsAndUpdates';
import FormatQuoteIcon from '@mui/icons-material/FormatQuote';

import usePageTitle from '../hooks/usePageTitle';
import ButtonWithIconAndTooltip from '../components/controls/ButtonWithIconAndTooltip';

/**
 * Search signpost page
 * @constructor
 */
const Search: FC = () => {
	usePageTitle('Search');

	const groupToolTip =
		'Search for opportunities by author or a group of authors. Opportunities are found using the characteristics of ' +
		'projects authored by the selected authors.' +
		'You can later choose specific projects of interest to boost.';
	const projectToolTip =
		'Search for opportunities by a specific project or a group of projects. ' +
		"You can later decide, if you want to take other author's projects into consideration.";
	const phraseToolTip =
		'Search by a phrase or keywords. Simple, yet effective.';

	return (
		<Grid container spacing={1} justifyContent="center">
			<Grid item xs={3}>
				<ButtonWithIconAndTooltip
					text="Search by phrase"
					tooltip={phraseToolTip}
					linkPath="/phraseSearch"
				>
					<FormatQuoteIcon />
				</ButtonWithIconAndTooltip>
			</Grid>
			<Grid item xs={3}>
				<ButtonWithIconAndTooltip
					text="Search by group or person"
					tooltip={groupToolTip}
					linkPath="/personSearch"
				>
					<PeopleIcon />
				</ButtonWithIconAndTooltip>
			</Grid>
			<Grid item xs={3}>
				<ButtonWithIconAndTooltip
					text="Search by project"
					tooltip={projectToolTip}
					linkPath="/projectSearch"
				>
					<TipsAndUpdatesIcon />
				</ButtonWithIconAndTooltip>
			</Grid>
		</Grid>
	);
};

export default Search;
