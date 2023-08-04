import { FC, PropsWithChildren, useContext, useState } from 'react';
import {
	Box,
	Button,
	Card,
	CardContent,
	Modal,
	SelectChangeEvent,
	TextField
} from '@mui/material';
import { useQueryClient } from 'react-query';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';

import {
	AuthorDto,
	DepartmentDto,
	ProjectDto
} from '../../types/Project.Types';
import { useUpdateProject } from '../../hooks/api/useProjectsApi';
import useField from '../../hooks/useField';
import AuthorsComboBoxSelect from '../author/AuthorsComboBoxSelect';
import DepartmentsComboBoxSelect from '../controls/DepartmentsComboBoxSelect';
import { useAlert } from '../../hooks/useAppAlert';

type Props = PropsWithChildren<{
	project?: ProjectDto;
	onUpdate: () => Promise<void>;
	authors?: AuthorDto[];
	departments?: DepartmentDto[];
}>;

/**
 * Project edit modal
 * @param project Project to edit
 * @param onUpdate Callback to update the project list
 * @param authors Authors to select from
 * @param departments Departments to select from
 * @param children Clickable element to open the modal window (project edit icon)
 * @constructor
 */
const ProjectEdit: FC<Props> = ({
	project,
	onUpdate,
	authors,
	departments,
	children
}) => {
	// context
	const qc = useQueryClient();
	const { token } = useContext<IAuthContext>(AuthContext);
	const [, setAlertOptions] = useAlert();

	// state
	const [open, setOpen] = useState(false);
	const [selectedAuthor, setSelectedAuthor] = useState<AuthorDto | undefined>(
		project?.author
	);
	const [selectedDepartment, setSelectedDepartment] = useState<
		DepartmentDto | undefined
	>(project?.department);

	// Fields
	const [title, titleProps] = useField('title', true);
	const [annotation, annotationProps] = useField('annotation', true);
	const [projId, projIdProps] = useField('projId', true);
	const [regCode, regCodeProps] = useField('regCode', true);
	const [muniRole, muniRoleProps] = useField('muniRole', true);

	const updateUserCall = project
		? useUpdateProject(qc, token, project.id, setAlertOptions)
		: undefined;

	// handlers
	const handleChangeSelectedAuthor = (event: SelectChangeEvent) => {
		const id = Number(event.target.value);
		if (!isNaN(id)) {
			setSelectedAuthor(authors?.find(a => a.id === id) ?? selectedAuthor);
		}
	};
	const handleChangeSelectedDepartment = (event: SelectChangeEvent) => {
		const id = Number(event.target.value);
		if (!isNaN(id)) {
			setSelectedDepartment(
				departments?.find(a => a.id === id) ?? selectedDepartment
			);
		}
	};

	const handleOpen = () => {
		titleProps.onChange({ target: { value: project?.title } } as never);
		annotationProps.onChange({
			target: { value: project?.annotation }
		} as never);
		projIdProps.onChange({ target: { value: project?.projId } } as never);
		regCodeProps.onChange({ target: { value: project?.regCode } } as never);
		muniRoleProps.onChange({ target: { value: project?.muniRole } } as never);
		setOpen(true);
	};
	const handleClose = () => {
		setOpen(false);
	};
	const onSave = async () => {
		const updatedProject: ProjectDto = {
			annotation,
			author: selectedAuthor,
			dateBegin: project?.dateBegin ?? '',
			dateEnd: project?.dateEnd ?? '',
			department: selectedDepartment,
			investor: project?.investor ?? '',
			muniRole,
			projId,
			regCode,
			state: project?.state ?? '',
			title,
			id: project?.id ?? 0
		};
		try {
			await updateUserCall?.mutateAsync(updatedProject);
			await onUpdate();
		} catch {
			console.error('Failed to update project');
		} finally {
			setOpen(false);
		}
	};

	return (
		<>
			<Button onClick={handleOpen} variant="contained" sx={{ mx: 1 }}>
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
						<Box sx={{ my: 2 }} />
						<TextField label="Project ID" fullWidth {...projIdProps} />
						<Box sx={{ my: 2 }} />
						<TextField label="Registration code" fullWidth {...regCodeProps} />
						<Box sx={{ my: 2 }} />
						<TextField label="Title" fullWidth {...titleProps} />
						<Box sx={{ my: 2 }} />
						<AuthorsComboBoxSelect
							label="Author"
							options={authors ?? []}
							onChange={handleChangeSelectedAuthor}
							initialValue={selectedAuthor}
						/>
						<Box sx={{ my: 2 }} />
						<DepartmentsComboBoxSelect
							label="Department"
							options={departments ?? []}
							onChange={handleChangeSelectedDepartment}
							initialValue={selectedDepartment}
						/>
						<Box sx={{ my: 2 }} />
						<TextField label="Muni role" fullWidth {...muniRoleProps} />
						<Box sx={{ my: 2 }} />
						<TextField
							label="Annotation"
							multiline
							fullWidth
							rows={8}
							{...annotationProps}
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

export default ProjectEdit;
