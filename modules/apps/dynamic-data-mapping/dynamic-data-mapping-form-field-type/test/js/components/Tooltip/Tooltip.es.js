/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import Tooltip from 'source/components/Tooltip/Tooltip.es';
import {dom as MetalTestUtil} from 'metal-dom';

let component;
const spritemap = 'icons.svg';

describe('Field Tooltip', () => {
	beforeEach(() => {
		jest.useFakeTimers();
	});

	afterEach(() => {
		if (component) {
			component.dispose();
		}
	});

	it('should render the default markup', () => {
		component = new Tooltip({
			icon: 'question-circle-full',
			spritemap,
			text: 'This is a tooltip information about this component'
		});

		expect(component).toMatchSnapshot();
	});

	it('should update the tooltip visible state when the mouse is over the tooltip target', () => {
		component = new Tooltip({
			icon: 'question-circle-full',
			spritemap,
			text: 'This is a tooltip information about this component'
		});

		jest.runAllTimers();

		const {tooltipTarget} = component.refs;

		MetalTestUtil.triggerEvent(tooltipTarget, 'mouseover');

		expect(component.showContent).toBe(true);
		expect(component).toMatchSnapshot();
	});

	it('should update the tooltip visible state when the mouse leaved the tooltip target', () => {
		component = new Tooltip({
			icon: 'question-circle-full',
			spritemap,
			text: 'This is a tooltip information about this component'
		});

		component._handleTooltipHovered();
		component._handleTooltipLeaved();

		expect(component.showContent).toBe(false);
		expect(component).toMatchSnapshot();
	});

	it('should update the tooltip visible state when the mouse is over the tooltip target', () => {
		component = new Tooltip({
			icon: 'question-circle-full',
			spritemap,
			text: 'This is a tooltip information about this component'
		});

		jest.runAllTimers();

		component.refs.tooltipSource = {
			element: document.createElement('div')
		};

		component._handleTooltipHovered();
		component._handleTooltipRendered();

		expect(component.showContent).toBe(true);
		expect(component).toMatchSnapshot();
	});
});
