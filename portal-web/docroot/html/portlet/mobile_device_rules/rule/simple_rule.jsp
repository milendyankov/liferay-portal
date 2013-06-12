<%--
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
--%>

<%@ include file="/html/portlet/mobile_device_rules/init.jsp" %>

<%
MDRRule rule = (MDRRule)request.getAttribute(WebKeys.MOBILE_DEVICE_RULES_RULE);

Set<String> operatingSystems = Collections.emptySet();

int tablet = 0;

String displayHeightMax = StringPool.BLANK;
String displayHeightMin = StringPool.BLANK;
String displayWidthMax = StringPool.BLANK;
String displayWidthMin = StringPool.BLANK;

String resolutionHeightMax = StringPool.BLANK;
String resolutionHeightMin = StringPool.BLANK;
String resolutionWidthMax = StringPool.BLANK;
String resolutionWidthMin = StringPool.BLANK;

if (rule != null) {
	UnicodeProperties typeSettingsProperties = rule.getTypeSettingsProperties();

	operatingSystems = SetUtil.fromArray(StringUtil.split(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_OS)));

	String tabletString = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_TABLET));

	if (tabletString.equals(StringPool.TRUE)) {
		tablet = 1;
	}
	else if (tabletString.equals(StringPool.FALSE)) {
		tablet = 2;
	}

	displayHeightMax = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MAX));
	displayHeightMin = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MIN));
	displayWidthMax = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MAX));
	displayWidthMin = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MIN));

	resolutionHeightMax = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MAX));
	resolutionHeightMin = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MIN));
	resolutionWidthMax = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MAX));
	resolutionWidthMin = GetterUtil.getString(typeSettingsProperties.get(SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MIN));
}

Set<Dimensions> knownDisplaySizes = DeviceDetectionUtil.getKnownDisplaySizes();
Set<Dimensions> knownScreenResolutions = DeviceDetectionUtil.getKnownScreenResolutions();
%>

<c:choose>
	<c:when test="<%= PluginPackageUtil.isInstalled("wurfl-web") %>">
		<h3><liferay-ui:message key="generic-product-information" /></h3>

		<aui:select multiple="<%= true %>" name="os">

			<aui:option label="any-os" selected="<%= operatingSystems.isEmpty() %>" value="" />

			<%
			Set<VersionableName> knownOperationSystems = DeviceDetectionUtil.getKnownOperatingSystems();

			for (VersionableName knownOperationSystem : knownOperationSystems) {
			%>

				<aui:option label="<%= knownOperationSystem.getName() %>" selected="<%= operatingSystems.contains(knownOperationSystem.getName()) %>" />

			<%
			}
			%>

		</aui:select>

		<aui:select label="device-type" name="tablet">
			<aui:option label="any" selected="<%= tablet == 0 %>" value="" />
			<aui:option label="tablets" selected="<%= tablet == 1 %>" value="<%= true %>" />
			<aui:option label="other-devices" selected="<%= tablet == 2 %>" value="<%= false %>" />
		</aui:select>

		<h3><liferay-ui:message key="screen-size-in-millimiters" /></h3>


		<div class="row-fields">
			<liferay-ui:message key="minimum-display-size" />

			<aui:input
				cssClass="custom-min-display-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MIN %>"
				inlineField="<%= true %>"
				label="width"
				name="<%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MIN %>"
				style="max-width:50px;"
				value="<%= displayWidthMin %>" />
			<%= _SEPARATOR %>
			<aui:input
				cssClass="custom-min-display-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MIN %>"
				inlineField="<%= true %>"
				label="height"
				name="<%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MIN %>"
				style="max-width:50px;"
				value="<%= displayHeightMin %>" />

			(<liferay-ui:message key="known-display-sizes" />

			<aui:select
				id="min-known-display-sizes"
				inlineField="<%= true %>"
				label=""
				multiple="<%= false %>"
				name="min-known-display-sizes"
				style="max-width:100px;">

				<aui:option label="" />

				<%
				for (Dimensions knownDisplaySize : knownDisplaySizes) {
				%>

					<aui:option label="<%= knownDisplaySize.getWidth() + _SEPARATOR + knownDisplaySize.getHeight() %>" />

				<%
				}
				%>

			</aui:select>
			)
		</div>

		<div class="row-fields">
			<liferay-ui:message key="maximum-display-size" />

			<aui:input
				cssClass="custom-max-display-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MAX %>"
				inlineField="<%= true %>"
				label="width"
				name="<%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MAX %>"
				style="max-width:50px;"
				value="<%= displayWidthMax %>" />
			<%= _SEPARATOR %>
			<aui:input
				cssClass="custom-max-display-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MAX %>"
				inlineField="<%= true %>"
				label="height"
				name="<%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MAX %>"
				style="max-width:50px;"
				value="<%= displayHeightMax %>" />

			(<liferay-ui:message key="known-display-sizes" />

			<aui:select
				id="max-known-display-sizes"
				inlineField="<%= true %>"
				label=""
				multiple="<%= false %>"
				name="max-known-display-sizes"
				style="max-width:100px;">

				<aui:option label="" />

				<%
				for (Dimensions knownDisplaySize : knownDisplaySizes) {
				%>

					<aui:option label="<%= knownDisplaySize.getWidth() + _SEPARATOR + knownDisplaySize.getHeight() %>" />

				<%
				}
				%>

			</aui:select>
			)
		</div>

		<h3><liferay-ui:message key="screen-resolution" /></h3>

		<div class="row-fields">
			<liferay-ui:message key="minimum-screen-resolution" />

			<aui:input
				cssClass="custom-min-resolution-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MIN %>"
				inlineField="<%= true %>"
				label="width"
				name="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MIN %>"
				style="max-width:50px;"
				value="<%= resolutionWidthMin %>" />
			<%= _SEPARATOR %>
			<aui:input
				cssClass="custom-min-resolution-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MIN %>"
				inlineField="<%= true %>"
				label="height"
				name="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MIN %>"
				style="max-width:50px;"
				value="<%= resolutionHeightMin %>" />

			(<liferay-ui:message key="known-screen-resolutions" />

			<aui:select
				id="min-known-screen-resolutions"
				inlineField="<%= true %>"
				label=""
				multiple="<%= false %>"
				name="min-known-screen-resolutions"
				style="max-width:100px;"
				>

				<aui:option label="" />

				<%
				for (Dimensions knownScreenResolution : knownScreenResolutions) {
				%>

					<aui:option label="<%= knownScreenResolution.getWidth() + _SEPARATOR + knownScreenResolution.getHeight() %>" />

				<%
				}
				%>

			</aui:select>
			)
		</div>

		<div class="row-fields">
			<liferay-ui:message key="maximum-screen-resolution" />

			<aui:input
				cssClass="custom-max-resolution-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MAX %>"
				inlineField="<%= true %>"
				label="width"
				name="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MAX %>"
				style="max-width:50px;"
				value="<%= resolutionWidthMax %>" />
			<%= _SEPARATOR %>
			<aui:input
				cssClass="custom-max-resolution-field aui-field-digits"
				id="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MAX %>"
				inlineField="<%= true %>"
				label="height"
				name="<%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MAX %>"
				style="max-width:50px;"
				value="<%= resolutionHeightMax %>" />

			(<liferay-ui:message key="known-screen-resolutions" />

			<aui:select
				id="max-known-screen-resolutions"
				inlineField="<%= true %>"
				label=""
				multiple="<%= false %>"
				name="max-known-screen-resolutions"
				style="max-width:100px;">

				<aui:option label="" />

				<%
				for (Dimensions knownScreenResolution : knownScreenResolutions) {
				%>

					<aui:option label="<%= knownScreenResolution.getWidth() + _SEPARATOR + knownScreenResolution.getHeight() %>" />

				<%
				}
				%>

			</aui:select>
			)

		</div>
	</c:when>
	<c:otherwise>
		<liferay-ui:message key="the-device-recognition-provider-is-not-present" />
	</c:otherwise>
