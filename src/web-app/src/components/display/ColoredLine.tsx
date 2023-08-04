import { FC } from 'react';

type ColoredLineProps = {
	color: string;
	height: number;
};

/**
 * Colored line
 * @param color Color of the line
 * @param height Height of the line
 * @constructor
 */
const ColoredLine: FC<ColoredLineProps> = ({ color, height }) => (
	<hr
		style={{
			color,
			backgroundColor: color,
			height
		}}
	/>
);

export default ColoredLine;
