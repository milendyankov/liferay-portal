/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.mobile.device.rulegroup.rule.impl;

import com.liferay.portal.kernel.mobile.device.Device;
import com.liferay.portal.kernel.mobile.device.Dimensions;
import com.liferay.portal.kernel.mobile.device.rulegroup.rule.RuleHandler;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.mobiledevicerules.model.MDRRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Edward Han
 * @author Milen Daynkov
 */
public class SimpleRuleHandler implements RuleHandler {

	public static final String PROPERTY_DISPLAY_HEIGHT_MAX =
		"display_height_max";
	public static final String PROPERTY_DISPLAY_WIDTH_MAX = "display_width_max";
	public static final String PROPERTY_DISPLAY_HEIGHT_MIN =
		"display_height_min";
	public static final String PROPERTY_DISPLAY_WIDTH_MIN = "display_width_min";
	public static final String PROPERTY_OS = "os";
	public static final String PROPERTY_RESOLUTION_HEIGHT_MAX =
		"resolution_height_max";
	public static final String PROPERTY_RESOLUTION_WIDTH_MAX =
		"resolution_width_max";
	public static final String PROPERTY_RESOLUTION_HEIGHT_MIN =
		"resolution_height_min";
	public static final String PROPERTY_RESOLUTION_WIDTH_MIN =
		"resolution_width_min";
	public static final String PROPERTY_TABLET = "tablet";

	public static String getHandlerType() {
		return SimpleRuleHandler.class.getName();
	}

	@Override
	public boolean evaluateRule(MDRRule mdrRule, ThemeDisplay themeDisplay) {
		Device device = themeDisplay.getDevice();

		if ((device == null) || Validator.isNull(device.getOS())) {
			return false;
		}

		UnicodeProperties typeSettingsProperties =
			mdrRule.getTypeSettingsProperties();

		String os = typeSettingsProperties.get(PROPERTY_OS);

		if (Validator.isNotNull(os)) {
			String[] operatingSystems = StringUtil.split(os);

			if (!ArrayUtil.contains(operatingSystems, device.getOS())) {
				return false;
			}
		}

		String tablet = typeSettingsProperties.get(PROPERTY_TABLET);

		if (Validator.isNotNull(tablet)) {
			boolean tabletBoolean = GetterUtil.getBoolean(tablet);

			if (tabletBoolean != device.isTablet()) {
				return false;
			}
		}

		Dimensions displaySize = device.getDisplaySize();

		if (!isAllowedValue(
				displaySize.getHeight(),
				typeSettingsProperties.get(PROPERTY_DISPLAY_HEIGHT_MIN),
				typeSettingsProperties.get(PROPERTY_DISPLAY_HEIGHT_MAX))) {

			return false;
		}

		if (!isAllowedValue(
				displaySize.getWidth(),
				typeSettingsProperties.get(PROPERTY_DISPLAY_WIDTH_MIN),
				typeSettingsProperties.get(PROPERTY_DISPLAY_WIDTH_MAX))) {

			return false;
		}

		Dimensions resolution = device.getResolution();

		if (!isAllowedValue(
				resolution.getHeight(),
				typeSettingsProperties.get(PROPERTY_RESOLUTION_HEIGHT_MIN),
				typeSettingsProperties.get(PROPERTY_RESOLUTION_HEIGHT_MAX))) {

			return false;
		}

		if (!isAllowedValue(
				resolution.getWidth(),
				typeSettingsProperties.get(PROPERTY_RESOLUTION_WIDTH_MIN),
				typeSettingsProperties.get(PROPERTY_RESOLUTION_WIDTH_MAX))) {

			return false;
		}

		return true;
	}

	@Override
	public Collection<String> getPropertyNames() {
		return _propertyNames;
	}

	@Override
	public String getType() {
		return getHandlerType();
	}

	protected boolean isAllowedValue(int value, String min, String max) {
		if (Validator.isNull(max) && Validator.isNull(min)) {
			return true;
		}

		if (Validator.isNotNull(max)) {
			int maxInt = GetterUtil.getInteger(max);

			if (value > maxInt) {
				return false;
			}
		}

		if (Validator.isNotNull(min)) {
			int minInt = GetterUtil.getInteger(min);

			if (value < minInt) {
				return false;
			}
		}

		return true;
	}

	private static Collection<String> _propertyNames;

	static {
		_propertyNames = new ArrayList<String>(10);

		_propertyNames.add(PROPERTY_OS);
		_propertyNames.add(PROPERTY_DISPLAY_WIDTH_MAX);
		_propertyNames.add(PROPERTY_DISPLAY_WIDTH_MIN);
		_propertyNames.add(PROPERTY_DISPLAY_HEIGHT_MAX);
		_propertyNames.add(PROPERTY_DISPLAY_HEIGHT_MIN);
		_propertyNames.add(PROPERTY_RESOLUTION_WIDTH_MAX);
		_propertyNames.add(PROPERTY_RESOLUTION_WIDTH_MIN);
		_propertyNames.add(PROPERTY_RESOLUTION_HEIGHT_MAX);
		_propertyNames.add(PROPERTY_RESOLUTION_HEIGHT_MIN);
		_propertyNames.add(PROPERTY_TABLET);

		_propertyNames = Collections.unmodifiableCollection(_propertyNames);
	}

}