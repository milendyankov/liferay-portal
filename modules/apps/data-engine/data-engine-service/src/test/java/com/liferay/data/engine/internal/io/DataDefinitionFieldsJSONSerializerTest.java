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

package com.liferay.data.engine.internal.io;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.liferay.data.engine.exception.DataDefinitionFieldsSerializerException;
import com.liferay.data.engine.io.DataDefinitionFieldsSerializerApplyRequest;
import com.liferay.data.engine.io.DataDefinitionFieldsSerializerApplyResponse;
import com.liferay.data.engine.model.DataDefinitionColumnType;
import com.liferay.data.engine.model.DataDefinitionField;
import com.liferay.portal.json.JSONFactoryImpl;

/**
 * @author Leonardo Barros
 */
public class DataDefinitionFieldsJSONSerializerTest extends BaseTestCase {

	@Test
	public void testApply() throws Exception {
		Map<String, String> nameLabels = new HashMap<String, String>() {
			{
				put("pt_BR", "Nome");
				put("en_US", "Name");
			}
		};
		DataDefinitionField dataDefinitionColumn1 =
				DataDefinitionField.buildField()
					.called("name")
					.ofType(DataDefinitionColumnType.STRING)
					.withLabels(nameLabels)
					.required()
					.done();

		Map<String, String> emailLabels = new HashMap<String, String>() {
			{
				put("pt_BR", "Endereço de Email");
				put("en_US", "Email Address");
			}
		};

		Map<String, String> emailTips = new HashMap<String, String>() {
			{
				put("en_US", "Enter an email address");
				put("pt_BR", "Informe um endereço de email");
			}
		};

		DataDefinitionField dataDefinitionColumn2 =
				DataDefinitionField.buildField()
					.called("email")
					.ofType(DataDefinitionColumnType.STRING)
					.withDefaultValue("test@liferay.com")
					.withLabels(emailLabels)
					.withTips(emailTips)
					.notIndexable()
					.done();

		DataDefinitionFieldsSerializerApplyRequest.Builder builder =
			DataDefinitionFieldsSerializerApplyRequest.Builder.newBuilder(
				Arrays.asList(dataDefinitionColumn1, dataDefinitionColumn2)
			);

		DataDefinitionFieldsJSONSerializer
			dataDefinitionFieldsJSONSerializer =
				new DataDefinitionFieldsJSONSerializer();

		dataDefinitionFieldsJSONSerializer.jsonFactory = new JSONFactoryImpl();

		DataDefinitionFieldsSerializerApplyResponse
			dataDefinitionFieldsSerializerApplyResponse =
				dataDefinitionFieldsJSONSerializer.apply(builder.build());

		String json = read("data-definition-columns-serializer.json");

		JSONAssert.assertEquals(
			json, dataDefinitionFieldsSerializerApplyResponse.getContent(),
			false);
	}

	@Test(expected = DataDefinitionFieldsSerializerException.class)
	public void testRequiredName()
		throws DataDefinitionFieldsSerializerException {

		DataDefinitionField dataDefinitionColumn1 =
				DataDefinitionField.buildField()
					.called(null)
					.ofType(DataDefinitionColumnType.BOOLEAN)
					.done();
		
		DataDefinitionFieldsSerializerApplyRequest.Builder builder =
				DataDefinitionFieldsSerializerApplyRequest.Builder.newBuilder(
				Arrays.asList(dataDefinitionColumn1)
			);

		DataDefinitionFieldsJSONSerializer
			dataDefinitionFieldsJSONSerializer =
				new DataDefinitionFieldsJSONSerializer();

		dataDefinitionFieldsJSONSerializer.jsonFactory = new JSONFactoryImpl();

		dataDefinitionFieldsJSONSerializer.apply(builder.build());
	}

	@Test(expected = DataDefinitionFieldsSerializerException.class)
	public void testRequiredType()
		throws DataDefinitionFieldsSerializerException {

		DataDefinitionField dataDefinitionColumn1 =
				DataDefinitionField.buildField()
					.called("name")
					.ofType(null)
					.done();

		DataDefinitionFieldsSerializerApplyRequest.Builder builder =
			DataDefinitionFieldsSerializerApplyRequest.Builder.newBuilder(
				Arrays.asList(dataDefinitionColumn1)
			);

		DataDefinitionFieldsJSONSerializer
			dataDefinitionFieldsJSONSerializer =
				new DataDefinitionFieldsJSONSerializer();

		dataDefinitionFieldsJSONSerializer.jsonFactory = new JSONFactoryImpl();

		dataDefinitionFieldsJSONSerializer.apply(builder.build());
	}

}