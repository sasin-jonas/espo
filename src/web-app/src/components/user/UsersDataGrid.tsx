import {FC, useEffect, useState} from 'react';
import {Box, Tooltip} from '@mui/material';
import {DataGrid, GridColDef, GridFilterModel, GridLinkOperator, GridRenderCellParams} from '@mui/x-data-grid';
import EditIcon from '@mui/icons-material/Edit';

import {RoleDto, UserDto} from '../../types/User.Types';

import UserEdit from './UserEdit';
import {handleResetAllTableFilters} from '../../utils/utilFunctions';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

type Props = {
	rows: UserDto[];
	pageSize: number;
	loading: boolean;
	onPageSizeChange: (size: number) => void;
};

/**
 * Users data grid
 * @param rows Users to display
 * @param pageSize Number of rows per page
 * @param onPageSizeChange Callback when page size changes
 * @param loading Loading flag
 * @constructor
 */
const UsersDataGrid: FC<Props> = ({
	rows,
	pageSize,
	onPageSizeChange,
	loading
}) => {
	const [filterModelPtr, setFilterModelPtr] = useState<GridFilterModel>({
		items: [],
		linkOperator: GridLinkOperator.And,
		quickFilterLogicOperator: GridLinkOperator.And,
		quickFilterValues: []
	});

	useEffect(() => {
		const handleKeyDown = async (event: { keyCode: number }) => {
			await handleResetAllTableFilters(event, onFilterChange, 'name', undefined);
		};

		document.addEventListener('keydown', handleKeyDown);

		// Don't forget to clean up
		return function cleanup() {
			document.removeEventListener('keydown', handleKeyDown);
		};
	}, []);

	const onFilterChange = async (filterModel: GridFilterModel) => {
		setFilterModelPtr(filterModel);
	};

	const columns: GridColDef[] = [
		{
			field: 'uco',
			headerName: 'Uco',
			headerClassName: 'column--header--theme',
			flex: 0.2,
			width: 150
		},
		{
			field: 'name',
			headerName: 'Name',
			headerClassName: 'column--header--theme',
			flex: 0.25,
			width: 150
		},
		{
			field: 'email',
			headerName: 'E-mail',
			headerClassName: 'column--header--theme',
			flex: 0.25,
			width: 150
		},
		{
			field: 'roles',
			headerName: 'Roles',
			headerClassName: 'column--header--theme',
			flex: 0.3,
			width: 150,
			valueGetter: params => {
				const result: string[] = [];
				const roles: RoleDto[] = params.row.roles;
				if (roles) {
					result.push(`${roles.map(r => r.name).join(', ')}`);
				}
				return result.join(', ');
			}
		},
		{
			field: 'id',
			headerName: '',
			headerClassName: 'column--header--theme',
			renderCell: (params: GridRenderCellParams<number>) => (
				<strong>
					<UserEdit user={rows.find(u => u.id === params.value)}>
						<EditIcon />
					</UserEdit>
				</strong>
			)
		}
	];
	return (
		<Box sx={{ width: '100%' }}>
			<Tooltip title="Clear all table filters by pressing 'F1'">
				<QuestionMarkIcon fontSize="small" sx={{ maxHeight: 10 }} />
			</Tooltip>
			<DataGrid
				filterModel={filterModelPtr}
				onFilterModelChange={onFilterChange}
				autoHeight
				rows={rows}
				columns={columns}
				pageSize={pageSize}
				onPageSizeChange={onPageSizeChange}
				rowsPerPageOptions={[5, 10, 20, 50]}
				pagination
				loading={loading}
				disableSelectionOnClick
				experimentalFeatures={{ newEditingApi: true }}
				sx={{
					'& .column--header--theme': {
						backgroundColor: '#d4d4d5'
					}
				}}
			/>
		</Box>
	);
};

export default UsersDataGrid;
