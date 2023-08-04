import { ChangeEvent, FC } from 'react';
import { Checkbox, FormControlLabel, FormGroup } from '@mui/material';

type Props = {
	onChange: (event: ChangeEvent, checked: boolean) => void;
	defValue: boolean;
	label: string;
};

/**
 * Checkbox with label text
 * @param defValue Default value
 * @param onChange On change handler
 * @param label Label text
 * @constructor
 */
const CheckBoxWithLabel: FC<Props> = ({ defValue, onChange, label }) => (
	<FormGroup>
		<FormControlLabel
			control={<Checkbox onChange={onChange} checked={defValue} />}
			label={label}
		/>
	</FormGroup>
);

export default CheckBoxWithLabel;
