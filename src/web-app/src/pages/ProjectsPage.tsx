import React, {
	ChangeEvent,
	FC,
	useCallback,
	useContext,
	useState
} from 'react';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { Box, Button, Typography } from '@mui/material';
import { useQueryClient } from 'react-query';
import { AxiosResponse } from 'axios';

import usePageTitle from '../hooks/usePageTitle';
import useLoggedInUser from '../hooks/useLoggedInUser';
import {
	useGetSampleCsv,
	useUploadAndReplaceProjects,
	useUploadNewProjects
} from '../hooks/api/useProjectsApi';
import ProjectsDataGrid from '../components/project/ProjectsDataGrid';
import UploadButtons from '../components/controls/UploadButtons';
import { useAlert } from '../hooks/useAppAlert';
import { handleDownload } from '../utils/utilFunctions';

/**
 * Projects management page
 * @constructor
 */
const ProjectsPage: FC = () => {
	// context
	usePageTitle('Manage projects');
	const qc = useQueryClient();
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);
	const user = useLoggedInUser();

	// state
	const [filename, setFilename] = useState('');
	const [file, setFile] = useState<File | null>();

	// api calls
	const uploadNewCall = useUploadNewProjects(token, qc, setAlertOptions);
	const uploadAllCall = useUploadAndReplaceProjects(token, qc, setAlertOptions);
	const getSampleCsvCall = useGetSampleCsv(token, setAlertOptions);

	// helpers
	const isAdmin = useCallback(
		() => user?.roles.map(r => r.name).includes('ROLE_ADMIN'),
		[user]
	);

	// handlers
	const handleFileUpload = (e: ChangeEvent<HTMLInputElement>) => {
		if (!e.target.files) {
			return;
		}
		const file = e.target.files[0];
		const { name } = file;
		setFilename(name);
		setFile(file);
		e.target.value = '';
	};

	const handleUploadNewProjects = async () => {
		const formData = new FormData();
		if (file) {
			formData.append('file', file);
			let message: string;
			try {
				message = await uploadNewCall.mutateAsync(formData);
			} catch {
				console.error('Failed to upload new projects');
				setFilename('Failed to upload new projects');
				return;
			}
			setFilename(message);
			setFile(null);
		} else {
			setFilename('Please select a csv file first');
		}
	};

	const handleUploadAndReplaceProjects = async () => {
		const formData = new FormData();
		if (file) {
			formData.append('file', file);
			let message: string;
			try {
				message = await uploadAllCall.mutateAsync(formData);
			} catch {
				console.error('Failed to upload and replace new projects');
				setFilename('Failed to upload and replace new projects');
				return;
			}
			setFilename(message);
			setFile(null);
		} else {
			setFilename('Please select a csv file first');
		}
	};

	const handleCsvDownload = async () => {
		let data: AxiosResponse<Blob>;
		try {
			data = await getSampleCsvCall.mutateAsync();
		} catch {
			console.error('Failed to download sample CSV file');
			return;
		}
		handleDownload(data, 'text/csv', 'csv');
	};

	return isAdmin() ? (
		<>
			<Box
				sx={{
					backgroundColor: '#E8E8EEFF',
					borderRadius: 2,
					px: 2,
					my: -1
				}}
			>
				<Typography variant="h5">Manage Masaryk University projects</Typography>
			</Box>
			<UploadButtons
				handleFileUpload={handleFileUpload}
				handleUploadNew={handleUploadNewProjects}
				handleUploadAndReplace={handleUploadAndReplaceProjects}
				isLoading={uploadNewCall.isLoading || uploadAllCall.isLoading}
				fileName={filename}
			>
				<Button
					sx={{ mx: 1, color: 'black', fontSize: 10 }}
					onClick={() => handleCsvDownload()}
				>
					Download sample CSV
				</Button>
			</UploadButtons>
			<ProjectsDataGrid selection={false} serverSide administrate />
		</>
	) : (
		<Typography>You are unauthorized for the management section</Typography>
	);
};

export default ProjectsPage;
