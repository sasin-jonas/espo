import { FC } from 'react';
import { Button, Tooltip } from '@mui/material';
import ReadMoreIcon from '@mui/icons-material/ReadMore';
import { Link } from 'react-router-dom';

import { OpportunityDto } from '../../types/Opportunity.Types';

type OpportunityProps = {
	opportunity: OpportunityDto;
};

/**
 * 'More like this' search button
 * @param opportunity Opportunity to find similar results for
 * @constructor
 */
const MltButton: FC<OpportunityProps> = ({ opportunity }) => (
	<Tooltip title="More results like this">
		<Button
			variant="contained"
			component={Link}
			to={`/more-like/${opportunity.esId}`}
			target="_blank"
		>
			<ReadMoreIcon />
		</Button>
	</Tooltip>
);

export default MltButton;
