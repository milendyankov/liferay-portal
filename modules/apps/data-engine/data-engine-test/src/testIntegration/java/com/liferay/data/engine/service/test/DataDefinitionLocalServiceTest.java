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

package com.liferay.data.engine.service.test;

import static com.liferay.data.engine.model.DataDefinition.buildDefinition;
import static com.liferay.data.engine.model.DataDefinition.buildDefinitionFrom;
import static com.liferay.data.engine.model.DataDefinitionField.buildField;
import static com.liferay.data.engine.model.DataDefinitionField.buildFieldFrom;
import static com.liferay.data.engine.service.DataDefinitionRequest.toDelete;
import static com.liferay.data.engine.service.DataDefinitionRequest.toGet;
import static com.liferay.data.engine.service.DataDefinitionRequest.toSave;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.liferay.arquillian.extension.junit.bridge.junit.Arquillian;
import com.liferay.data.engine.exception.DataDefinitionException;
import com.liferay.data.engine.model.DataDefinition;
import com.liferay.data.engine.model.DataDefinitionColumnType;
import com.liferay.data.engine.model.DataDefinitionField;
import com.liferay.data.engine.service.DataDefinitionDeleteRequest;
import com.liferay.data.engine.service.DataDefinitionGetRequest;
import com.liferay.data.engine.service.DataDefinitionGetResponse;
import com.liferay.data.engine.service.DataDefinitionLocalService;
import com.liferay.data.engine.service.DataDefinitionSaveRequest;
import com.liferay.data.engine.service.DataDefinitionSaveResponse;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.ResourceConstants;
import com.liferay.portal.kernel.model.ResourcePermission;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.ResourcePermissionLocalService;
import com.liferay.portal.kernel.service.RoleLocalService;
import com.liferay.portal.kernel.test.rule.AggregateTestRule;
import com.liferay.portal.kernel.test.rule.DeleteAfterTestRun;
import com.liferay.portal.kernel.test.util.GroupTestUtil;
import com.liferay.portal.kernel.test.util.UserTestUtil;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.test.rule.Inject;
import com.liferay.portal.test.rule.LiferayIntegrationTestRule;

/**
 * @author Leonardo Barros
 */
@RunWith(Arquillian.class)
public class DataDefinitionLocalServiceTest {

	@ClassRule
	@Rule
	public static final AggregateTestRule aggregateTestRule =
		new LiferayIntegrationTestRule();

	@Before
	public void setUp() throws Exception {
		_group = GroupTestUtil.addGroup();

		_user = UserTestUtil.addGroupOwnerUser(_group);
	}

	@Test(expected = DataDefinitionException.class)
	public void testDelete() throws Exception {
		Map<String, String> expectedNameLabels = new HashMap<String, String>() {
			{
				put("pt_BR", "Nome");
				put("en_US", "Name");
			}
		};


		DataDefinitionField dataDefinitionField = buildField()
			.called("name")
			.ofType(DataDefinitionColumnType.STRING)
			.withLabels(expectedNameLabels)
			.done();

		DataDefinition dataDefinition = buildDefinition()
			.withName(LocaleUtil.US, "Definition 1")
			.ofStorageType("json")
			.withFields(Arrays.asList(dataDefinitionField))
			.done();

		DataDefinitionSaveRequest dataDefinitionSaveRequest = 
			toSave(dataDefinition)
				.onBehalfOf(_user.getUserId())
				.inGroup(_group.getGroupId())
				.done();


		DataDefinitionSaveResponse dataDefinitionSaveResponse =
			_dataDefinitionLocalService.save(dataDefinitionSaveRequest);

		long dataDefinitionId = 
				dataDefinitionSaveResponse.getDataDefinitionId();

		DataDefinitionDeleteRequest dataDefinitionDeleteRequest = 
			toDelete()
				.byId(dataDefinitionId)
				.done();

		_dataDefinitionLocalService.delete(dataDefinitionDeleteRequest);

		DataDefinitionGetRequest dataDefinitionGetRequest = toGet()
				.byId(dataDefinitionId)
				.done();

		_dataDefinitionLocalService.get(dataDefinitionGetRequest);
	}

