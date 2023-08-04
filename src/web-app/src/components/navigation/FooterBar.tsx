import {
	AppBar,
	Box,
	Button,
	Container,
	Link,
	Toolbar,
	Typography
} from '@mui/material';
import { FC } from 'react';
import { Link as ReactLink } from 'react-router-dom';
import { HelpOutline, OpenInNew } from '@mui/icons-material';

const { REACT_APP_THESIS_URL, REACT_APP_THESIS_URL_DEFAULT } = process.env;
const thesisUrl =
	REACT_APP_THESIS_URL === undefined || REACT_APP_THESIS_URL.length === 0
		? REACT_APP_THESIS_URL_DEFAULT
		: REACT_APP_THESIS_URL;

/**
 * Footer bar
 * @constructor
 */
const FooterBar: FC = () => {
	return (
		<AppBar
			sx={{
				position: 'sticky',
				bottom: 0,
				backgroundColor: '#0000DC',
				height: 0.5
			}}
		>
			<Container maxWidth="xl">
				<Toolbar disableGutters sx={{ gap: 1, backgroundColor: 'inherit' }}>
					<Typography color="white" fontSize={13}>
						2023 | Created by Jonáš Sasín & IBA FM MU
					</Typography>

					<Link
						rel="noopener noreferrer"
						href={thesisUrl}
						target="_blank"
						sx={{ color: 'darkblue' }}
					>
						<OpenInNew fontSize="small" />
					</Link>

					<Box sx={{ flexGrow: 1 }} />
					<Button component={ReactLink} to="/about">
						<HelpOutline />
					</Button>
				</Toolbar>
			</Container>
		</AppBar>
	);
};

export default FooterBar;
