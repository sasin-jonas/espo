import { FC, PropsWithChildren, useState } from 'react';
import {
	Box,
	Button,
	Card,
	CardContent,
	Grid,
	Link,
	Modal,
	Typography
} from '@mui/material';

import { OpportunityDto } from '../../types/Opportunity.Types';
import ChipList from '../display/ChipList';
import AttachmentLink from '../display/AttachmentLink';
import TextWithCaption from '../display/TextWithCaption';
import TextWithCaptionAndLink from '../display/TextWithCaptionAndLink';
import CloseIcon from '@mui/icons-material/Close';

type Props = PropsWithChildren<{
	opportunity?: OpportunityDto;
}>;

/**
 * Opportunity detail modal window component
 * @param opportunity Opportunity to display
 * @param children Clickable element to open the modal window (opportunity preview)
 * @constructor
 */
const OpportunityDetail: FC<Props> = ({ opportunity, children }) => {
	const [open, setOpen] = useState(false);
	const handleOpen = () => setOpen(true);
	const handleClose = () => setOpen(false);

	return (
		<>
			<Button onClick={handleOpen}>{children}</Button>
			<Modal
				sx={{
					top: '10%',
					left: '5%',
					right: '5%',
					bottom: '10%',
					overflow: 'scroll'
				}}
				open={open}
				onClose={handleClose}
				aria-describedby="opportunity-detail"
			>
				<Card sx={{ width: 1, borderRadius: 2 }}>
					<Grid container justifyContent="right">
						<Button
							onClick={handleClose}
							sx={{
								maxHeight: 25,
								backgroundColor: '#d4d4d9',
								color: 'black',
								mt: 0.5,
								mr: 2,
								mb: -1
							}}
						>
							<CloseIcon />
						</Button>
					</Grid>
					<CardContent sx={{ width: 'inherit' }}>
						<Box
							sx={{
								backgroundColor: '#E8E8EEFF',
								borderRadius: 2,
								px: 2,
								py: 1
							}}
						>
							<Typography variant="h5" textAlign="left" color="textPrimary">
								{opportunity?.title}
							</Typography>
						</Box>
						<Link
							rel="noopener noreferrer"
							href={opportunity?.url}
							target="_blank"
							sx={{ color: 'darkblue', px: 2 }}
						>
							Source
						</Link>
						<Box>
							<Typography
								textAlign="left"
								sx={{
									fontSize: 13,
									px: 2,
									pt: 2
								}}
							>
								{opportunity?.description}
							</Typography>
						</Box>
						<Grid container justifyContent="left" sx={{ p: 2 }} spacing={1}>
							<ChipList title="Helixes:" tags={opportunity?.helix} />
							<ChipList title="Expertise:" tags={opportunity?.expertise} />
							<ChipList title="Role:" tags={opportunity?.role} />
						</Grid>
						<Grid container>
							<Grid item>
								<TextWithCaptionAndLink
									caption="Institution"
									text={opportunity?.institutionName}
									url={opportunity?.institutionUrl}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption caption="Author" text={opportunity?.author} />
							</Grid>
							{opportunity?.appendixUrl && (
								<Grid item container xs={12} justifyContent="left">
									<AttachmentLink url={opportunity.appendixUrl} />
								</Grid>
							)}
						</Grid>
					</CardContent>
				</Card>
			</Modal>
		</>
	);
};

export default OpportunityDetail;
