import { FC, useContext, useEffect, useState } from 'react';
import { AuthContext, IAuthContext } from 'react-oauth2-code-pkce';
import { useParams } from 'react-router-dom';
import { CircularProgress } from '@mui/material';

import usePageTitle from '../hooks/usePageTitle';
import SearchResults from '../components/display/SearchResults';
import { OpportunityDto } from '../types/Opportunity.Types';
import { useGetMoreLikeThis } from '../hooks/api/useSearchApi';
import { AppAlertTypes } from '../types/Alert.Types';
import { useAlert } from '../hooks/useAppAlert';

/**
 * MoreLikeThis page
 * @constructor
 */
const MoreLikeThisPage: FC = () => {
	// context
	usePageTitle('Search');
	const [, setAlertOptions] = useAlert();
	const { token } = useContext<IAuthContext>(AuthContext);
	const { id } = useParams();

	// state
	const [searchData, setSearchData] = useState<OpportunityDto[] | undefined>(
		undefined
	);
	// api calls
	const searchResult = useGetMoreLikeThis(token, id ?? '');

	// effects and handlers
	useEffect(() => {
		setSearchData(searchResult.data ? searchResult.data.data : undefined);
	}, [searchResult.data]);

	useEffect(() => {
		if (searchResult.isError) {
			setAlertOptions({
				open: true,
				error: searchResult.error,
				severity: AppAlertTypes.Error
			});
		}
	}, [searchResult.isError]);

	return (
		<>
			{searchData && <SearchResults searchResult={searchData} />}
			{!searchData && <CircularProgress />}
		</>
	);
};

export default MoreLikeThisPage;
