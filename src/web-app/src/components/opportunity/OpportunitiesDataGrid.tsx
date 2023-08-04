import { FC, useContext, useEffect, useRef, useState } from 'react';
import { Box, Tooltip } from '@mui/material';
import {
	DataGrid,
	GridColDef,
	GridFilterItem,
	GridFilterModel,
	GridRenderCellParams,
	GridSortItem,
	GridSortModel
} from '@mui/x-data-grid';
import { Visibility } from '@mui/icons-material';
import { useQueryClient } from 'react-query';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { AxiosResponse } from 'axios';

import {
	useDeleteOpportunity,
	useGetFilteredOpportunities,
	useGetOpportunitiesFirstPage
} from '../../hooks/api/useOpportunityApi';
import {
	OpportunityDto,
	OpportunityPageable
} from '../../types/Opportunity.Types';
import {
	checkRemainingTime,
	containsOnlyFilterOperators,
	filterChangeTimeout,
	getPageNumber,
	isDescString
} from '../../utils/utilFunctions';
import DeleteButtonWithConfirmDialog from '../controls/DeleteButtonWithConfirmDialog';
import { useAlert } from '../../hooks/useAppAlert';
import { AppAlertTypes } from '../../types/Alert.Types';

import OpportunityDetail from './OpportunityDetail';

/**
 * Opportunities data grid
 * @constructor
 */
