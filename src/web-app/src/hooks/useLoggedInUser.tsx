import {
	createContext,
	Dispatch,
	FC,
	PropsWithChildren,
	SetStateAction,
	useContext,
	useEffect,
	useState
} from 'react';

import { UserDto } from '../types/User.Types';

type UserState = [
	UserDto | undefined,
	Dispatch<SetStateAction<UserDto | undefined>>
];

const UserContext = createContext<UserState>(undefined as never);

const getInitialState = (): UserDto | undefined => {
	const user = localStorage.getItem('auth');
	if (user) {
		try {
			return JSON.parse(user);
		} catch (e) {
			return undefined;
		}
	}
	return undefined;
};

/**
 * Provider for the user context
 * @param children Child components
 * @constructor
 */
export const UserInfoProvider: FC<PropsWithChildren> = ({ children }) => {
	const userState = useState<UserDto | undefined>(getInitialState());
	const [user, setUser] = userState;

	useEffect(() => {
		if (user !== undefined) {
			localStorage.setItem('auth', JSON.stringify(user));
		} else {
			localStorage.removeItem('auth');
		}
	}, [user]);

	const storageEventHandler = (e: StorageEvent) => {
		if (e.key === 'auth') {
			if (e.newValue === null) {
				setUser(undefined);
			} else {
				setUser(JSON.parse(e.newValue));
			}
		}
	};

	useEffect(() => {
		window.addEventListener('storage', e => storageEventHandler(e));
		return () => {
			window.removeEventListener('storage', e => storageEventHandler(e));
		};
	}, []);

	return (
		<UserContext.Provider value={userState}>{children}</UserContext.Provider>
	);
};

/**
 * Hook providing user information
 */
export const useUserInfo = () => useContext(UserContext);

/**
 * Hook providing logged in user information
 */
const useLoggedInUser = (): UserDto | undefined => {
	const [user] = useContext(UserContext);

	if (user === undefined) {
		return undefined;
	}

	return user;
};

export default useLoggedInUser;
