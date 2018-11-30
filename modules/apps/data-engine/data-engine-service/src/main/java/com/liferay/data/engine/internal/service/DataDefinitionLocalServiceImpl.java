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

package com.liferay.data.engine.internal.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import com.liferay.data.engine.exception.DataDefinitionException;
import com.liferay.data.engine.exception.DataDefinitionFieldsDeserializerException;
import com.liferay.data.engine.exception.DataDefinitionFieldsSerializerException;
import com.liferay.data.engine.internal.io.DataDefinitionFieldsDeserializerTracker;
import com.liferay.data.engine.internal.io.DataDefinitionFieldsSerializerTracker;
import com.liferay.data.engine.io.DataDefinitionFieldsDeserializer;
import com.liferay.data.engine.io.DataDefinitionFieldsDeserializerApplyRequest;
import com.liferay.data.engine.io.DataDefinitionFieldsDeserializerApplyResponse;
import com.liferay.data.engine.io.DataDefinitionFieldsSerializer;
import com.liferay.data.engine.io.DataDefinitionFieldsSerializerApplyRequest;
import com.liferay.data.engine.io.DataDefinitionFieldsSerializerApplyResponse;
import com.liferay.data.engine.model.DataDefinition;
import com.liferay.data.engine.model.DataDefinitionField;
import com.liferay.data.engine.service.DataDefinitionDeleteRequest;
import com.liferay.data.engine.service.DataDefinitionDeleteResponse;
import com.liferay.data.engine.service.DataDefinitionGetRequest;
import com.liferay.data.engine.service.DataDefinitionGetResponse;
import com.liferay.data.engine.service.DataDefinitionLocalService;
import com.liferay.data.engine.service.DataDefinitionSaveRequest;
import com.liferay.data.engine.service.DataDefinitionSaveResponse;
import com.liferay.dynamic.data.lists.model.DDLRecordSet;
import com.liferay.dynamic.data.lists.model.DDLRecordSetConstants;
import com.liferay.dynamic.data.lists.service.DDLRecordSetLocalService;
import com.liferay.dynamic.data.mapping.model.DDMStructure;
import com.liferay.dynamic.data.mapping.model.DDMStructureConstants;
import com.liferay.dynamic.data.mapping.service.DDMStructureLocalService;
import com.liferay.portal.kernel.dao.orm.ActionableDynamicQuery;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.ResourceLocalService;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.UserLocalService;
import com.liferay.portal.kernel.service.permission.ModelPermissions;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.Portal;

/**
 * @author Leonardo Barros
 */
