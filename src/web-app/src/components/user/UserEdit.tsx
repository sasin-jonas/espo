import { FC, PropsWithChildren, useContext, useState } from 'react';
import {
	Box,
	Button,
	Card,
	CardContent,
	Modal,
	Typography
} from '@mui/material';
import { useQueryClient } from 'react-query';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';

import { RoleDto, UserDto } from '../../types/User.Types';
import { useUpdateUser } from '../../hooks/api/useUsersApi';
import AutocompleteTagSelect from '../controls/AutocompleteTagSelect';
import { useAlert } from '../../hooks/useAppAlert';

type Props = PropsWithChildren<{
	user?: UserDto;
}>;

/**
 * User edit modal
 * @param user User to edit
 * @param children Clickable element to open the modal window (user edit icon)
 * @constructor
 */
const UserEdit: FC<Props> = ({ user, children }) => {
	// context
	const qc = useQueryClient();
	const { token } = useContext<IAuthContext>(AuthContext);
	const [, setAlertOptions] = useAlert();

	// state
	const [open, setOpen] = useState(false);
	const [selectedRoles, setSelectedRoles] = useState<string[]>([]);

	// api calls
	const updateUserCall = user
		? useUpdateUser(qc, token, user.id, setAlertOptions)
		: undefined;

	// handlers
	const handleOpen = () => setOpen(true);
	const handleClose = () => setOpen(false);
	const onSave = async () => {
		const roles: RoleDto[] = [];
		selectedRoles.forEach(r => roles.push({ name: r }));
		const updatedUser: UserDto = {
			id: user?.id ?? 0,
			jwtIdentifier: user?.jwtIdentifier ?? '',
			email: user?.email ?? '',
			name: user?.name ?? '',
			uco: user?.uco ?? '',
			roles
		};
		try {
			await updateUserCall?.mutateAsync(updatedUser);
		} catch {
			console.error('Failed to update user');
		} finally {
			setOpen(false);
		}
	};

	return (
		<>
			<Button onClick={handleOpen} variant="contained" size="small">
				{children}
			</Button>
			<Modal
				sx={{
					top: '15%',
					left: '5%',
					right: '5%',
					bottom: '10%',
					overflow: 'scroll'
				}}
				open={open}
				onClose={handleClose}
				aria-describedby="user-edit"
			>
				<Card sx={{ width: 1, borderRadius: 2 }}>
					<CardContent sx={{ width: 'inherit' }}>
						<Box
							sx={{
								backgroundColor: '#E8E8EEFF',
								borderRadius: 2,
								px: 2,
								py: 1,
								mb: 2
							}}
						>
							<Typography variant="h5" textAlign="left" color="textPrimary">
								{user?.name}
							</Typography>
						</Box>

						<AutocompleteTagSelect
							label="Roles"
							placeHolder="Select roles"
							options={['ROLE_ADMIN', 'ROLE_USER']}
							onChange={(_, value) => setSelectedRoles(value)}
							defaultOptions={user?.roles.map(r => r.name)}
						/>

						<Button onClick={onSave} variant="contained" sx={{ my: 2 }}>
							Update
						</Button>
					</CardContent>
				</Card>
			</Modal>
		</>
	);
};

export default UserEdit;
