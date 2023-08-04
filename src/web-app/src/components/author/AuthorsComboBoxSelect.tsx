import { FC } from 'react';
import {
	FormControl,
	InputLabel,
	MenuItem,
	Select,
	SelectChangeEvent
} from '@mui/material';

import { AuthorDto } from '../../types/Project.Types';

type Props = {
	onChange: (event: SelectChangeEvent) => void;
	initialValue?: AuthorDto;
	label: string;
	options: AuthorDto[];
};

/**
 * ComboBox select for project authors
 * @param initialValue Initial value
 * @param onChange On change handler
 * @param options Available authors
 * @param label Label text
 * @constructor
 */
const AuthorsComboBoxSelect: FC<Props> = ({
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
				.sort((a, b) => a.name.localeCompare(b.name))
				.map(i => (
					<MenuItem key={i.id} value={i.id}>
						{i.name} - {i.uco}
					</MenuItem>
				))}
		</Select>
	</FormControl>
);

export default AuthorsComboBoxSelect;
