package com.liferay.data.engine.internal.model;

import java.io.Serializable;

import com.liferay.data.engine.model.DataDefinition;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.portal.kernel.model.ClassedModel;

public class InternalDataDefinition implements ClassedModel {

	
	public InternalDataDefinition (DataDefinition dataDefinition) {
		_dataDefinition = dataDefinition;
	}
	
	
	@Override
	public ExpandoBridge getExpandoBridge() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Class<?> getModelClass() {
		return DataDefinition.class;
	}

	@Override
	public String getModelClassName() {
		return DataDefinition.class.getName();
	}

	@Override
	public Serializable getPrimaryKeyObj() {
		return _dataDefinition.getDataDefinitionId();
	}
	
	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		_dataDefinition = DataDefinition
				.buildDefinitionFrom(_dataDefinition)
				.withId(((Long)primaryKeyObj).longValue())
				.done();
	}

	public long getDataDefinitionId() {
		return _dataDefinition.getDataDefinitionId();
	}

	private DataDefinition _dataDefinition;

}
