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

package com.liferay.portal.template.soy.internal;

import com.google.common.io.CharStreams;
import com.google.template.soy.SoyFileSet;

import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.cache.SingleVMPool;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.template.TemplateResource;
import com.liferay.portal.kernel.template.URLTemplateResource;
import com.liferay.portal.kernel.test.ReflectionTestUtil;
import com.liferay.portal.kernel.util.ProxyUtil;
import com.liferay.portal.template.soy.SoyTemplateResourceFactory;

import java.io.Reader;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Marcellus Tavares
 * @author Bruno Basto
 */
public class SoyTestHelper {

	public SoyManager getSoyManager() {
		return _soyManager;
	}

	public SoyTemplate getSoyTemplate(
		List<TemplateResource> templateResources) {

		return (SoyTemplate)_soyManager.getTemplate(
			_soyTemplateResourceFactory.createSoyTemplateResource(
				templateResources),
			false);
	}

	public SoyTemplate getSoyTemplate(String fileName) {
		TemplateResource templateResource = getTemplateResource(fileName);

		return (SoyTemplate)_soyManager.getTemplate(templateResource, false);
	}

	public SoyTemplate getSoyTemplate(String... fileNames) {
		List<TemplateResource> templateResources = getTemplateResources(
			Arrays.asList(fileNames));

		return getSoyTemplate(templateResources);
	}

	public void setUp() {
		JSONFactoryUtil jsonFactoryUtil = new JSONFactoryUtil();

		jsonFactoryUtil.setJSONFactory(new JSONFactoryImpl());

		setUpSoyManager();
	}

	public void tearDown() {
		_soyManager.destroy();
	}

	protected SoyFileSet getSoyFileSet(List<TemplateResource> templateResources)
		throws Exception {

		SoyFileSet.Builder builder = SoyFileSet.builder();

		for (TemplateResource templateResource : templateResources) {
			Reader reader = templateResource.getReader();

			builder.add(
				CharStreams.toString(reader), templateResource.getTemplateId());
		}

		return builder.build();
	}

	protected TemplateResource getTemplateResource(String name) {
		TemplateResource templateResource = null;

		String resource = _TPL_PATH.concat(name);

		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		URL url = classLoader.getResource(resource);

		if (url != null) {
			templateResource = new URLTemplateResource(resource, url);
		}

		return templateResource;
	}

	protected List<TemplateResource> getTemplateResources(
		List<String> fileNames) {

		List<TemplateResource> templateResources = new ArrayList<>();

		for (String fileName : fileNames) {
			templateResources.add(getTemplateResource(fileName));
		}

		return templateResources;
	}

	protected PortalCache mockPortalCache() {
		Map<List<TemplateResource>, SoyTofuCacheBag> cache = new HashMap<>();

		return (PortalCache)ProxyUtil.newProxyInstance(
			PortalCache.class.getClassLoader(),
			new Class<?>[] {PortalCache.class},
			(proxy, method, args) -> {
				String methodName = method.getName();

				if (methodName.equals("get")) {
					return cache.get(args[0]);
				}
				else if (methodName.equals("getKeys")) {
					return new ArrayList<>(cache.keySet());
				}
				else if (methodName.equals("put")) {
					cache.put(
						(List<TemplateResource>)args[0],
						(SoyTofuCacheBag)args[1]);
				}
				else if (methodName.equals("remove")) {
					cache.remove(args[0]);
				}

				return null;
			});
	}

	protected void setUpSoyManager() {
		_soyTemplateResourceFactory = new SoyTemplateResourceFactoryImpl();

		_soyManager = new SoyManager();

		_soyManager.setTemplateContextHelper(new SoyTemplateContextHelper());

		_soyManager.setSingleVMPool(
			(SingleVMPool)ProxyUtil.newProxyInstance(
				SingleVMPool.class.getClassLoader(),
				new Class<?>[] {SingleVMPool.class},
				(proxy, method, args) -> {
					if (Objects.equals(method.getName(), "getPortalCache")) {
						return mockPortalCache();
					}

					throw new UnsupportedOperationException(method.toString());
				}));

		ReflectionTestUtil.setFieldValue(
			_soyManager, "_soyTemplateResourceFactory",
			_soyTemplateResourceFactory);
	}

	private static final String _TPL_PATH =
		"com/liferay/portal/template/soy/dependencies/";

	private SoyManager _soyManager;
	private SoyTemplateResourceFactory _soyTemplateResourceFactory;

}