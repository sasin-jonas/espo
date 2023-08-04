import { createTheme } from '@mui/material';

const theme = createTheme({
	palette: {
		primary: { main: 'rgb(187,188,196)', contrastText: 'rgb(0,0,0)' },
		background: {
			default: '#ededf5'
		},
		secondary: { main: '#0000DC' },
		mode: 'light'
	},
	components: {
		MuiCssBaseline: {
			styleOverrides: {
				'body, #root': {
					display: 'flex',
					flexDirection: 'column',
					minHeight: '100vh'
				}
			}
		},
		MuiButton: {
			styleOverrides: {
				colorInherit: 'secondary'
			}
		}
	}
});

export default theme;
