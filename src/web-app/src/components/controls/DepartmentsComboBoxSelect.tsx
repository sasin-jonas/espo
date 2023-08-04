import { FC } from 'react';
import {
	FormControl,
	InputLabel,
	MenuItem,
	Select,
	SelectChangeEvent
} from '@mui/material';

import { DepartmentDto } from '../../types/Project.Types';

type Props = {
	onChange: (event: SelectChangeEvent) => void;
	initialValue?: DepartmentDto;
	label: string;
	options: DepartmentDto[];
};

/**
 * ComboBox select for project departments
 * @param initialValue Initial value
 * @param onChange On change handler
 * @param options Available departments
 * @param label Label text
 * @constructor
 */
const DepartmentsComboBoxSelect: FC<Props> = ({
	initialValue,
	onChange,
	options,
	label
}) => (
	<FormControl fullWidth>
		<InputLabel id="author-label">{label}</InputLabel>
		<Select
			labelId={`${label}-label-id`}
			id={`${label}-id`}
			value={initialValue?.id.toString()}
			label={label}
			onChange={onChange}
		>
			{[...options]
				.sort(
					(a, b) =>
						a.orgUnit.localeCompare(b.orgUnit) ||
						a.departmentName.localeCompare(b.departmentName)
				)
				.map(i => (
					<MenuItem key={i.id} value={i.id}>
						{i.orgUnit} - {i.departmentName}
					</MenuItem>
				))}
		</Select>
	</FormControl>
);

export default DepartmentsComboBoxSelect;
