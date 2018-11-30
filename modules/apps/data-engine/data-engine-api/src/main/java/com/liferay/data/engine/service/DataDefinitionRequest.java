package com.liferay.data.engine.service;

import com.liferay.data.engine.model.DataDefinition;

public class DataDefinitionRequest {

	public static DataDefinitionSaveRequest.Builder saveRequestFor (DataDefinition dataDefinition) {
		return new DataDefinitionSaveRequest.Builder(dataDefinition);
	}


	public static DataDefinitionGetRequest.Builder getRequest () {
		return new DataDefinitionGetRequest.Builder();
	}

	public static DataDefinitionDeleteRequest.Builder deleteRequest () {
		return new DataDefinitionDeleteRequest.Builder();
	}
}
