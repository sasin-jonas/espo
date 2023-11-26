import {FC, useCallback, useContext, useEffect, useRef, useState} from 'react';
import {Box, Tooltip} from '@mui/material';
import {
	DataGrid,
	GridColDef,
	GridFilterItem,
	GridFilterModel,
	GridLinkOperator,
	GridRenderCellParams,
	GridSelectionModel,
	GridSortItem,
	GridSortModel
} from '@mui/x-data-grid';
import {Visibility} from '@mui/icons-material';
import {useQueryClient} from 'react-query';
import {AuthContext, IAuthContext} from 'react-oauth2-code-pkce';
import EditIcon from '@mui/icons-material/Edit';
import {AxiosResponse} from 'axios';

import {AuthorDto, DepartmentDto, ProjectDto, ProjectPageable} from '../../types/Project.Types';
import {
	useDeleteProject,
	useGetAllAuthors,
	useGetAllDepartments,
	useGetFilteredProjects,
	useGetFirstPageProjects
} from '../../hooks/api/useProjectsApi';
import {
	checkRemainingTime,
	containsOnlyFilterOperators,
	filterChangeTimeout,
	getPageNumber,
	handleResetAllTableFiltersAndSort,
	isDescString
} from '../../utils/utilFunctions';
import {useAlert} from '../../hooks/useAppAlert';
import {AppAlertTypes} from '../../types/Alert.Types';
import DeleteButtonWithConfirmDialog from '../controls/DeleteButtonWithConfirmDialog';

import ProjectDetail from './ProjectDetail';
import ProjectEdit from './ProjectEdit';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

type Props = {
    rows?: ProjectDto[];
    pageSize?: number;
    loading?: boolean;
    onGridSelectionChange?: (ids: GridSelectionModel) => void;
    onPageSizeChange?: (size: number) => void;
    administrate: boolean;
    selection: boolean;
    serverSide: boolean;
    showScore?: boolean;
    selectionModel?: GridSelectionModel;
};

/**
 * Projects data grid
 * @param rows Rows to display
 * @param onGridSelectionChange Callback for when the selection changes
 * @param loading Loading state flag
 * @param selection Selection enabled flag
 * @param serverSide Server-side flag
 * @param administrate If update and delete buttons should be shown
 * @param showScore If the table will show project score instead of author uco
 * @param selectionModel Current selection
 * @constructor
 */
