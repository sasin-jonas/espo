import { FC } from 'react';
import { Chip, Grid, Typography } from '@mui/material';

type Props = {
	title?: string;
	tags?: string[];
};

/**
 * Chip list for displaying tags
 * @param title Title of the list
 * @param tags Tags to display
 * @constructor
 */
const ChipList: FC<Props> = ({ title, tags }) => (
	<Grid item container spacing={0.5}>
		<Grid item>
			<Typography color="grey">{title}</Typography>
		</Grid>
		{tags
			?.filter(h => h.length !== 0)
			.filter((value, index, array) => array.indexOf(value) === index)
			.map(h => (
				<Grid item key={h}>
					<Chip label={h} variant="outlined" />
				</Grid>
			))}
	</Grid>
);

export default ChipList;
