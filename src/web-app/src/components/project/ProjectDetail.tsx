import { FC, PropsWithChildren, useState } from 'react';
import {
	Box,
	Button,
	Card,
	CardContent,
	Grid,
	Modal,
	Typography
} from '@mui/material';

import { ProjectDto } from '../../types/Project.Types';
import TextWithCaption from '../display/TextWithCaption';
import CloseIcon from '@mui/icons-material/Close';

type Props = PropsWithChildren<{
	project?: ProjectDto;
}>;

/**
 * Project detail modal window component
 * @param project Project to display
 * @param children Clickable element to open the modal window (project detail icon)
 * @constructor
 */
const ProjectDetail: FC<Props> = ({ project, children }) => {
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
								py: 1,
								mb: 1
							}}
						>
							<Typography variant="h5" textAlign="left" color="textPrimary">
								{project?.title}
							</Typography>
						</Box>
						<Grid container>
							<Grid item>
								<TextWithCaption
									caption="Author"
									text={`${project?.author?.name} - ${project?.author?.uco}`}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption
									caption="Registration code"
									text={project?.regCode}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption caption="ID" text={project?.projId} />
							</Grid>
						</Grid>
						<Box>
							<Typography
								textAlign="left"
								sx={{
									fontSize: 13,
									p: 2
								}}
							>
								{project?.annotation}
							</Typography>
						</Box>
						<Grid container sx={{ pb: 1 }}>
							<Grid item>
								<TextWithCaption
									caption="Project state"
									text={project?.state}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption
									caption="Start date"
									text={project?.dateBegin}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption caption="End date" text={project?.dateEnd} />
							</Grid>
						</Grid>
						<Grid container>
							<Grid item>
								<TextWithCaption
									caption="Department"
									text={`${project?.department?.orgUnit} - ${project?.department?.departmentName}`}
								/>
							</Grid>
							<Grid item>
								<TextWithCaption caption="MU role" text={project?.muniRole} />
							</Grid>
							<Grid item>
								<TextWithCaption caption="Investor" text={project?.investor} />
							</Grid>
						</Grid>
					</CardContent>
				</Card>
			</Modal>
		</>
	);
};

export default ProjectDetail;
