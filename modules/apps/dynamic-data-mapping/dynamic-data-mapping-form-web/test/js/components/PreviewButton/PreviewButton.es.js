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

import Notifications from 'source/util/Notifications.es';
import PreviewButton from 'source/components/PreviewButton/PreviewButton.es';

const previewURL = '/my/form/preview';
const spritemap = 'spritemap';

const props = {
	resolvePreviewURL: () => Promise.resolve(previewURL),
	spritemap
};

describe('PreviewButton', () => {
	let component;

	afterEach(() => {
		if (component) {
			component.dispose();
		}
	});

	beforeEach(() => {
		jest.useFakeTimers();
		fetch.resetMocks();
	});

	it('should render', () => {
		component = new PreviewButton(props);

		expect(component).toMatchSnapshot();
	});

	describe('preview()', () => {
		it('should call fetch with published=true', () => {
			component = new PreviewButton(props);

			const windowOpenSpy = jest.spyOn(window, 'open');

			windowOpenSpy.mockImplementation(() => null);

			return component
				.preview()
				.then(() =>
					expect(windowOpenSpy).toHaveBeenCalledWith(
						previewURL,
						'_blank'
					)
				);
		});

		it('should be called when button is clicked', () => {
			component = new PreviewButton(props);

			const previewSpy = jest.spyOn(component, 'preview');

			component.refs.button.emit('click');

			jest.runAllTimers();

			expect(previewSpy).toHaveBeenCalled();
		});

		it('should show error notification when resolvePreviewURL fails', () => {
			component = new PreviewButton({
				...props,
				resolvePreviewURL: () => Promise.reject()
			});

			const notificationSpy = jest.spyOn(Notifications, 'showError');

			component.preview().catch(() => {
				expect(notificationSpy).toHaveBeenCalled();
			});
		});
	});
});
