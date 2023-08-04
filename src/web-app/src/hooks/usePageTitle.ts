import { useEffect } from 'react';

/**
 * Custom hook to set the page title
 * @param title
 */
const usePageTitle = (title: string) => {
	useEffect(() => {
		document.title = `${title} | ESPO`;
	}, [title]);
};

export default usePageTitle;