@Component(immediate = true, service = DataDefinitionLocalService.class)
public class DataDefinitionLocalServiceImpl
	implements DataDefinitionLocalService {

	@Override
	public DataDefinitionDeleteResponse delete(
			DataDefinitionDeleteRequest dataDefinitionDeleteRequest)
		throws DataDefinitionException {

		try {
			long dataDefinitionId =
				dataDefinitionDeleteRequest.getDataDefinitionId();

			deleteDDLRecordSet(dataDefinitionId);

			ddmStructureLocalService.deleteDDMStructure(dataDefinitionId);

			return DataDefinitionDeleteResponse.Builder.of(dataDefinitionId);
		}
		catch (Exception e)
		{
			throw new DataDefinitionException(e);
		}
	}

	@Override
	public DataDefinitionGetResponse get(
			DataDefinitionGetRequest dataDefinitionGetRequest)
		throws DataDefinitionException {

		try {
			long dataDefinitionId =
				dataDefinitionGetRequest.getDataDefinitionId();

			DDMStructure ddmStructure = ddmStructureLocalService.getStructure(
				dataDefinitionId);

			return DataDefinitionGetResponse.Builder.of(map(ddmStructure));
		}
		catch (Exception e)
		{
			throw new DataDefinitionException(e);
		}
	}

	@Override
	public DataDefinitionSaveResponse save(
			DataDefinitionSaveRequest dataDefinitionSaveRequest)
		throws DataDefinitionException {

		try {
			long userId = dataDefinitionSaveRequest.getUserId();

			long groupId = dataDefinitionSaveRequest.getGroupId();

			long classNameId = portal.getClassNameId(DataDefinition.class);

			DataDefinition dataDefinition =
				dataDefinitionSaveRequest.getDataDefinition();

			long dataDefinitionId = dataDefinition.getDataDefinitionId();

			/**
			 *  It is fine for now to assumes we are in portal environment 
			 *  but in the future we may want to have this in stand-alone environment 
			 *  
			 *  The `if` below makes a poor attempt to distinguish the two scenarios
			 *  I don't expect this code to made it in production in this form.
			 *  I wrote it this way to demonstrate the the need to hide internal details 
			 *  from the consumer!
			 */

			if (userLocalService != null && groupLocalService != null) {

				/**
				 *  In portal scenario we always have all the services (user, permission, etc)
				 *   
				 *  Since `ServiceContext` ONLY makes sense in portal environment 
				 *  it MUST NOT be exposed to consumers. Let alone expecting them to 
				 *  know to use it via `ThreadLocal`s.
				 */
			
				ServiceContext serviceContext = createServiceContext(
						groupLocalService.getGroup(groupId),
						userLocalService.getUser(userId),
						createModelPermissions());
				
				if (dataDefinitionId == 0) {
					DDMStructure ddmStructure = createDDMStructure(
						userId, groupId, classNameId, dataDefinition,
						serviceContext);
	
					dataDefinitionId = ddmStructure.getStructureId();
	
					resourceLocalService.addModelResources(
						ddmStructure.getCompanyId(), groupId, userId,
						DataDefinition.class.getName(), dataDefinitionId,
						serviceContext.getModelPermissions());
	
					ddlRecordSetLocalService.addRecordSet(
						userId, groupId, dataDefinitionId,
						String.valueOf(dataDefinitionId), ddmStructure.getNameMap(),
						ddmStructure.getDescriptionMap(), 0,
						DDLRecordSetConstants.SCOPE_DATA_ENGINE, serviceContext);
				}
				else {
					updateDDMStructure(userId, dataDefinition, serviceContext);
				}
				
			} else {
				
				/**
				 *  Throw exception for now but in the future we may want to support
				 *  stand-alone environment where portal services are not present 
				 *  (data is then stored anonymously and not scoped). 
				 */
				
				throw new IllegalStateException ("Missing important services");
			}

			return DataDefinitionSaveResponse.Builder.of(dataDefinitionId);
		}
		catch (Exception e)
		{
			throw new DataDefinitionException(e);
		}
	}

	protected DDMStructure createDDMStructure(
			long userId, long groupId, long classNameId,
			DataDefinition dataDefinition, ServiceContext serviceContext)
		throws PortalException {

		Map<Locale, String> nameMap = createLocalizedMap(
			dataDefinition.getName());

		Map<Locale, String> descriptionMap = createLocalizedMap(
			dataDefinition.getDescription());

		return ddmStructureLocalService.addStructure(
			userId, groupId, classNameId,
			DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID, null, nameMap,
			descriptionMap, serialize(dataDefinition),
			dataDefinition.getStorageType(), serviceContext);
	}

	protected Map<Locale, String> createLocalizedMap(Map<String, String> map) {
		Set<Map.Entry<String, String>> entrySet = map.entrySet();

		Stream<Map.Entry<String, String>> entryStream = entrySet.stream();

		return entryStream.collect(
			Collectors.toMap(
				entry -> LocaleUtil.fromLanguageId(entry.getKey()),
				entry -> entry.getValue()));
	}

	protected void deleteDDLRecordSet(long dataDefinitionId) {
		ActionableDynamicQuery actionableDynamicQuery =
			ddlRecordSetLocalService.getActionableDynamicQuery();

		actionableDynamicQuery.setAddCriteriaMethod(
			dynamicQuery -> {
				Property ddmStructureIdProperty = PropertyFactoryUtil.forName(
					"DDMStructureId");

				dynamicQuery.add(ddmStructureIdProperty.eq(dataDefinitionId));
			});

		actionableDynamicQuery.setPerformActionMethod(
			(DDLRecordSet ddlRecordSet) ->
				ddlRecordSetLocalService.deleteDDLRecordSet(ddlRecordSet));
	}

	protected List<DataDefinitionField> deserialize(String content)
		throws DataDefinitionFieldsDeserializerException {

		DataDefinitionFieldsDeserializer dataDefinitionFieldsDeserializer =
			dataDefinitionFieldsDeserializerTracker.
				getDataDefinitionFieldsDeserializer("json");

		DataDefinitionFieldsDeserializerApplyRequest
			dataDefinitionFieldsDeserializerApplyRequest =
				DataDefinitionFieldsDeserializerApplyRequest.Builder.newBuilder(
					content
				).build();

		DataDefinitionFieldsDeserializerApplyResponse
			dataDefinitionFieldsDeserializerApplyResponse =
				dataDefinitionFieldsDeserializer.apply(
					dataDefinitionFieldsDeserializerApplyRequest);

		return dataDefinitionFieldsDeserializerApplyResponse.
			getDataDefinitionFields();
	}

	protected DataDefinition map(DDMStructure ddmStructure)
		throws DataDefinitionFieldsDeserializerException {

		List<DataDefinitionField> dataDefinitionFields = deserialize(
			ddmStructure.getDefinition());

		return DataDefinition.buildDefinition()
				.withId(ddmStructure.getStructureId())
				.withNames(ddmStructure.getNameMap())
				.withDescriptions(ddmStructure.getDescriptionMap())
				.ofStorageType(ddmStructure.getStorageType())
				.withFields(dataDefinitionFields)
				.done();
	}

	protected String serialize(DataDefinition dataDefinition)
		throws DataDefinitionFieldsSerializerException {

		DataDefinitionFieldsSerializer dataDefinitionFieldsSerializer =
			dataDefinitionFieldsSerializerTracker.
				getDataDefinitionFieldsSerializer("json");

		DataDefinitionFieldsSerializerApplyRequest
			dataDefinitionFieldsSerializerApplyRequest =
				DataDefinitionFieldsSerializerApplyRequest.Builder.of(
					dataDefinition.getFields());

		DataDefinitionFieldsSerializerApplyResponse
			dataDefinitionFieldsSerializerApplyResponse =
				dataDefinitionFieldsSerializer.apply(
					dataDefinitionFieldsSerializerApplyRequest);

		return dataDefinitionFieldsSerializerApplyResponse.getContent();
	}

	protected void updateDDMStructure(
			long userId, DataDefinition dataDefinition,
			ServiceContext serviceContext)
		throws PortalException {

		Map<Locale, String> nameMap = createLocalizedMap(
			dataDefinition.getName());

		Map<Locale, String> descriptionMap = createLocalizedMap(
			dataDefinition.getDescription());

		ddmStructureLocalService.updateStructure(
			userId, dataDefinition.getDataDefinitionId(),
			DDMStructureConstants.DEFAULT_PARENT_STRUCTURE_ID, nameMap,
			descriptionMap, serialize(dataDefinition), serviceContext);
	}

	
	protected ServiceContext createServiceContext(
		Group group, User user, ModelPermissions modelPermissions) {

		ServiceContext serviceContext = new ServiceContext();

		serviceContext.setAddGroupPermissions(false);
		serviceContext.setAddGuestPermissions(false);
		serviceContext.setCompanyId(group.getCompanyId());
		serviceContext.setModelPermissions(modelPermissions);
		serviceContext.setScopeGroupId(group.getGroupId());
		serviceContext.setUserId(user.getUserId());

		return serviceContext;
	}
	
	protected ModelPermissions createModelPermissions() {
		ModelPermissions modelPermissions = new ModelPermissions();

		modelPermissions.addRolePermissions(
			RoleConstants.OWNER, ActionKeys.VIEW);

		return modelPermissions;
	}
	
	@Reference
	protected DataDefinitionFieldsDeserializerTracker
		dataDefinitionFieldsDeserializerTracker;

	@Reference
	protected DataDefinitionFieldsSerializerTracker
		dataDefinitionFieldsSerializerTracker;

	@Reference
	protected DDLRecordSetLocalService ddlRecordSetLocalService;

	@Reference
	protected DDMStructureLocalService ddmStructureLocalService;

	@Reference
	protected Portal portal;

	@Reference
	protected ResourceLocalService resourceLocalService;

	@Reference(cardinality=ReferenceCardinality.OPTIONAL)
	protected GroupLocalService groupLocalService;

	@Reference(cardinality=ReferenceCardinality.OPTIONAL)
	protected UserLocalService userLocalService;

}