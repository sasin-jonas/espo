import { ChangeEvent, FC, PropsWithChildren } from 'react';
import { Box, Button, CircularProgress, Grid } from '@mui/material';
import { UploadFile } from '@mui/icons-material';

type Props = PropsWithChildren<{
	handleFileUpload: (e: ChangeEvent<HTMLInputElement>) => void;
	handleUploadNew: () => void;
	handleUploadAndReplace: () => void;
	isLoading: boolean;
	fileName?: string;
}>;

/**
 * Upload buttons for selecting and uploading files
 * @param handleFileUpload On file upload handler
 * @param handleUploadNew On upload new click handler
 * @param handleUploadAndReplace On upload and replace click handler
 * @param isLoading Is loading flag
 * @param fileName File name
 * @param children Additional info like sample file download link
 * @constructor
 */
const UploadButtons: FC<Props> = ({
	handleFileUpload,
	handleUploadNew,
	handleUploadAndReplace,
	isLoading,
	fileName,
	children
}) => (
	<Grid container spacing={1}>
		<Grid item xs={12}>
			<Button
				component="label"
				startIcon={<UploadFile />}
				sx={{ marginRight: '1rem', color: 'black' }}
				variant="text"
			>
				Select file
				<input type="file" accept=".csv" hidden onInput={handleFileUpload} />
			</Button>
			{isLoading ? (
				<CircularProgress />
			) : (
				<Box sx={{ fontSize: 10 }}>{fileName}</Box>
			)}
		</Grid>
		<Grid item xs={12}>
			<Button
				onClick={handleUploadNew}
				variant="outlined"
				sx={{ mr: 1, color: 'black' }}
			>
				Upload new
			</Button>
			<Button
				onClick={handleUploadAndReplace}
				variant="outlined"
				sx={{ color: 'black' }}
			>
				Upload and replace
			</Button>
			{children}
		</Grid>
	</Grid>
);

export default UploadButtons;
