import * as React from 'react';
import { FC, PropsWithChildren } from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogTitle from '@mui/material/DialogTitle';
import { Delete } from '@mui/icons-material';

type ConfirmDeleteDialogProps = PropsWithChildren<{
	onClick: () => void;
}>;

/**
 * Delete button with confirm dialog
 * @param onClick On click handler
 * @param children Dialog content
 * @constructor
 */
const DeleteButtonWithConfirmDialog: FC<ConfirmDeleteDialogProps> = ({
	onClick,
	children
}) => {
	const [open, setOpen] = React.useState(false);

	const handleClickOpen = () => {
		setOpen(true);
	};

	const handleCancel = () => {
		setOpen(false);
	};

	const handleOk = () => {
		onClick();
		setOpen(false);
	};

	return (
		<>
			<Button onClick={handleClickOpen} variant="contained">
				<Delete />
			</Button>
			<Dialog
				open={open}
				onClose={handleCancel}
				aria-labelledby="alert-dialog-title"
				aria-describedby="alert-dialog-description"
			>
				<DialogTitle id="alert-dialog-title" fontSize={17}>
					{children}
				</DialogTitle>
				<DialogActions>
					<Button onClick={handleOk} variant="contained">
						Ok
					</Button>
					<Button onClick={handleCancel} variant="contained">
						Cancel
					</Button>
				</DialogActions>
			</Dialog>
		</>
	);
};

export default DeleteButtonWithConfirmDialog;
