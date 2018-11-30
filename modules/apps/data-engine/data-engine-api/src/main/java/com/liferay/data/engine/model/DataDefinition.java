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

package com.liferay.data.engine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.liferay.petra.lang.HashUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

/**
 * @author Leonardo Barros
 */
public final class DataDefinition implements Serializable {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DataDefinition)) {
			return false;
		}

		DataDefinition dataDefinition = (DataDefinition)obj;

		if (Objects.equals(_name, dataDefinition._name) &&
			Objects.equals(_description, dataDefinition._description) &&
			Objects.equals(
				_dataDefinitionId, dataDefinition._dataDefinitionId) &&
			Objects.equals(_storageType, dataDefinition._storageType) &&
			Objects.equals(_columns, dataDefinition._columns)) {

			return true;
		}

		return false;
	}

	public List<DataDefinitionField> getFields() {
		return Collections.unmodifiableList(_columns);
	}

	public DataDefinitionField getField(String name) {
		return _columns.stream()
				.filter(c -> name.equals(c.getName()))
				.findFirst()
				.orElse(null);
	}

	public long getDataDefinitionId() {
		return _dataDefinitionId;
	}

	public Map<String, String> getDescription() {
		return Collections.unmodifiableMap(_description);
	}

	public Map<String, String> getName() {
		return Collections.unmodifiableMap(_name);
	}

	public String getStorageType() {
		return _storageType;
	}

	@Override
	public int hashCode() {
		int hash = HashUtil.hash(0, _name.hashCode());

		hash = HashUtil.hash(hash, _description.hashCode());

		hash = HashUtil.hash(hash, _dataDefinitionId);

		hash = HashUtil.hash(hash, _storageType.hashCode());

		return HashUtil.hash(hash, _columns.hashCode());
	}

	public static Builder buildDefinition() {
		return new Builder();
	}

	public static Builder buildDefinitionFrom (DataDefinition definition) {
		return new Builder(definition);
	}

	public static final class Builder {
		
		public Builder() {
			_dataDefinition = new DataDefinition();
		}

		public Builder(DataDefinition definition) {
			// TODO deep clone instead of reference  
			_dataDefinition = definition;
		}

		public DataDefinition done() {
			return _dataDefinition;
		}

		public Builder withId(long dataDefinitionId) {
			_dataDefinition._dataDefinitionId = dataDefinitionId;

			return this;
		}

		public Builder withDescription(Locale locale, String description) {
			_dataDefinition._description.put(
				LocaleUtil.toLanguageId(locale), description);

			return this;
		}

		public Builder withDescriptions(Map<Locale, String> descriptions) {
			for (Map.Entry<Locale, String> entry : descriptions.entrySet()) {
				_dataDefinition._description.put(
					LocaleUtil.toLanguageId(entry.getKey()), entry.getValue());
			}

			return this;
		}

		public Builder withName(Locale locale, String name) {
			_dataDefinition._name.put(LocaleUtil.toLanguageId(locale), name);

			return this;
		}

		public Builder withNames(Map<Locale, String> names) {
			for (Map.Entry<Locale, String> entry : names.entrySet()) {
				_dataDefinition._name.put(
					LocaleUtil.toLanguageId(entry.getKey()), entry.getValue());
			}

			return this;
		}

		public Builder ofStorageType(String type) {
			_dataDefinition._storageType = type;

			return this;
		}
		
		public Builder withFields(List<DataDefinitionField> columns) {
			_dataDefinition._columns.addAll(columns);

			return this;
		}

		public Builder withFields(DataDefinitionField... columns) {
			_dataDefinition._columns.addAll(Arrays.asList(columns));

			return this;
		}

		public Builder withoutFields(DataDefinitionField... columns) {
			_dataDefinition._columns.removeAll(Arrays.asList(columns));

			return this;
		}
		
		public Builder withoutFields() {
			_dataDefinition._columns.clear();

			return this;
		}


		private final DataDefinition _dataDefinition;

	}

	private DataDefinition() {
	}

	private final List<DataDefinitionField> _columns = new ArrayList<>();
	private long _dataDefinitionId;
	private final Map<String, String> _description = new HashMap<>();
	private final Map<String, String> _name = new HashMap<>();
	private String _storageType = "json";

}