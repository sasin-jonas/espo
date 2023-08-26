import { FC } from 'react';
import { Box, Card, CardContent, Typography } from '@mui/material';
import { OpportunitySearchResultDto } from '../../types/Search.Types';

/**
 * Opportunity preview component
 * @param opportunity Opportunity to display
 * @constructor
 */
const AuthorPreview: FC<OpportunitySearchResultDto> = ({
	authorDto,
	relevantProjects,
	sumScore,
	maxScore,
	averageScore,
	rank
}) => (
	<Card sx={{ width: 1, borderRadius: 2 }}>
		<CardContent sx={{ width: 'inherit' }}>
			<Box
				sx={{
					backgroundColor: '#E8E8EEFF',
					borderRadius: 2,
					px: 2,
					py: 1
				}}
			>
				<Typography
					variant="h5"
					textAlign="left"
					color="textPrimary"
					sx={{ textTransform: 'none' }}
				>
					{rank}: {authorDto.name} - {authorDto.uco}
				</Typography>
			</Box>
			<Box>
				<Typography
					textAlign="left"
					sx={{
						fontSize: 13,
						overflow: 'hidden',
						textOverflow: 'ellipsis',
						display: '-webkit-box',
						WebkitLineClamp: '5',
						WebkitBoxOrient: 'vertical',
						px: 2,
						pt: 2,
						textTransform: 'none'
					}}
				>
					{relevantProjects.length} relevant projects
					<br />
					Total project score: {(Math.round(sumScore * 100) / 100).toFixed(2)}
					<br />
					Highest score: {(Math.round(maxScore * 100) / 100).toFixed(2)}
					<br />
					Average score: {(Math.round(averageScore * 100) / 100).toFixed(2)}
					<br />
				</Typography>
			</Box>
		</CardContent>
	</Card>
);

export default AuthorPreview;
