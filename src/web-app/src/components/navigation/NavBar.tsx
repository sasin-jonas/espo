import {
	AppBar,
	Box,
	Button,
	Container,
	Menu,
	MenuItem,
	Toolbar,
	Typography
} from '@mui/material';
import { Link, useLocation } from 'react-router-dom';
import React, { FC, useCallback, useContext, useState } from 'react';
import LogoutIcon from '@mui/icons-material/Logout';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { ExpandMore } from '@mui/icons-material';

// @ts-ignore
import muniLogo from '../../resources/M_logo.png';
import { useUserInfo } from '../../hooks/useLoggedInUser';
import SearchIcon from '@mui/icons-material/Search';

/**
 * Navigation bar
 * @constructor
 */
const NavBar: FC = () => {
	// context
	const { logOut, idTokenData } = useContext<IAuthContext>(AuthContext);
	const [userInfo, setUserInfo] = useUserInfo();

	// state
	const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
	const open = Boolean(anchorEl);

	// location
	const location = useLocation();

	// handlers
	const handleClick = (event: React.MouseEvent<HTMLButtonElement>) => {
		if (anchorEl !== event.currentTarget) {
			setAnchorEl(event.currentTarget);
		}
	};
	const handleClose = () => {
		setAnchorEl(null);
	};

	const logoutHandler = () => () => {
		logOut();
		setUserInfo(undefined);
	};

	// helpers
	const isAdmin = useCallback(() => {
		if (userInfo?.roles) {
			return userInfo?.roles.map(r => r.name).includes('ROLE_ADMIN');
		}
		return false;
	}, [userInfo]);

	const pagesWithoutSearchNavBtn: string[] = ['/search', '/'];
	return (
		<AppBar sx={{ position: 'sticky', top: 0, backgroundColor: '#0000DC' }}>
			<Container maxWidth="xl" sx={{ height: 1 }}>
				<Toolbar
					disableGutters
					sx={{ gap: 1, backgroundColor: '#0000DC', height: 1 }}
				>
					<Button
						component={Link}
						to="/"
						sx={{
							height: 1,
							maxWidth: 230,
							backgroundColor: 'rgb(37,37,206)'
						}}
					>
						<>
							<img src={muniLogo} alt="muniLogo" height={40} />
							<Typography fontSize={12}>
								Effective search for project opportunities
							</Typography>
						</>
					</Button>
					<Box sx={{ flexGrow: 1 }} />
					{!pagesWithoutSearchNavBtn.includes(location.pathname) &&
						idTokenData && (
							<Button
								component={Link}
								to="/search"
								sx={{
									backgroundColor: '#0000DC',
									color: '#ffffff'
								}}
								variant="contained"
							>
								<Typography fontSize={12} fontWeight={20}>
									Let&apos;s search
								</Typography>
								<Box sx={{ flexGrow: 0.5 }} />
								<SearchIcon fontSize="medium" />
							</Button>
						)}
					<Box sx={{ flexGrow: 1 }} />
					{isAdmin() && idTokenData && (
						<>
							<Button
								aria-controls={open ? 'basic-menu' : undefined}
								aria-haspopup="true"
								aria-expanded={open ? 'true' : undefined}
								onClick={handleClick}
								onMouseOver={handleClick}
								variant="contained"
							>
								Manage <ExpandMore />
							</Button>
							<Menu
								id="basic-menu"
								anchorEl={anchorEl}
								open={open}
								onClose={handleClose}
								MenuListProps={{ onMouseLeave: handleClose }}
							>
								<MenuItem component={Link} to="/users">
									Users
								</MenuItem>
								<MenuItem component={Link} to="/projects">
									MU projects
								</MenuItem>
								<MenuItem component={Link} to="/opportunities">
									Crowdhelix opportunities
								</MenuItem>
							</Menu>
						</>
					)}
					{idTokenData && (
						<>
							<Box sx={{ flexGrow: 0.01 }} />
							<Typography
								sx={{
									color: 'white',
									textDecoration: 'none',
									boxShadow: 'none'
								}}
								component={Link}
								to="/me"
							>
								{idTokenData.name}
							</Typography>
							<Box sx={{ flexGrow: 0.01 }} />
							<Button onClick={logoutHandler()}>
								<LogoutIcon />
							</Button>
						</>
					)}
				</Toolbar>
			</Container>
		</AppBar>
	);
};

export default NavBar;
