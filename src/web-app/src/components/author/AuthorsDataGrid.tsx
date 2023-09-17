import { FC, useEffect, useState } from 'react';
import { Box, Tooltip } from '@mui/material';
import {
	DataGrid,
	GridColDef,
	GridFilterModel,
	GridLinkOperator,
	GridSelectionModel
} from '@mui/x-data-grid';

import { AuthorDto } from '../../types/Project.Types';
import { handleResetAllTableFilters } from '../../utils/utilFunctions';
import QuestionMarkIcon from '@mui/icons-material/QuestionMark';

type Props = {
	rows: AuthorDto[];
	pageSize: number;
	loading: boolean;
	onGridSelectionChange: (ids: GridSelectionModel) => void;
	onPageSizeChange: (size: number) => void;
};

/**
 * DataGrid for project authors
 * @param rows Available authors
 * @param pageSize Page size
 * @param onGridSelectionChange On grid selection change handler
 * @param onPageSizeChange On page size change handler
 * @param loading Loading flag
 * @constructor
 */
const AuthorsDataGrid: FC<Props> = ({
	rows,
	pageSize,
	onGridSelectionChange,
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
			await handleResetAllTableFilters(event, onFilterChange, 'name');
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
			field: 'name',
			headerName: 'Name',
			headerClassName: 'column--header--theme',
			flex: 0.7,
			width: 150
		},
		{
			field: 'uco',
			headerName: 'Uco',
			headerClassName: 'column--header--theme',
			flex: 0.3,
			width: 150
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
				onSelectionModelChange={onGridSelectionChange}
				columns={columns}
				pageSize={pageSize}
				onPageSizeChange={onPageSizeChange}
				rowsPerPageOptions={[5, 10, 20, 50]}
				pagination
				loading={loading}
				checkboxSelection
				disableSelectionOnClick
				experimentalFeatures={{ newEditingApi: true }}
				sx={{
					'& .column--header--theme': {
						backgroundColor: '#d4d4d5'
					},
					'& .MuiDataGrid-columnHeaderCheckbox .MuiDataGrid-columnHeaderTitleContainer':
						{
							display: 'none'
						}
				}}
			/>
		</Box>
	);
};

export default AuthorsDataGrid;
