import { AxiosError } from 'axios';

export enum AppAlertTypes {
	Error = 'error',
	Success = 'success',
	Warning = 'warning',
	Info = 'info'
}

export type AppAlertOptions = {
	open: boolean;
	severity?: AppAlertTypes;
	successMessage?: string;
	error?: AxiosError<ErrorResponse>;
};

export type ErrorResponse = {
	message?: string;
	error?: string;
};

export type AppAlertSetter = {
	setAppAlertOptions: (e: AppAlertOptions) => void;
};

export type AppAlertProps = AppAlertOptions & AppAlertSetter;
