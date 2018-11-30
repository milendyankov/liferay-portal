package com.liferay.data.engine.service;

import com.liferay.data.engine.model.DataDefinition;

public class DataDefinitionRequest {

	public static DataDefinitionSaveRequest.Builder toSave (DataDefinition dataDefinition) {
		return new DataDefinitionSaveRequest.Builder(dataDefinition);
	}


	public static DataDefinitionGetRequest.Builder toGet () {
		return new DataDefinitionGetRequest.Builder();
	}

	public static DataDefinitionDeleteRequest.Builder toDelete () {
		return new DataDefinitionDeleteRequest.Builder();
	}
}