const OpportunitiesDataGrid: FC = () => {
	// context
	const qc = useQueryClient();
	const { token } = useContext<IAuthContext>(AuthContext);
	const [, setAlertOptions] = useAlert();

	// data
	const [rows, setRows] = useState<OpportunityDto[]>([]);
	const [rowCount, setRowCount] = useState(0);

	// filter, sort & paging options
	const [pageSize, setPageSize] = useState(10);
	const [page, setPage] = useState(0);
	const [queryOptions, setQueryOptions] = useState<
		GridFilterItem | undefined
	>();
	const [sortOptions, setSortOptions] = useState<GridSortItem | undefined>();

	// specifies if the initial first page request has been made
	const [filtered, setFiltered] = useState(false);

	// api calls
	const deleteOpportunityCall = useDeleteOpportunity(
		token,
		qc,
		setAlertOptions
	);
	const filterOpportunitiesCall = useGetFilteredOpportunities(
		token,
		qc,
		setAlertOptions
	);
	const opportunitiesFirstPage = useGetOpportunitiesFirstPage(token);

	// refs
	const timeoutRef = useRef<NodeJS.Timeout | null>(null);
	const remainingTimeRef = useRef<number | null>(null);

	// useEffects & handlers
	useEffect(() => {
		if (!filtered) {
			setRows(
				opportunitiesFirstPage.data
					? opportunitiesFirstPage.data.data.content
					: []
			);
			setRowCount(
				opportunitiesFirstPage.data
					? opportunitiesFirstPage.data.data.totalElements
					: 0
			);
		}
	}, [opportunitiesFirstPage.data]);
	useEffect(() => {
		if (opportunitiesFirstPage.isError) {
			setAlertOptions({
				open: true,
				error: opportunitiesFirstPage.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [opportunitiesFirstPage.isError]);
	useEffect(() => {
		checkRemainingTime(remainingTimeRef, timeoutRef);
	}, [queryOptions]);

	const deleteOpportunityHandler = async (value: string | undefined) => {
		if (value) {
			const toDelete = rows.find(p => p.esId === value);
			if (toDelete) {
				try {
					await deleteOpportunityCall.mutateAsync(toDelete.esId);
				} catch {
					console.error('Failed to delete opportunity');
				}
				await onFilter();
			}
		}
	};

	const onFilter = async (
		p?: number,
		ps?: number,
		so?: GridSortItem,
		qo?: GridFilterItem
	) => {
		if (qo) {
			// If the timeout is still active
			if (timeoutRef.current !== null) {
				remainingTimeRef.current = Date.now() + filterChangeTimeout;
				return;
			}
			timeoutRef.current = setTimeout(() => {
				timeoutRef.current = null;
				remainingTimeRef.current = null;
			}, filterChangeTimeout);
		}

		const filterField = qo ? qo.columnField : queryOptions?.columnField;
		const filterValue = qo ? qo.value : queryOptions?.value;
		const sortField = so ? so.field : sortOptions?.field;
		const desc = isDescString(so, sortOptions);
		const pageNum = getPageNumber(qo, p, page);

		const params: { [k: string]: any } = {};
		params.page = pageNum;
		params.size = ps ?? pageSize;
		params.sortBy = sortField;
		params.desc = desc;
		params.filterField = filterField;
		params.filterValue = filterValue;

		let filteredOpportunitiesResult: AxiosResponse<OpportunityPageable>;
		try {
			filteredOpportunitiesResult = await filterOpportunitiesCall.mutateAsync(
				params
			);
		} catch {
			console.error('Failed to fetch opportunities data');
			return;
		}
		const filteredOpportunities = filteredOpportunitiesResult.data;
		setFiltered(true);
		setRows(filteredOpportunities.content);
		setRowCount(filteredOpportunities.totalElements);
		setPage(pageNum ?? 0);
	};

	const onFilterChange = async (filterModel: GridFilterModel) => {
		if (filterModel.items.length > 0) {
			await onFilter(undefined, undefined, undefined, filterModel.items[0]);
			setQueryOptions(filterModel.items[0] ? filterModel.items[0] : undefined);
		} else {
			setQueryOptions(undefined);
		}
	};

	const onSortChange = async (sortModel: GridSortModel) => {
		if (sortModel.length > 0) {
			await onFilter(undefined, undefined, sortModel[0], undefined);

			setSortOptions(sortModel[0].field ? sortModel[0] : undefined);
		} else {
			setSortOptions(undefined);
		}
	};

	// column definitions
	const columns: GridColDef[] = [
		{
			field: 'title',
			headerName: 'Title',
			flex: 0.8,
			sortable: false,
			headerClassName: 'column--header--theme',
			filterOperators: containsOnlyFilterOperators,
			minWidth: 200
		},
		{
			field: 'institutionName',
			headerName: 'Institution',
			flex: 0.2,
			headerClassName: 'column--header--theme',
			filterOperators: containsOnlyFilterOperators,
			minWidth: 200
		},
		{
			field: 'author',
			headerName: 'Author name',
			flex: 0.2,
			headerClassName: 'column--header--theme',
			filterOperators: containsOnlyFilterOperators,
			minWidth: 200
		},
		{
			field: 'esId',
			headerName: '',
			filterable: false,
			sortable: false,
			minWidth: 150,
			headerClassName: 'column--header--theme',
			renderCell: (params: GridRenderCellParams<string>) => (
				<strong>
					<OpportunityDetail
						opportunity={rows.find(p => p.esId === params.value)}
					>
						<Tooltip enterDelay={1000} title="See opportunity details">
							<Visibility />
						</Tooltip>
					</OpportunityDetail>
					<DeleteButtonWithConfirmDialog
						onClick={() => deleteOpportunityHandler(params.value)}
					>
						Really want to delete opportunity{' '}
						{`'${rows.find(p => p.esId === params.value)?.title}'`}?
					</DeleteButtonWithConfirmDialog>
				</strong>
			)
		}
	];
	return (
		<Box sx={{ width: '100%' }}>
			<DataGrid
				autoHeight
				rows={rows}
				columns={columns}
				pageSize={pageSize}
				onPageSizeChange={async newSize => {
					await onFilter(undefined, newSize, undefined, undefined);
					setPageSize(newSize);
				}}
				onPageChange={async page => {
					await onFilter(page, undefined, undefined, undefined);
					setPage(page);
				}}
				onFilterModelChange={onFilterChange}
				onSortModelChange={onSortChange}
				rowsPerPageOptions={[5, 10, 20, 50]}
				rowCount={rowCount}
				pagination
				page={page}
				loading={filterOpportunitiesCall.isLoading}
				checkboxSelection={false}
				disableSelectionOnClick
				getRowId={row => row.esId}
				experimentalFeatures={{ newEditingApi: true }}
				sx={{
					'& .column--header--theme': {
						backgroundColor: '#d4d4d5'
					}
				}}
				paginationMode="server"
				filterMode="server"
				sortingMode="server"
			/>
		</Box>
	);
};

export default OpportunitiesDataGrid;
