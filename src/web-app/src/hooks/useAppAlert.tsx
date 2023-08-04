import {
	createContext,
	Dispatch,
	FC,
	PropsWithChildren,
	SetStateAction,
	useContext,
	useState
} from 'react';

import { AppAlertOptions } from '../types/Alert.Types';

type AlertState = [AppAlertOptions, Dispatch<SetStateAction<AppAlertOptions>>];

const AlertContext = createContext<AlertState>(undefined as never);

/**
 * Provider for the alert context
 * @param children Child components
 * @constructor
 */
export const AlertProvider: FC<PropsWithChildren> = ({ children }) => {
	const appAlertOptions = useState<AppAlertOptions>({
		open: false
	});
	return (
		<AlertContext.Provider value={appAlertOptions}>
			{children}
		</AlertContext.Provider>
	);
};

export const useAlert = () => useContext(AlertContext);
