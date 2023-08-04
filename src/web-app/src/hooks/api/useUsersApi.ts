import { QueryClient, useMutation, useQuery } from 'react-query';
import { AxiosError, AxiosResponse } from 'axios';

import { UserDto } from '../../types/User.Types';
import {
	AppAlertOptions,
	AppAlertTypes,
	ErrorResponse
} from '../../types/Alert.Types';

import { apiRoutes } from './apiRoutes';
import { getObjects, putObject } from './useApi';

/**
 * Get all users
 * @param token The bearer token
 */
export const useGetAllUsers = (token: string) =>
	useQuery<AxiosResponse<UserDto[]>, AxiosError<ErrorResponse>>(
		['list', 'users'],
		() => getObjects<UserDto[]>(apiRoutes.usersUrl, token)
	);

/**
 * Get my user info
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetMyUserInfo = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<AxiosResponse<UserDto>, AxiosError<ErrorResponse>, string>(
		['me'],
		() => getObjects<UserDto>(`${apiRoutes.usersUrl}/me`, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['me']);
			},
			onError: error => {
				setAlertOptions({
					open: true,
					error,
					severity: AppAlertTypes.Error
				});
			}
		}
	);

/**
 * Get admin emails
 * @param token The bearer token
 */
export const useGetAdminEmails = (token: string) =>
	useQuery<AxiosResponse<string[]>, AxiosError<ErrorResponse>>(
		['list', 'adminEmails'],
		() => getObjects<string[]>(`${apiRoutes.usersUrl}/admins`, token)
	);

/**
 * Update a user
 * @param qc The query client
 * @param token The bearer token
 * @param userId The user id
 * @param setAlertOptions The function for setting the alert options
 */
export const useUpdateUser = (
	qc: QueryClient,
	token: string,
	userId: number,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<void, AxiosError<ErrorResponse>, UserDto>(
		(r: UserDto) => putObject(`${apiRoutes.usersUrl}/${userId}`, token, r),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'users']);
				await qc.invalidateQueries(['me']);
				setAlertOptions({
					open: true,
					successMessage: 'User updated successfully',
					severity: AppAlertTypes.Success
				});
			},
			onError: error => {
				setAlertOptions({
					open: true,
					error,
					severity: AppAlertTypes.Error
				});
			}
		}
	);
