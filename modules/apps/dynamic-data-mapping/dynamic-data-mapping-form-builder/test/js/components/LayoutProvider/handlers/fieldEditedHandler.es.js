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

import * as fieldEditedHandler from 'source/components/LayoutProvider/handlers/fieldEditedHandler.es';
import * as focusedFieldUtil from 'source/components/LayoutProvider/util/focusedField.es';
import mockPages from 'mock/mockPages.es';

describe('LayoutProvider/handlers/fieldEditedHandler', () => {
	describe('handleFieldEdited(state, event)', () => {
		it('should call updateFocusedField()', () => {
			const event = {
				propertyName: 'dataType',
				propertyValue: 'string'
			};
			const state = {
				focusedField: {},
				pages: mockPages,
				rules: []
			};

			const updateFocusedFieldSpy = jest.spyOn(
				focusedFieldUtil,
				'updateFocusedField'
			);

			updateFocusedFieldSpy.mockImplementation(() => ({}));

			fieldEditedHandler.handleFieldEdited(state, event);

			expect(updateFocusedFieldSpy).toHaveBeenCalled();

			updateFocusedFieldSpy.mockRestore();
		});

		it('should not call updateFocusedField() when changing name to an empty string', () => {
			const event = {
				propertyName: 'name',
				propertyValue: ''
			};
			const state = {
				focusedField: {},
				pages: mockPages,
				rules: []
			};

			const updateFocusedFieldSpy = jest.spyOn(
				focusedFieldUtil,
				'updateFocusedField'
			);

			updateFocusedFieldSpy.mockImplementation(() => ({}));

			fieldEditedHandler.handleFieldEdited(state, event);

			expect(updateFocusedFieldSpy).not.toHaveBeenCalled();

			updateFocusedFieldSpy.mockRestore();
		});
	});
});
