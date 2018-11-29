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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.liferay.petra.lang.HashUtil;
import com.liferay.portal.kernel.util.LocaleUtil;

/**
 * @author Leonardo Barros
 */
public final class DataDefinitionField implements Serializable {

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DataDefinitionField)) {
			return false;
		}

		DataDefinitionField dataDefinitionField = (DataDefinitionField)obj;

		if (Objects.equals(_defaultValue, dataDefinitionField._defaultValue) &&
			Objects.equals(_indexable, dataDefinitionField._indexable) &&
			Objects.equals(_label, dataDefinitionField._label) &&
			Objects.equals(_localizable, dataDefinitionField._localizable) &&
			Objects.equals(_name, dataDefinitionField._name) &&
			Objects.equals(_repeatable, dataDefinitionField._repeatable) &&
			Objects.equals(_required, dataDefinitionField._required) &&
			Objects.equals(_tip, dataDefinitionField._tip) &&
			Objects.equals(_type, dataDefinitionField._type)) {

			return true;
		}

		return false;
	}

	public Object getDefaultValue() {
		return _defaultValue;
	}

	public Map<String, String> getLabel() {
		return Collections.unmodifiableMap(_label);
	}

	public String getName() {
		return _name;
	}

	public Map<String, String> getTip() {
		return Collections.unmodifiableMap(_tip);
	}

	public DataDefinitionColumnType getType() {
		return _type;
	}

	@Override
	public int hashCode() {
		int hash = 0;

		if (_defaultValue != null) {
			hash = HashUtil.hash(hash, _defaultValue.hashCode());
		}

		hash = HashUtil.hash(hash, _indexable);

		hash = HashUtil.hash(hash, _label.hashCode());

		hash = HashUtil.hash(hash, _localizable);

		hash = HashUtil.hash(hash, _name.hashCode());

		hash = HashUtil.hash(hash, _repeatable);

		hash = HashUtil.hash(hash, _required);

		hash = HashUtil.hash(hash, _tip.hashCode());

		return HashUtil.hash(hash, _type.hashCode());
	}

	public boolean isIndexable() {
		return _indexable;
	}

	public boolean isLocalizable() {
		return _localizable;
	}

	public boolean isRepeatable() {
		return _repeatable;
	}

	public boolean isRequired() {
		return _required;
	}
	
	public static Builder buildField() {
		return new Builder();
	}

	public static final class Builder {

//		private static final String DEFAULT_NAME = "Unnamed";
//		private static final DataDefinitionColumnType DEFAULT_TYPE = DataDefinitionColumnType.STRING;
		
		public DataDefinitionField done() {
//			if (_dataDefinitionColumn._name == null) {
//				_dataDefinitionColumn._name = DEFAULT_NAME;
//			}
//			if (_dataDefinitionColumn._type == null) {
//				_dataDefinitionColumn._type = DEFAULT_TYPE;
//			}
			return _dataDefinitionField;
		}

		public Builder withDefaultValue(Object defaultValue) {
			_dataDefinitionField._defaultValue = defaultValue;

			return this;
		}

		public Builder indexable(boolean indexable) {
			_dataDefinitionField._indexable = indexable;

			return this;
		}
		
		public Builder notIndexable() {
			indexable(false);

			return this;
		}

		public Builder withLabel(String locale, String label) {
			_dataDefinitionField._label.put(locale, label);

			return this;
		}

		public Builder withLabel(Locale locale, String label) {
			_dataDefinitionField._label.put(LocaleUtil.toLanguageId(locale), label);

			return this;
		}

		public Builder withLabels(Map<String, String> label) {
			_dataDefinitionField._label.putAll(label);

			return this;
		}

		public Builder localizable(boolean localizable) {
			_dataDefinitionField._localizable = localizable;

			return this;
		}

		public Builder localizable() {
			localizable(true);

			return this;
		}
		
		public Builder called(String name) {
			_dataDefinitionField._name = name;

			return this;
		}

		public Builder repeatable(boolean repeatable) {
			_dataDefinitionField._repeatable = repeatable;

			return this;
		}		
		
		public Builder repeatable() {
			repeatable(true);

			return this;
		}
		
		public Builder required(boolean required) {
			_dataDefinitionField._required = required;

			return this;
		}
		
		public Builder required() {
			required(true);

			return this;
		}

		public Builder withTips(Map<String, String> tip) {
			_dataDefinitionField._tip.putAll(tip);

			return this;
		}

		public Builder withTip(String locale, String tip) {
			_dataDefinitionField._tip.put(locale, tip);

			return this;
		}

		public Builder withTip(Locale locale, String tip) {
			_dataDefinitionField._tip.put(LocaleUtil.toLanguageId(locale), tip);

			return this;
		}

		public Builder ofType(DataDefinitionColumnType type) {
			_dataDefinitionField._type = type;

			return this;
		}

		private final DataDefinitionField _dataDefinitionField =
			new DataDefinitionField();

	}

	private DataDefinitionField() {
	}

	private Object _defaultValue;
	private boolean _indexable = true;
	private final Map<String, String> _label = new HashMap<>();
	private boolean _localizable;
	private String _name;
	private boolean _repeatable;
	private boolean _required;
	private final Map<String, String> _tip = new HashMap<>();
	private DataDefinitionColumnType _type;

}