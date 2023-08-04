import { FC } from 'react';
import { Typography } from '@mui/material';

type Props = {
	caption: string;
	text?: string | number;
};

/**
 * Text with caption
 * @param caption Caption
 * @param text Text to display
 * @constructor
 */
const TextWithCaption: FC<Props> = ({ caption, text }) => (
	<>
		<Typography textAlign="left" sx={{ px: 2 }} fontSize={10} color="grey">
			{caption}:
		</Typography>
		<Typography textAlign="left" sx={{ px: 2 }} fontSize={10}>
			{text}
		</Typography>
	</>
);

export default TextWithCaption;
