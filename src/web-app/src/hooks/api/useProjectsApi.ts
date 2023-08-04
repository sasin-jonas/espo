import { QueryClient, useMutation, useQuery } from 'react-query';
import { AxiosError, AxiosResponse } from 'axios';

import {
	AuthorDto,
	DepartmentDto,
	ProjectDto,
	ProjectPageable
} from '../../types/Project.Types';
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
	postObject,
	putObject
} from './useApi';

/**
 * Get the first page of projects
 * @param token The bearer token
 */
export const useGetFirstPageProjects = (token: string) =>
	useQuery<AxiosResponse<ProjectPageable>, AxiosError<ErrorResponse>>(
		['list', 'projects'],
		() => getObjects<ProjectPageable>(apiRoutes.projectsUrl, token)
	);

/**
 * Get filtered projects
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetFilteredProjects = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<AxiosResponse<ProjectPageable>, AxiosError<ErrorResponse>, any>(
		['filteredList', 'projects'],
		(params: any) =>
			getObjectsQueryParams<ProjectPageable>(
				apiRoutes.projectsUrl,
				token,
				params
			),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['filteredList', 'projects']);
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
 * Delete a project
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useDeleteProject = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<void, AxiosError<ErrorResponse>, any>(
		(projectId: string) =>
			deleteObject(`${apiRoutes.projectsUrl}/${projectId}`, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'projects']);
				await qc.invalidateQueries(['filteredList', 'projects']);
				setAlertOptions({
					open: true,
					successMessage: 'Project deleted successfully',
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
 * Upload a project
 * @param qc The query client
 * @param token The bearer token
 * @param projectId The project id
 * @param setAlertOptions The function for setting the alert options
 */
export const useUpdateProject = (
	qc: QueryClient,
	token: string,
	projectId: number,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<void, AxiosError<ErrorResponse>, any>(
		(r: ProjectDto) =>
			putObject(`${apiRoutes.projectsUrl}/${projectId}`, token, r),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'projects']);
				await qc.invalidateQueries(['filteredList', 'projects']);
				setAlertOptions({
					open: true,
					successMessage: 'Project updated successfully',
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
 * Get projects by author
 * @param token The bearer token
 * @param qc	The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetProjectsByAuthor = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<AxiosResponse<ProjectDto[]>, AxiosError<ErrorResponse>, string>(
		['authorList', 'projects'],
		(uco: string) =>
			getObjects<ProjectDto[]>(`${apiRoutes.projectsUrl}/author/${uco}`, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['authorList', 'projects']);
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
 * Get all project authors
 * @param token The bearer token
 */
export const useGetAllAuthors = (token: string) =>
	useQuery<AxiosResponse<AuthorDto[]>, AxiosError<ErrorResponse>>(
		['list', 'authors'],
		() => getObjects<AuthorDto[]>(apiRoutes.authorsUrl, token)
	);

/**
 * Get sample csv for projects upload
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
		['sampleCsv'],
		() => getBlob<Blob>(`${apiRoutes.projectsUrl}/example-csv`, token),
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

/**
 * Get sample json for projects upload
 * @param token The bearer token
 * @param setAlertOptions The function for setting the alert options
 */
export const useGetSampleJson = (
	token: string,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<AxiosResponse<Blob>, AxiosError<ErrorResponse>, void>(
		['sampleJson'],
		() => getBlob<Blob>(`${apiRoutes.projectsUrl}/example-json`, token),
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

/**
 * Get all project departments
 * @param token	The bearer token
 */
export const useGetAllDepartments = (token: string) =>
	useQuery<AxiosResponse<DepartmentDto[]>, AxiosError<ErrorResponse>>(
		['list', 'departments'],
		() => getObjects<DepartmentDto[]>(apiRoutes.departmentsUrl, token)
	);

/**
 * Upload new projects
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useUploadNewProjects = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<string, AxiosError<ErrorResponse>, FormData>(
		['uploadAllP'],
		(r: FormData) => postObject(`${apiRoutes.projectsUrl}/load`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'projects']);
				await qc.invalidateQueries(['filteredList', 'projects']);
				setAlertOptions({
					open: true,
					successMessage: 'Projects uploaded successfully',
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
 * Upload and replace projects
 * @param token The bearer token
 * @param qc The query client
 * @param setAlertOptions The function for setting the alert options
 */
export const useUploadAndReplaceProjects = (
	token: string,
	qc: QueryClient,
	setAlertOptions: (
		value: ((prevState: AppAlertOptions) => AppAlertOptions) | AppAlertOptions
	) => void
) =>
	useMutation<string, AxiosError<ErrorResponse>, FormData>(
		['uploadAllPR'],
		(r: FormData) => postObject(`${apiRoutes.projectsUrl}/load-all`, r, token),
		{
			onSuccess: async () => {
				await qc.invalidateQueries(['list', 'projects']);
				await qc.invalidateQueries(['filteredList', 'projects']);
				setAlertOptions({
					open: true,
					successMessage: 'Projects uploaded and replaced successfully',
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
