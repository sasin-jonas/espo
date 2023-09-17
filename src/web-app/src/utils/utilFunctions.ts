import {
	getGridStringOperators,
	GridFilterItem,
	GridFilterModel,
	GridLinkOperator,
	GridSortItem,
	GridSortModel
} from '@mui/x-data-grid';
import { MutableRefObject } from 'react';
import { AxiosResponse } from 'axios';

export const filterChangeTimeout = 1000;

/**
 * Handle download of a file
 * @param data The data to download
 * @param contentType The content type of the data
 * @param suffix The suffix of the file
 */
export const handleDownload = (
	data: AxiosResponse<Blob>,
	contentType: string,
	suffix: string
) => {
	const csvData = new Blob([data.data], { type: contentType });
	const csvUrl = window.URL.createObjectURL(csvData);
	const downloadLink = document.createElement('a');
	downloadLink.href = csvUrl;
	downloadLink.download = `example.${suffix}`;
	document.body.appendChild(downloadLink);
	downloadLink.click();
	document.body.removeChild(downloadLink);
};

/**
 * Check if the sort is descending
 * @param newSortOptions The new sort options
 * @param oldSortOptions The old sort options
 * @returns 'true' if descending, 'false' if ascending
 */
export const isDescString = (
	newSortOptions: GridSortItem | undefined,
	oldSortOptions: GridSortItem | undefined
) => {
	if (newSortOptions) {
		return newSortOptions.sort === 'desc' ? 'true' : 'false';
	} else {
		return oldSortOptions?.sort === 'desc' ? 'true' : 'false';
	}
};

/**
 * Get the page number
 * @param qo Filter item
 * @param page New page
 * @param oldPage Old page
 * @returns The page number
 */
export const getPageNumber = (
	qo: GridFilterItem | undefined,
	page: number | undefined,
	oldPage: number | undefined
) => {
	if (qo) {
		return 0;
	} else {
		return page ?? oldPage;
	}
};

/**
 * Check remaining time
 * @param remainingTimeRef The remaining time reference
 * @param timeoutRef The timeout reference
 */
export const checkRemainingTime = (
	remainingTimeRef: MutableRefObject<number | null>,
	timeoutRef: MutableRefObject<NodeJS.Timeout | null>
) => {
	if (remainingTimeRef.current !== null) {
		const remainingTime = remainingTimeRef.current - Date.now();
		if (remainingTime <= 0) {
			timeoutRef.current = null;
			remainingTimeRef.current = null;
		} else {
			remainingTimeRef.current = Date.now() + remainingTime;
			setTimeout(checkRemainingTime, remainingTime);
		}
	}
};

export const handleResetAllTableFiltersAndSort = async (
	event: { keyCode: number },
	onFilterChange: (gridFilterModel: GridFilterModel) => void,
	field: string,
	onSortChange: (sortModel: GridSortModel) => Promise<void>
) => {
	if (event.keyCode === 112) {
		await onFilterChange({
			items: [
				{
					columnField: field,
					operatorValue: 'contains'
				}
			],
			linkOperator: GridLinkOperator.And,
			quickFilterLogicOperator: GridLinkOperator.And,
			quickFilterValues: []
		});
		await onSortChange([]);
	}
};

export const handleResetAllTableFilters = async (
	event: { keyCode: number },
	onFilterChange: (gridFilterModel: GridFilterModel) => void,
	field: string
) => {
	if (event.keyCode === 112) {
		// F1 key
		await onFilterChange({
			items: [
				{
					columnField: field,
					operatorValue: 'contains'
				}
			],
			linkOperator: GridLinkOperator.And,
			quickFilterLogicOperator: GridLinkOperator.And,
			quickFilterValues: []
		});
	}
};

// only filter by 'contains'
export const containsOnlyFilterOperators = getGridStringOperators().filter(
	({ value }) => ['contains'].includes(value)
);

export const getThesisUrl = () => {
	const { REACT_APP_THESIS_URL, REACT_APP_THESIS_URL_DEFAULT } = process.env;
	return REACT_APP_THESIS_URL === undefined || REACT_APP_THESIS_URL.length === 0
		? REACT_APP_THESIS_URL_DEFAULT
		: REACT_APP_THESIS_URL;
};