	@Test
	public void testGet() throws Exception {
		
		Map<String, String> column1Labels = new HashMap<String, String>() {
			{
				put("en_US", "Column 1");
			}
		};


		DataDefinitionField dataDefinitionField1 = buildField()
			.called("column1")
			.ofType(DataDefinitionColumnType.STRING)
			.withLabels(column1Labels)
			.done();

		DataDefinitionField dataDefinitionField2 = buildField()
			.called("column2")
			.ofType(DataDefinitionColumnType.NUMBER)
			.withLabel("en_US", "Column 2")
			.done();

		DataDefinitionField dataDefinitionField3 = buildField()
			.called("column3")
			.ofType(DataDefinitionColumnType.DATE)
			.withLabel(Locale.US, "Column 3")
			.done();
		
		DataDefinition expectedDataDefinition = 
			buildDefinition()
				.withName(LocaleUtil.US, "Definition 2")
				.ofStorageType("json")
				.withFields(
						dataDefinitionField1, 
						dataDefinitionField2,
						dataDefinitionField3)
				.done();

		DataDefinitionSaveRequest dataDefinitionSaveRequest = 
			toSave(expectedDataDefinition)
				.onBehalfOf(_user.getUserId())
				.inGroup(_group.getGroupId())
				.done();

		DataDefinitionSaveResponse dataDefinitionSaveResponse =
			_dataDefinitionLocalService.save(dataDefinitionSaveRequest);

		long dataDefinitionId = dataDefinitionSaveResponse.getDataDefinitionId();

		expectedDataDefinition = buildDefinitionFrom(expectedDataDefinition)
			.withId(dataDefinitionId)
			.done();

		DataDefinitionGetRequest dataDefinitionGetRequest =
			toGet().byId(dataDefinitionId).done();

		DataDefinitionGetResponse dataDefinitionGetResponse =
			_dataDefinitionLocalService.get(dataDefinitionGetRequest);

		Assert.assertEquals(
			expectedDataDefinition,
			dataDefinitionGetResponse.getDataDefinition());

		DataDefinitionDeleteRequest dataDefinitionDeleteRequest =
			toDelete().byId(dataDefinitionId).done();

		_dataDefinitionLocalService.delete(dataDefinitionDeleteRequest);
	}

	@Test
	public void testInsert() throws Exception {
		Map<String, String> expectedNameLabels = new HashMap<String, String>() {
			{
				put("pt_BR", "Nome");
				put("en_US", "Name");
			}
		};


		DataDefinitionField expectedDataDefinitionField1 = buildField()
			.called("name")
			.ofType(DataDefinitionColumnType.STRING)
			.withLabels(expectedNameLabels)
			.done();

		DataDefinitionField expectedDataDefinitionField2 = buildField()
			.called("email")
			.ofType(DataDefinitionColumnType.STRING)
			.withLabel("pt_BR", "Endereço de Email")
			.withLabel("en_US", "Email Address")
			.done();

	
		DataDefinition expectedDataDefinition = buildDefinition()
			.withName(LocaleUtil.US, "Contact")
			.withName(LocaleUtil.BRAZIL, "Contato")
			.withDescription(LocaleUtil.US, "Contact description")
			.withDescription(LocaleUtil.BRAZIL, "Descrição do contato")
			.ofStorageType("json")
			.withFields(
					expectedDataDefinitionField1, 
					expectedDataDefinitionField2)
			.done();
		
		DataDefinitionSaveRequest dataDefinitionSaveRequest = 
			toSave(expectedDataDefinition)
				.onBehalfOf(_user.getUserId())
				.inGroup(_group.getGroupId())
				.done();


		DataDefinitionSaveResponse dataDefinitionSaveResponse =
			_dataDefinitionLocalService.save(dataDefinitionSaveRequest);

		long dataDefinitionId = dataDefinitionSaveResponse.getDataDefinitionId();

		expectedDataDefinition = DataDefinition
			.buildDefinitionFrom(expectedDataDefinition)
			.withId(dataDefinitionId)
			.done();

		DataDefinitionGetRequest dataDefinitionGetRequest =
			toGet().byId(dataDefinitionId).done();

		DataDefinitionGetResponse dataDefinitionGetResponse =
			_dataDefinitionLocalService.get(dataDefinitionGetRequest);

		DataDefinition dataDefinition =
			dataDefinitionGetResponse.getDataDefinition();

		Assert.assertEquals(expectedDataDefinition, dataDefinition);

		Role ownerRole = _roleLocalService.getRole(
			_group.getCompanyId(), RoleConstants.OWNER);

		ResourcePermission resourcePermission =
			_resourcePermissionLocalService.fetchResourcePermission(
				_group.getCompanyId(), DataDefinition.class.getName(),
				ResourceConstants.SCOPE_INDIVIDUAL,
				String.valueOf(dataDefinitionId), ownerRole.getRoleId());

		Assert.assertTrue(resourcePermission.hasActionId(ActionKeys.VIEW));

		DataDefinitionDeleteRequest dataDefinitionDeleteRequest =
			toDelete().byId(dataDefinitionId).done();

		_dataDefinitionLocalService.delete(dataDefinitionDeleteRequest);
	}

