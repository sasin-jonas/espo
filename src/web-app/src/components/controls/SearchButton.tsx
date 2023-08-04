import { FC } from 'react';
import { Box, Button } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';

type Props = {
	onClick: () => void;
	isDisabled: boolean;
};

/**
 * Search button
 * @param onClick On click handler
 * @param isDisabled Is button disabled
 * @constructor
 */
const SearchButton: FC<Props> = ({ onClick, isDisabled }) => (
	<Button
		variant="contained"
		sx={{ backgroundColor: '#0000DC', color: 'white', width: 1 }}
		onClick={onClick}
		disabled={isDisabled}
	>
		Search <Box sx={{ flexGrow: 1 }} /> <SearchIcon fontSize="large" />
	</Button>
);

export default SearchButton;
