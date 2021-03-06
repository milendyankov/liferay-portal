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

package com.liferay.talend.common.json;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Igor Beslic
 */
public class JsonFinderTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testGetDescendantJsonArray() {
		JsonFinder jsonFinder = new JsonFinder();

		JsonObject jsonObject = _getTestOASJsonObject();

		JsonArray descendantJsonArray = jsonFinder.getDescendantJsonArray(
			"invalid>path>to>array", jsonObject);

		Assert.assertNotNull(
			"Expect non null json array object", descendantJsonArray);
		Assert.assertEquals(
			"Expect empty json array", JsonValue.EMPTY_JSON_ARRAY,
			descendantJsonArray);

		descendantJsonArray = jsonFinder.getDescendantJsonArray(
			"components>schemas>Option>properties>fieldType>enum", jsonObject);

		Assert.assertEquals(
			"Expected json array size", 6, descendantJsonArray.size());
	}

	@Test
	public void testGetDescendantJsonObject() {
		JsonFinder jsonFinder = new JsonFinder();

		JsonObject jsonObject = _getTestOASJsonObject();

		JsonObject descendantJsonObject = jsonFinder.getDescendantJsonObject(
			"invalid>path>to>object", jsonObject);

		Assert.assertNotNull(
			"Expect non null json object", descendantJsonObject);

		Assert.assertEquals(
			"Expect empty json object", JsonValue.EMPTY_JSON_OBJECT,
			descendantJsonObject);
	}

	@Test
	public void testHasPath() {
		JsonFinder jsonFinder = new JsonFinder();

		JsonObject jsonObject = _getTestOASJsonObject();

		Assert.assertFalse(
			"Expect path \"invalid>path\" is invalid",
			jsonFinder.hasPath("invalid>path", jsonObject));
		Assert.assertFalse(
			"Expect empty path is invalid", jsonFinder.hasPath("", jsonObject));
		Assert.assertFalse(
			"Expect path \"invalid\" is invalid",
			jsonFinder.hasPath("invalid", jsonObject));
		Assert.assertTrue(
			"Expect path \"info\" is valid",
			jsonFinder.hasPath("info", jsonObject));
		Assert.assertTrue(
			"Expect path \"info>description\" is valid",
			jsonFinder.hasPath("info>description", jsonObject));
	}

	private JsonObject _getTestOASJsonObject() {
		Class<?> clazz = getClass();

		InputStream resourceAsStream = clazz.getResourceAsStream(
			"openapi.json");

		JsonReader jsonReader = Json.createReader(resourceAsStream);

		return jsonReader.readObject();
	}

}