import { Box, Link } from '@mui/material';
import { FC } from 'react';

type Props = {
	url: string;
};

/**
 * Link to attachment
 * @param url Attachment URL
 * @constructor
 */
const AttachmentLink: FC<Props> = ({ url }) => (
	<Box
		sx={{
			backgroundColor: '#E8E8EEFF',
			borderRadius: 2,
			mx: 1,
			my: 1
		}}
	>
		<Link
			rel="noopener noreferrer"
			href={url}
			target="_blank"
			sx={{ color: 'darkblue', py: 1, px: 1 }}
			fontSize={10}
		>
			Attachment
		</Link>
	</Box>
);

export default AttachmentLink;