	@Test
	public void testUpdate() throws Exception {
		Map<String, String> expectedTitleLabels = new HashMap<String, String>() {
			{
				put("pt_BR", "Título");
				put("en_US", "Title");
			}
		};

		DataDefinitionField dataDefinitionField1 =
				buildField()
					.called("title")
					.ofType(DataDefinitionColumnType.STRING)
					.withLabels(expectedTitleLabels)
					.localizable()
					.done();
		
		DataDefinition expectedDataDefinition =
				buildDefinition()
					.withName(LocaleUtil.US, "Story")
					.withName(LocaleUtil.BRAZIL, "Estória")
					.ofStorageType("json")
					.withFields(dataDefinitionField1)
					.done();
		
		DataDefinitionSaveRequest dataDefinitionSaveRequest =
				toSave(expectedDataDefinition)
					.onBehalfOf(_user.getUserId())
					.inGroup(_group.getGroupId())
					.done();

		DataDefinitionSaveResponse dataDefinitionSaveResponse =
			_dataDefinitionLocalService.save(dataDefinitionSaveRequest);

		long dataDefinitionId = 
				dataDefinitionSaveResponse.getDataDefinitionId();

		expectedDataDefinition = buildDefinitionFrom(expectedDataDefinition)
				.withId(dataDefinitionId)
				.done();

		DataDefinitionField dataDefinitionField2 = buildField()
					.called("description")
					.ofType(DataDefinitionColumnType.STRING)
					.withLabel("pt_BR", "Descrição")
					.withLabel("en_US", "Description")
					.localizable()
					.done();

		expectedDataDefinition = buildDefinition()
					.withId(dataDefinitionId)
					.withName(LocaleUtil.US, "Story")
					.withName(LocaleUtil.BRAZIL, "Estória")
					.ofStorageType("json")
					.withFields(dataDefinitionField1, dataDefinitionField2)
					.done();

		dataDefinitionSaveRequest =
				toSave(expectedDataDefinition)
					.onBehalfOf(_user.getUserId())
					.inGroup(_group.getGroupId())
					.done();

		_dataDefinitionLocalService.save(dataDefinitionSaveRequest);

		DataDefinitionGetRequest dataDefinitionGetRequest =
			toGet().byId(dataDefinitionId).done();

		DataDefinitionGetResponse dataDefinitionGetResponse =
			_dataDefinitionLocalService.get(dataDefinitionGetRequest);

		DataDefinition dataDefinition =
			dataDefinitionGetResponse.getDataDefinition();

		Assert.assertEquals(expectedDataDefinition, dataDefinition);

		DataDefinitionDeleteRequest dataDefinitionDeleteRequest =
			toDelete().byId(dataDefinitionId).done();

		_dataDefinitionLocalService.delete(dataDefinitionDeleteRequest);

	}
	
	@Test
	public void testUpdateFields() throws Exception {
		
		DataDefinition expectedDataDefinition =
			buildDefinition()
				.withName(LocaleUtil.US, "Story")
				.withName(LocaleUtil.BRAZIL, "Estória")
				.ofStorageType("json")
				.withFields(
					DataDefinitionField.buildField()
						.called("title")
						.ofType(DataDefinitionColumnType.STRING)
						.localizable()
						.done(),
					DataDefinitionField.buildField()
						.called("deleteMe")
						.ofType(DataDefinitionColumnType.STRING)
						.localizable()
						.done())
				.done();
		
		long id = _dataDefinitionLocalService.save(
				toSave(expectedDataDefinition)
					.onBehalfOf(_user.getUserId())
					.inGroup(_group.getGroupId())
					.done()
			).getDataDefinitionId();

		DataDefinition dataDefinitionFromStorage =
				_dataDefinitionLocalService.get(
					toGet().byId(id).done()
				).getDataDefinition();

		DataDefinitionField titleFiled = expectedDataDefinition.getField("title");
		titleFiled = buildFieldFrom(titleFiled)
					.withTip(LocaleUtil.US, "Some tip")
					.done();
		
		expectedDataDefinition = buildDefinitionFrom(dataDefinitionFromStorage)
				.withoutFields()
				.withFields(titleFiled)
				.done();

		long newId = _dataDefinitionLocalService.save(
						toSave(expectedDataDefinition)
							.onBehalfOf(_user.getUserId())
							.inGroup(_group.getGroupId())
							.done()
					).getDataDefinitionId();

		assertEquals(id, newId);
		
		dataDefinitionFromStorage = _dataDefinitionLocalService.get(
				toGet().byId(id).done()
			).getDataDefinition();
		
		assertEquals(1, dataDefinitionFromStorage.getFields().size());
		assertNull(dataDefinitionFromStorage.getField("deleteMe"));
		assertEquals("Some tip", dataDefinitionFromStorage.getField("title").getTip(Locale.US));

		_dataDefinitionLocalService.delete(
				toDelete().byId(id).done()
			);

	}


	@Inject(type = DataDefinitionLocalService.class)
	private DataDefinitionLocalService _dataDefinitionLocalService;

	@DeleteAfterTestRun
	private Group _group;

	@Inject(type = ResourcePermissionLocalService.class)
	private ResourcePermissionLocalService _resourcePermissionLocalService;

	@Inject(type = RoleLocalService.class)
	private RoleLocalService _roleLocalService;

	@DeleteAfterTestRun
	private User _user;

}