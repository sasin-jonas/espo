import { FC } from 'react';
import { Box, Card, CardContent, Grid, Typography } from '@mui/material';

import { OpportunityDto } from '../../types/Opportunity.Types';
import ChipList from '../display/ChipList';
import AttachmentLink from '../display/AttachmentLink';
import TextWithCaption from '../display/TextWithCaption';

import OpportunityDetail from './OpportunityDetail';

type OpportunityProps = {
	opportunity: OpportunityDto;
};

/**
 * Opportunity preview component
 * @param opportunity Opportunity to display
 * @constructor
 */
const OpportunityPreview: FC<OpportunityProps> = ({ opportunity }) => (
	<OpportunityDetail opportunity={opportunity}>
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
						{opportunity.title}
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
						{opportunity.description}
					</Typography>
				</Box>
				<Grid container justifyContent="left" sx={{ p: 2, pb: 1 }} spacing={1}>
					<ChipList title="Helixes:" tags={opportunity.helix} />
					<ChipList title="Expertise:" tags={opportunity.expertise} />
					<ChipList title="Role:" tags={opportunity.role} />
				</Grid>
				<Grid container>
					{opportunity.appendixUrl && (
						<Grid item container xs={12} justifyContent="left">
							<AttachmentLink url={opportunity.appendixUrl} />
						</Grid>
					)}
					<Grid item>
						<TextWithCaption
							caption="Institution"
							text={opportunity.institutionName}
						/>
					</Grid>
					<Grid item>
						<TextWithCaption caption="Score" text={opportunity.score} />
					</Grid>
				</Grid>
			</CardContent>
		</Card>
	</OpportunityDetail>
);

export default OpportunityPreview;
