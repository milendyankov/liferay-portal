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

package com.liferay.arquillian.extension.junit.bridge.remote.manager;

import com.liferay.arquillian.extension.junit.bridge.LiferayArquillianJUnitBridgeExtension;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.ApplicationScoped;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.spi.HashObjectStore;
import org.jboss.arquillian.core.spi.NonManagedObserver;
import org.jboss.arquillian.core.spi.context.AbstractContext;
import org.jboss.arquillian.core.spi.context.ApplicationContext;
import org.jboss.arquillian.core.spi.context.ObjectStore;

/**
 * @author Matthew Tambara
 */
public class Manager {

	public Manager() throws ReflectiveOperationException {
		_applicationContext = new ApplicationContextImpl();

		_extensions.addAll(
			_createExtensions(
				LiferayArquillianJUnitBridgeExtension.getObservers()));

		_applicationContext.activate();
	}

	public <T> void bind(Class<T> type, T instance) {
		ObjectStore objectStore = _applicationContext.getObjectStore();

		objectStore.add(type, instance);
	}

	public void fire(Object event) {
		fire(event, null);
	}

	public <T> void fire(T event, NonManagedObserver<T> nonManagedObserver) {
		List<Observer> observers = new ArrayList<>();

		Class<?> eventClass = event.getClass();

		for (Object extension : _extensions) {
			for (Observer observer : Observer.getObservers(extension)) {
				Type type = observer.getType();

				Class<?> clazz = (Class<?>)type;

				if (clazz.isAssignableFrom(eventClass)) {
					observers.add(observer);
				}
			}
		}

		if ((nonManagedObserver == null) && observers.isEmpty()) {
			return;
		}

		boolean activatedApplicationContext = false;

		try {
			if (!_applicationContext.isActive()) {
				_applicationContext.activate();

				activatedApplicationContext = true;
			}

			_proceed(observers, nonManagedObserver, event);
		}
		catch (ReflectiveOperationException roe) {
			if (roe instanceof InvocationTargetException) {
				_throwException(roe.getCause());
			}
			else {
				_throwException(roe);
			}
		}
		finally {
			if (activatedApplicationContext && _applicationContext.isActive()) {
				_applicationContext.deactivate();
			}
		}
	}

	public <T> T resolve(Class<T> type) {
		if (!_applicationContext.isActive()) {
			return null;
		}

		ObjectStore objectStore = _applicationContext.getObjectStore();

		T object = objectStore.get(type);

		if (object != null) {
			return object;
		}

		return null;
	}

	public void start() {
		fire(new ManagerStarted());

		_applicationContext.activate();
	}

	private static <T, E extends Throwable> T _throwException(
			Throwable throwable)
		throws E {

		throw (E)throwable;
	}

	private List<Object> _createExtensions(
			Collection<Class<?>> extensionClasses)
		throws ReflectiveOperationException {

		List<Object> created = new ArrayList<>();

		for (Class<?> extensionClass : extensionClasses) {
			Object extension = _createInstance(extensionClass);

			_inject(extension);

			created.add(extension);
		}

		return created;
	}

	private <T>T _createInstance(Class<T> clazz) {
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();

			constructor.setAccessible(true);

			return constructor.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void _inject(Object extension) throws ReflectiveOperationException {
		for (InjectionPoint injectionPoint :
				InjectionPoint.getInjections(extension)) {

			injectionPoint.set(this);
		}
	}

	private <T> void _proceed(
			List<Observer> observers, NonManagedObserver<T> nonManagedObserver,
			T event)
		throws ReflectiveOperationException {

		for (Observer observer : observers) {
			observer.invoke(this, event);
		}

		if (nonManagedObserver != null) {
			_inject(nonManagedObserver);

			nonManagedObserver.fired(event);
		}
	}

	private final ApplicationContext _applicationContext;
	private final List<Object> _extensions = new ArrayList<>();

	private class ApplicationContextImpl
		extends AbstractContext<String> implements ApplicationContext {

		@Override
		public void activate() {
			super.activate(_APP_CONTEXT_ID);
		}

		@Override
		public void destroy() {
			super.destroy(_APP_CONTEXT_ID);
		}

		@Override
		public Class<? extends Annotation> getScope() {
			return ApplicationScoped.class;
		}

		@Override
		protected ObjectStore createNewObjectStore() {
			return new HashObjectStore();
		}

		private static final String _APP_CONTEXT_ID = "app";

	}

}