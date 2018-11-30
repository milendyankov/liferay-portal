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

package com.liferay.data.engine.service;

import com.liferay.data.engine.model.DataDefinition;

/**
 * @author Leonardo Barros
 */
public final class DataDefinitionSaveRequest {

	public DataDefinition getDataDefinition() {
		return _dataDefinition;
	}

	public long getGroupId() {
		return _groupId;
	}

	public long getUserId() {
		return _userId;
	}

	public static final class Builder {

		Builder (DataDefinition dataDefinition) {
			_dataDefinitionSaveRequest._dataDefinition = dataDefinition;
		}

		public DataDefinitionSaveRequest done() {
			return _dataDefinitionSaveRequest;
		}


		public Builder onBehalfOf(long userId) {
			_dataDefinitionSaveRequest._userId = userId;

			return this;
		}

		public Builder inGroup(long groupId) {
			_dataDefinitionSaveRequest._groupId = groupId;

			return this;
		}

		private final DataDefinitionSaveRequest _dataDefinitionSaveRequest =
			new DataDefinitionSaveRequest();

	}

	private DataDefinitionSaveRequest() {
	}

	private DataDefinition _dataDefinition;
	private long _groupId;
	private long _userId;

}