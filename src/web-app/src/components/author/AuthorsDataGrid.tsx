import { FC } from 'react';
import { Box } from '@mui/material';
import { DataGrid, GridColDef, GridSelectionModel } from '@mui/x-data-grid';

import { AuthorDto } from '../../types/Project.Types';

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
			<DataGrid
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
