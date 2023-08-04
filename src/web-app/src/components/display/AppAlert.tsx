import { FC, useCallback } from 'react';
import { Alert, Snackbar } from '@mui/material';

import { AppAlertProps, AppAlertTypes } from '../../types/Alert.Types';

/**
 * Application alert component
 * @param open Open state flag
 * @param severity Alert severity
 * @param successMessage Success message
 * @param error Error response
 * @param setAppAlertOptions Function to set alert options
 * @constructor
 */
const AppAlert: FC<AppAlertProps> = ({
	open,
	severity = AppAlertTypes.Info,
	successMessage = '',
	error,
	setAppAlertOptions
}) => {
	const handleClose = useCallback(() => {
		setAppAlertOptions({
			open: false,
			successMessage: '',
			error: undefined
		});
	}, []);

	const constructErrorMessage = () => {
		let response = error?.response;
		if (!response) {
			return 'Unknown error has occurred';
		}

		let errorMessage = '';
		if (response.status === 500) {
			return 'Internal server error';
		} else if (response.status === 401) {
			return 'Unauthorized request, try refreshing the page';
		} else if (response.status === 403) {
			return 'You are unauthorized for this operation';
		} else {
			if (response.data?.error && response.status !== 400) {
				errorMessage = errorMessage.concat(response.data?.error);
			}
			if (response.data?.message) {
				if (errorMessage.length === 0) {
					errorMessage = response.data?.message;
				} else {
					errorMessage = errorMessage
						.concat(': ')
						.concat(response.data?.message);
				}
			}
			if (!response.data?.error && !response.data?.message) {
				return 'Unknown error has occurred';
			}
		}
		return errorMessage;
	};

	const constructAlertMessage = () => {
		if (!error) {
			return successMessage;
		} else {
			if (error?.response) {
				return constructErrorMessage();
			} else {
				return error.message;
			}
		}
	};

	let message = constructAlertMessage();

	return (
		<Snackbar
			open={open}
			autoHideDuration={3000}
			onClose={handleClose}
			anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
			sx={{ mb: 6 }}
		>
			<Alert onClose={handleClose} severity={severity} sx={{ width: '100%' }}>
				{message}
			</Alert>
		</Snackbar>
	);
};

export default AppAlert;
