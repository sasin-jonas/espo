import { QueryClient, useMutation, useQuery } from 'react-query';
import { AxiosError, AxiosResponse } from 'axios';

import { OpportunityDto } from '../../types/Opportunity.Types';
import {
	AppAlertOptions,
	AppAlertTypes,
	ErrorResponse
} from '../../types/Alert.Types';
import {
	OpportunitySearchResultDto,
	SearchProjectDto
} from '../../types/Search.Types';

import { apiRoutes } from './apiRoutes';
import { getObjects, postObject } from './useApi';
import { GridRowId } from '@mui/x-data-grid';

/**
 * Get all filter values
 * @param token The bearer token
 */
export const useGetAllFilterValues = (token: string) =>
	useQuery<
		AxiosResponse<{ [id: string]: string[] }>,
		AxiosError<ErrorResponse>
	>(['map', 'filters'], () =>
		getObjects<{ [id: string]: string[] }>(
			`${apiRoutes.searchUrl}/unique-filters`,
			token
		)
	);

/**
 * Search by phrase
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useSearchByPhrase = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<OpportunityDto[], AxiosError<ErrorResponse>, SearchProjectDto>(
		['projectsSearchResult'],
		(r: SearchProjectDto) =>
			postObject(`${apiRoutes.searchUrl}/byPhrase`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries('projectsSearchResult');
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
 * Search by projects
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useSearchByProjects = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<OpportunityDto[], AxiosError<ErrorResponse>, SearchProjectDto>(
		['projectsSearchResult'],
		(r: SearchProjectDto) =>
			postObject(`${apiRoutes.searchUrl}/byProjects`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries('projectsSearchResult');
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
 * Search by authors
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useSearchByAuthors = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<OpportunityDto[], AxiosError<ErrorResponse>, SearchProjectDto>(
		['projectsSearchResult'],
		(r: SearchProjectDto) =>
			postObject(`${apiRoutes.searchUrl}/byAuthors`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries('projectsSearchResult');
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
 * Search by opportunities
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useSearchByOpportunity = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<
		OpportunitySearchResultDto[],
		AxiosError<ErrorResponse>,
		GridRowId
	>(
		['opportunitySearchResult'],
		(r: GridRowId) =>
			postObject(`${apiRoutes.searchUrl}/byOpportunity`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries('opportunitySearchResult');
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
 * Search for more opportunities like this
 * @param token The bearer token
 * @param id The id of the opportunity
 */
export const useGetMoreLikeThis = (token: string, id: string) =>
	useQuery<AxiosResponse<OpportunityDto[]>, AxiosError<ErrorResponse>>(
		['moreLike', { id }],
		() => getObjects<OpportunityDto[]>(`${apiRoutes.recommendUrl}/${id}`, token)
	);
