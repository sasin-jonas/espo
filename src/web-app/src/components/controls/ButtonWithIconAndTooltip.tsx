import { Box, Button, Grid, Tooltip, Typography } from '@mui/material';
import { FC, PropsWithChildren } from 'react';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';
import { Link } from 'react-router-dom';

type Props = PropsWithChildren<{
	text: string;
	tooltip: string;
	linkPath: string;
}>;

/**
 * Button with icon and tooltip
 * @param text Button text
 * @param tooltip Tooltip text
 * @param linkPath Link path
 * @param children The button icon
 * @constructor
 */
const ButtonWithIconAndTooltip: FC<Props> = ({
	text,
	tooltip,
	linkPath,
	children
}) => (
	<Button
		sx={{
			backgroundColor: '#0000DC',
			color: '#ffffff',
			height: 120,
			width: 1,
			borderRadius: 3
		}}
		component={Link}
		to={linkPath}
		variant="contained"
	>
		<Grid container justifyContent="right">
			<Grid container item xs={12} justifyContent="center">
				<Grid item xs={12} justifyContent="center">
					<Typography align="center">{text}</Typography>
				</Grid>
				<Grid item xs={12}>
					<Box height={5} />
				</Grid>
				<Grid item>{children}</Grid>
			</Grid>
			<Grid item>
				<Tooltip title={tooltip}>
					<QuestionMarkIcon fontSize="small" sx={{ maxHeight: 15 }} />
				</Tooltip>
			</Grid>
		</Grid>
	</Button>
);

export default ButtonWithIconAndTooltip;
