import { FC } from 'react';
import { Box, Card, CardContent, Typography } from '@mui/material';
import { AuthorDto, ProjectDto } from '../../types/Project.Types';

type AuthorProps = {
	author: AuthorDto;
	projects: ProjectDto[];
	aggregateScore: number;
};

/**
 * Opportunity preview component
 * @param opportunity Opportunity to display
 * @constructor
 */
const AuthorPreview: FC<AuthorProps> = ({
	author,
	projects,
	aggregateScore
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
					{author.name} - {author.uco}
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
					{projects.length} relevant projects
					<br />
					score: {aggregateScore}
				</Typography>
			</Box>
		</CardContent>
	</Card>
);

export default AuthorPreview;
