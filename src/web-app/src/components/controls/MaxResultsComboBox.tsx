import { FC } from 'react';
import {
	FormControl,
	InputLabel,
	MenuItem,
	Select,
	SelectChangeEvent
} from '@mui/material';

type Props = {
	onChange: (event: SelectChangeEvent) => void;
	currentSize: string;
};

/**
 * ComboBox select for maximal result size
 * @param currentSize Current maximal result size
 * @param onChange On change handler
 * @constructor
 */
const MaxResultsComboBox: FC<Props> = ({ currentSize, onChange }) => (
	<FormControl fullWidth>
		<InputLabel id="paging-label">Maximal result size</InputLabel>
		<Select
			labelId="max-results-info-label"
			id="max-results-select"
			value={currentSize ?? ''}
			label="Max results"
			onChange={onChange}
		>
			{Array.from(Array(10).keys()).map(i => (
				<MenuItem key={i} value={(i + 1) * 10}>
					{(i + 1) * 10}
				</MenuItem>
			))}
		</Select>
	</FormControl>
);

export default MaxResultsComboBox;
