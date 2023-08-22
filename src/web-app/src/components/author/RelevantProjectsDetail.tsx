import { FC, PropsWithChildren, useState } from 'react';
import { Button, Card, CardContent, Grid, Modal } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { ProjectDto } from '../../types/Project.Types';
import ProjectsDataGrid from '../project/ProjectsDataGrid';

type Props = PropsWithChildren<{
	projects?: ProjectDto[];
}>;

/**
 * Relevant projects modal display
 * @param projects Relevant projects to display
 * @param children Clickable element to open the modal window (author preview)
 * @constructor
 */
const RelevantProjectsDetail: FC<Props> = ({ projects, children }) => {
	const [open, setOpen] = useState(false);
	const handleOpen = () => setOpen(true);
	const handleClose = () => setOpen(false);

	return (
		<>
			<Button sx={{ width: 1 }} onClick={handleOpen}>
				{children}
			</Button>
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
				aria-describedby="relevant-autho-projects"
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
						<ProjectsDataGrid
							selection={false}
							serverSide={false}
							rows={projects}
							administrate={false}
						></ProjectsDataGrid>
					</CardContent>
				</Card>
			</Modal>
		</>
	);
};

export default RelevantProjectsDetail;