</c:choose>

<script>
	YUI().use(
		'aui-node',
		function(Y) {
			var maxKnownSizes = Y.one('#<portlet:namespace/>max-known-display-sizes');
			var maxSizeHeight = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MAX %>');
			var maxSizeWidth = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MAX %>');

			var minKnownSizes = Y.one('#<portlet:namespace/>min-known-display-sizes');
			var minSizeHeight = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_DISPLAY_HEIGHT_MIN %>');
			var minSizeWidth = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_DISPLAY_WIDTH_MIN %>');

			var maxKnownResolutions = Y.one('#<portlet:namespace/>max-known-screen-resolutions');
			var maxResWidth = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MAX %>');
			var maxResHeight = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MAX %>');

			var minKnownResolutions = Y.one('#<portlet:namespace/>min-known-screen-resolutions');
			var minResWidth = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_RESOLUTION_WIDTH_MIN %>');
			var minResHeight = Y.one('#<portlet:namespace/><%= SimpleRuleHandler.PROPERTY_RESOLUTION_HEIGHT_MIN %>');

			maxKnownSizes.on(
			    'change',
			    function() {
			  		var sizes = maxKnownSizes.val().split("<%= _SEPARATOR %>");
			  		maxSizeWidth.val(sizes[0]);
			  		maxSizeHeight.val(sizes[1]);
			    }
			);

			minKnownSizes.on(
			    'change',
			    function() {
			  		var sizes = minKnownSizes.val().split("<%= _SEPARATOR %>");
			  		minSizeWidth.val(sizes[0]);
			  		minSizeHeight.val(sizes[1]);
			    }
			);

			maxKnownResolutions.on(
				'change',
				function() {
					var sizes = maxKnownResolutions.val().split("<%= _SEPARATOR %>");
					maxResWidth.val(sizes[0]);
					maxResHeight.val(sizes[1]);
			  	}
			);

			minKnownResolutions.on(
				'change',
				function() {
					var sizes = minKnownResolutions.val().split("<%= _SEPARATOR %>");
					minResWidth.val(sizes[0]);
					minResHeight.val(sizes[1]);
			  	}
			);

			Y.all('.custom-max-display-field').on(
				'change',
				function() {
					maxKnownSizes.set('selectedIndex', 0);
			  	}
			);


			Y.all('.custom-min-display-field').on(
				'change',
				function() {
					minKnownSizes.set('selectedIndex', 0);
			  	}
			);

			Y.all('.custom-max-resolution-field').on(
				'change',
				function() {
					maxKnownResolutions.set('selectedIndex', 0);
			  	}
			);

			Y.all('.custom-min-resolution-field').on(
				'change',
				function() {
					minKnownResolutions.set('selectedIndex', 0);
			  	}
			);
		}
	);
</script>

<%!
private final static String _SEPARATOR = " x ";
%>