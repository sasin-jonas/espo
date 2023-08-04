import { QueryClient, useMutation, useQuery } from 'react-query';
import { AxiosError, AxiosResponse } from 'axios';

import { OpportunityPageable } from '../../types/Opportunity.Types';
import {
	AppAlertOptions,
	AppAlertTypes,
	ErrorResponse
} from '../../types/Alert.Types';

import { apiRoutes } from './apiRoutes';
import {
	deleteObject,
	getBlob,
	getObjects,
	getObjectsQueryParams,
	postObject
} from './useApi';

/**
 * Get the first page of opportunities
 * @param token
 */
export const useGetOpportunitiesFirstPage = (token: string) =>
	useQuery<AxiosResponse<OpportunityPageable>, AxiosError<ErrorResponse>>(
		['list', 'opportunities'],
		() => getObjects<OpportunityPageable>(`${apiRoutes.searchAll}`, token)
	);

/**
 * Get filtered opportunities
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetFilteredOpportunities = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<
		AxiosResponse<OpportunityPageable>,
		AxiosError<ErrorResponse>,
		any
	>(
		['filteredList', 'opportunities'],
		(params: any) =>
			getObjectsQueryParams<OpportunityPageable>(
				apiRoutes.searchAll,
				token,
				params
			),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['filteredList', 'opportunities']);
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
 * Delete an opportunity
 * @param token The bearer token
 * @param qc 	The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useDeleteOpportunity = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<void, AxiosError<ErrorResponse>, any>(
		(esId: string) =>
			deleteObject(`${apiRoutes.opportunitiesUrl}/${esId}`, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'opportunities']);
				await qc.invalidateQueries(['filteredList', 'opportunities']);
				setAlertOptions({
					open: true,
					successMessage: 'Delete successful',
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

/**
 * Upload new opportunities
 * @param token The bearer token
 * @param qc 	The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useUploadNewOpportunities = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<string, AxiosError<ErrorResponse>, FormData>(
		['uploadAllO'],
		(r: FormData) => postObject(`${apiRoutes.opportunitiesUrl}/load`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'opportunities']);
				await qc.invalidateQueries(['filteredList', 'opportunities']);
				setAlertOptions({
					open: true,
					successMessage: 'Opportunities uploaded successfully',
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

/**
 * Upload and replace opportunities
 * @param token The bearer token
 * @param qc 	The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useUploadAndReplaceOpportunities = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<string, AxiosError<ErrorResponse>, FormData>(
		['uploadAllOR'],
		(r: FormData) =>
			postObject(`${apiRoutes.opportunitiesUrl}/load-all`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'opportunities']);
				await qc.invalidateQueries(['filteredList', 'opportunities']);
				setAlertOptions({
					open: true,
					successMessage: 'Opportunities uploaded and replaced successfully',
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

/**
 * Get the sample csv
 * @param token The bearer token
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetSampleCsv = (
	token: string,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<AxiosResponse<Blob>, AxiosError<ErrorResponse>, void>(
		['sampleCsvOpportunities'],
		() => getBlob<Blob>(`${apiRoutes.opportunitiesUrl}/example-csv`, token),
		{
			onError: error => {
				setAlertOptions({
					open: true,
					error,
					severity: AppAlertTypes.Error
				});
			}
		}
	);