const ProjectsDataGrid: FC<Props> = ({
                                         rows,
                                         onGridSelectionChange,
                                         loading,
                                         selection,
                                         serverSide,
                                         administrate,
                                         showScore,
                                         selectionModel
                                     }) => {
    // context
    const qc = useQueryClient();
    const {token} = useContext<IAuthContext>(AuthContext);
    const [, setAlertOptions] = useAlert();

    // filter options
    const [queryOptions, setQueryOptions] = useState<
        GridFilterItem | undefined
    >();
    const [sortOptions, setSortOptions] = useState<GridSortItem | undefined>();
    const [pageSize, setPageSize] = useState<number>(10);
    const [page, setPage] = useState<number>(0);
    const [filterModelPtr, setFilterModelPtr] = useState<GridFilterModel>({
        items: [],
        linkOperator: GridLinkOperator.And,
        quickFilterLogicOperator: GridLinkOperator.And,
        quickFilterValues: []
    });
    const [sortModelPtr, setSortModelPtr] = useState<GridSortModel>([]);

    // specifies if the initial first page request has been made
    const [filtered, setFiltered] = useState(false);

    // api call
    const projectsDataResult = useGetFirstPageProjects(token);
    const authorsResult = useGetAllAuthors(token);
    const departmentsResult = useGetAllDepartments(token);
    const filterProjectsCall = useGetFilteredProjects(token, qc, setAlertOptions);
    const deleteProjectCall = useDeleteProject(token, qc, setAlertOptions);

    // data
    const [projectRows, setProjectRows] = useState<ProjectDto[]>([]);
    const [rowCount, setRowCount] = useState(0);
    const [authors, setAuthors] = useState<AuthorDto[]>();
    const [departments, setDepartments] = useState<DepartmentDto[]>();

    const timeoutRef = useRef<NodeJS.Timeout | null>(null);
    const remainingTimeRef = useRef<number | null>(null);

    // data for client-side filtering and paging
    useEffect(() => {
        if (!serverSide) {
            setProjectRows(rows ?? []);
            setRowCount(rows?.length ?? 0);
        }
    }, [rows]);

    // default data (first page, unfiltered)
    useEffect(() => {
        if (serverSide && !filtered) {
            setProjectRows(
                projectsDataResult.data ? projectsDataResult.data.data.content : []
            );
            setRowCount(
                projectsDataResult.data ? projectsDataResult.data.data.totalElements : 0
            );
        }
    }, [projectsDataResult.data]);

    useEffect(() => {
        if (projectsDataResult.isError) {
            setAlertOptions({
                open: true,
                error: projectsDataResult.error,
                severity: AppAlertTypes.Error
            });
        }
    }, [projectsDataResult.isError]);

    useEffect(() => {
        if (authorsResult.data) {
            setAuthors(authorsResult.data.data);
        }
    }, [authorsResult.data]);
    useEffect(() => {
        if (departmentsResult.data) {
            setDepartments(departmentsResult.data.data);
        }
    }, [departmentsResult.data]);

    useEffect(() => {
        if (authorsResult.isError) {
            setAlertOptions({
                open: true,
                error: authorsResult.error,
                severity: AppAlertTypes.Error
            });
        } else if (departmentsResult.isError) {
            setAlertOptions({
                open: true,
                error: departmentsResult.error,
                severity: AppAlertTypes.Error
            });
        }
    }, [authorsResult.isError, departmentsResult.isError]);

    useEffect(() => {
        checkRemainingTime(remainingTimeRef, timeoutRef);
    }, [queryOptions]);

    useEffect(() => {
        const handleKeyDown = async (event: { keyCode: number }) => {
            await handleResetAllTableFiltersAndSort(
                event,
                onFilterChange,
                'title',
                onSortChange,
                onGridSelectionChange
            );
        };

        document.addEventListener('keydown', handleKeyDown);

        // Don't forget to clean up
        return function cleanup() {
            document.removeEventListener('keydown', handleKeyDown);
        };
    }, []);

    const onFilter = async (
        p?: number,
        ps?: number,
        so?: GridSortItem,
        qo?: GridFilterItem
    ) => {
        if (!serverSide) {
            return;
        }
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
        params[
            `${filterField?.replace('department_', '').replace('author_', '')}`
            ] = filterValue;

        let filteredProjectsResult: AxiosResponse<ProjectPageable>;
        try {
            filteredProjectsResult = await filterProjectsCall.mutateAsync(params);
        } catch {
            console.error('Failed to fetch projects');
            return;
        }
        const filteredProjects = filteredProjectsResult.data;
        setFiltered(true);
        setProjectRows(filteredProjects.content);
        setRowCount(filteredProjects.totalElements);
        setPage(pageNum ?? 0);
    };

    const onFilterChange = async (filterModel: GridFilterModel) => {
        setFilterModelPtr(filterModel);
        if (filterModel.items.length > 0) {
            await onFilter(undefined, undefined, undefined, filterModel.items[0]);
            setQueryOptions(filterModel.items[0] ? filterModel.items[0] : undefined);
        } else {
            setQueryOptions(undefined);
        }
    };

    const onSortChange = async (sortModel: GridSortModel) => {
        setSortModelPtr(sortModel);
        if (sortModel.length > 0) {
            await onFilter(undefined, undefined, sortModel[0], undefined);
            setSortOptions(sortModel[0].field ? sortModel[0] : undefined);
        } else {
            setSortOptions(undefined);
        }
    };

    const onSelectionModelChange = (ids: GridSelectionModel) =>
        onGridSelectionChange ? onGridSelectionChange(ids) : undefined;

    const onProjectDelete = useCallback(async (value: number | undefined) => {
        if (value) {
            try {
                await deleteProjectCall.mutateAsync(String(value));
            } catch {
                console.error('Failed to delete project');
            }
            await onFilter();
        }
    }, []);

    const ucoCell: GridColDef = {
        field: 'author_uco',
        headerName: 'Uco',
        headerClassName: 'column--header--theme',
        filterOperators: containsOnlyFilterOperators,
        width: 100,
        valueGetter: params => {
            const result: string[] = [];
            if (params.row.author.uco) {
                result.push(`${params.row.author.uco}`);
            }
            return result.join(', ');
        }
    };
    const scoreCell: GridColDef = {
        field: 'score',
        headerName: 'Score',
        headerClassName: 'column--header--theme',
        filterOperators: undefined,
        width: 100,
        valueGetter: params => {
            return (Math.round(params.row.score * 100) / 100).toFixed(3);
        }
    };
    const columns: GridColDef[] = [
        {
            field: 'regCode',
            headerName: 'Reg. Code',
            headerClassName: 'column--header--theme',
            filterOperators: containsOnlyFilterOperators,
            width: 100
        },
        {
            field: 'title',
            headerName: 'Title',
            flex: 0.55,
            headerClassName: 'column--header--theme',
            filterOperators: containsOnlyFilterOperators,
            minWidth: 200
        },
        showScore ? scoreCell : ucoCell,
        {
            field: 'department_orgUnit',
            headerName: 'Unit',
            headerClassName: 'column--header--theme',
            filterOperators: containsOnlyFilterOperators,
            width: 80,
            valueGetter: params => {
                const result: string[] = [];
                if (params.row.department) {
                    result.push(`${params.row.department.orgUnit}`);
                }
                return result.join(', ');
            }
        },
        {
            field: 'department_departmentName',
            headerName: 'Department',
            headerClassName: 'column--header--theme',
            filterOperators: containsOnlyFilterOperators,
            width: 200,
            flex: 0.25,
            valueGetter: params => {
                const result: string[] = [];
                if (params.row.department) {
                    result.push(`${params.row.department.departmentName}`);
                }
                return result.join(', ');
            }
        },
        {
            field: 'muniRole',
            headerName: 'MU role',
            flex: 0.2,
            headerClassName: 'column--header--theme',
            filterOperators: containsOnlyFilterOperators,
            minWidth: 150
        },
        {
            field: 'id',
            headerName: '',
            filterable: false,
            sortable: false,
            minWidth: selection || !administrate ? 50 : 230,
            headerClassName: 'column--header--theme',
            renderCell: (params: GridRenderCellParams<number>) => (
                <strong>
                    <ProjectDetail project={projectRows.find(p => p.id === params.value)}>
                        <Tooltip enterDelay={1000} title="See project details">
                            <Visibility/>
                        </Tooltip>
                    </ProjectDetail>
                    {!selection && administrate && (
                        <>
                            <ProjectEdit
                                project={projectRows.find(u => u.id === params.value)}
                                onUpdate={onFilter}
                                authors={authors}
                                departments={departments}
                            >
                                <EditIcon/>
                            </ProjectEdit>
                            <DeleteButtonWithConfirmDialog
                                onClick={() => onProjectDelete(params.value)}
                            >
                                Really want to delete project with registration code{' '}
                                {`'${projectRows.find(p => p.id === params.value)?.regCode}'`}?
                            </DeleteButtonWithConfirmDialog>
                        </>
                    )}
                </strong>
            )
        }
    ];

    return (
        <Box sx={{width: '100%'}}>
            <Tooltip title="Clear all table filters by pressing 'F1'">
                <QuestionMarkIcon fontSize="small" sx={{maxHeight: 10}}/>
            </Tooltip>
            <DataGrid
                filterModel={filterModelPtr}
                sortModel={sortModelPtr}
                autoHeight
                rows={projectRows}
                onSelectionModelChange={onSelectionModelChange}
                selectionModel={selectionModel}
                keepNonExistentRowsSelected
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
                rowCount={rowCount}
                page={page}
                rowsPerPageOptions={[5, 10, 20, 50]}
                pagination
                loading={!serverSide ? loading : filterProjectsCall.isLoading}
                checkboxSelection={selection}
                disableSelectionOnClick
                experimentalFeatures={{newEditingApi: true}}
                sx={{
                    '& .column--header--theme': {
                        backgroundColor: '#d4d4d5'
                    },
                    '& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer':
                        {
                            display: 'none'
                        }
                }}
                paginationMode={serverSide ? 'server' : 'client'}
                filterMode={serverSide ? 'server' : 'client'}
                sortingMode={serverSide ? 'server' : 'client'}
            />
        </Box>
    );
};

export default ProjectsDataGrid;
