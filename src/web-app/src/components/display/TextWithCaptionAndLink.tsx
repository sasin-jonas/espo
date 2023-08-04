import { FC } from 'react';
import { Link, Typography } from '@mui/material';

type Props = {
	caption: string;
	text?: string | number;
	url?: string;
};

/**
 * Text with caption and link
 * @param caption Caption of the text
 * @param text Text to display
 * @param url URL to link to
 * @constructor
 */
const TextWithCaptionAndLink: FC<Props> = ({ caption, text, url }) => (
	<>
		<Typography textAlign="left" sx={{ px: 2 }} fontSize={10} color="grey">
			{caption}:
		</Typography>
		<Typography textAlign="left" sx={{ px: 2 }} fontSize={10}>
			<Link
				rel="noopener noreferrer"
				href={url}
				target="_blank"
				sx={{ color: 'darkblue' }}
			>
				{text}
			</Link>
		</Typography>
	</>
);

export default TextWithCaptionAndLink;
