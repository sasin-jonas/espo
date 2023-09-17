import { FC, Key, SyntheticEvent } from 'react';
import { Autocomplete, TextField } from '@mui/material';

type Props = {
	label: string;
	placeHolder: string;
	options: string[];
	defaultOptions?: string[];
	onChange: (_: SyntheticEvent, value: string[]) => void;
	key?: Key;
};

/**
 * Autocomplete tag combo-box-like select
 * @param label Label text
 * @param placeHolder Placeholder text
 * @param options Available options
 * @param onChange On change handler
 * @param defaultOptions Default options
 * @constructor
 */
const AutocompleteTagSelect: FC<Props> = ({
	label,
	placeHolder,
	options,
	onChange,
	defaultOptions,
	key
}) => (
	<Autocomplete
		multiple
		id="tags-outlined"
		options={options}
		defaultValue={defaultOptions}
		filterSelectedOptions
		onChange={onChange}
		renderInput={params => (
			<TextField {...params} label={label} placeholder={placeHolder} />
		)}
		key={key}
	/>
);

export default AutocompleteTagSelect;
