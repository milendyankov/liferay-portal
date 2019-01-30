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

package com.liferay.arquillian.extension.junit.bridge.junit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.arquillian.test.spi.LifecycleMethodExecutor;
import org.jboss.arquillian.test.spi.TestMethodExecutor;
import org.jboss.arquillian.test.spi.TestResult;
import org.jboss.arquillian.test.spi.TestRunnerAdaptor;
import org.jboss.arquillian.test.spi.TestRunnerAdaptorBuilder;
import org.jboss.arquillian.test.spi.event.suite.AfterTestLifecycleEvent;
import org.jboss.arquillian.test.spi.event.suite.BeforeTestLifecycleEvent;
import org.jboss.arquillian.test.spi.execution.SkippedTestExecutionException;

import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.Fail;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.rules.MethodRule;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

/**
 * @author Shuyang Zhou
 */
public class Arquillian extends ParentRunner<FrameworkMethod> {

	public Arquillian(Class<?> clazz) throws InitializationError {
		super(clazz);

		_clazz = clazz;
	}

	@Override
	public void run(RunNotifier runNotifier) {
		_testRunnerAdaptor = _testRunnerAdaptorThreadLocal.get();

		if (_testRunnerAdaptor == null) {
			try {
				_testRunnerAdaptor = TestRunnerAdaptorBuilder.build();

				_testRunnerAdaptor.beforeSuite();

				_testRunnerAdaptorThreadLocal.set(_testRunnerAdaptor);
			}
			catch (Exception e) {
				runNotifier.fireTestFailure(new Failure(getDescription(), e));
			}
		}

		if (_testRunnerAdaptor == null) {
			return;
		}

		runNotifier.addListener(
			new RunListener() {

				@Override
				public void testRunFinished(Result result) throws Exception {
					try {
						_testRunnerAdaptor.afterSuite();

						_testRunnerAdaptor.shutdown();

						_testRunnerAdaptor = null;
					}
					finally {
						_testRunnerAdaptorThreadLocal.remove();
					}
				}

			});

		EachTestNotifier eachTestNotifier = new EachTestNotifier(
			runNotifier, getDescription());

		try {
			Statement statement = _classBlock(runNotifier);

			statement.evaluate();
		}
		catch (org.junit.internal.AssumptionViolatedException ave) {
			eachTestNotifier.addFailedAssumption(ave);
		}
		catch (StoppedByUserException sbue) {
			throw sbue;
		}
		catch (Throwable t) {
			eachTestNotifier.addFailure(t);
		}
	}

	@Override
	protected Description describeChild(FrameworkMethod frameworkMethod) {
		return _methodDescriptions.computeIfAbsent(
			frameworkMethod,
			keyFrameworkMethod -> {
				return Description.createTestDescription(
					_clazz, keyFrameworkMethod.getName(),
					keyFrameworkMethod.getAnnotations());
			});
	}

	@Override
	protected List<FrameworkMethod> getChildren() {
		TestClass testClass = _getTestClass();

		return testClass.getAnnotatedMethods(Test.class);
	}

	@Override
	protected boolean isIgnored(FrameworkMethod frameworkMethod) {
		if (frameworkMethod.getAnnotation(Ignore.class) != null) {
			return true;
		}

		return false;
	}

	@Override
	protected void runChild(
		FrameworkMethod frameworkMethod, RunNotifier runNotifier) {

		Description description = describeChild(frameworkMethod);

		if (isIgnored(frameworkMethod)) {
			runNotifier.fireTestIgnored(description);
		}
		else {
			Statement statement = _methodBlock(frameworkMethod);

			EachTestNotifier eachTestNotifier = new EachTestNotifier(
				runNotifier, description);

			eachTestNotifier.fireTestStarted();

			try {
				statement.evaluate();
			}
			catch (org.junit.internal.AssumptionViolatedException ave) {
				eachTestNotifier.addFailedAssumption(ave);
			}
			catch (Throwable e) {
				eachTestNotifier.addFailure(e);
			}
			finally {
				eachTestNotifier.fireTestFinished();
			}
		}
	}

	private Statement _classBlock(final RunNotifier notifier) {
		Statement statement = childrenInvoker(notifier);

		boolean hasTestMethod = false;

		for (FrameworkMethod frameworkMethod : getChildren()) {
			if (!isIgnored(frameworkMethod)) {
				hasTestMethod = true;

				break;
			}
		}

		if (hasTestMethod) {
			return new Statement() {

				@Override
				public void evaluate() throws Throwable {
					Throwable throwable = null;

					try {
						_testRunnerAdaptor.beforeClass(
							_clazz, LifecycleMethodExecutor.NO_OP);

						statement.evaluate();
					}
					catch (Throwable t) {
						throwable = t;
					}

					try {
						_testRunnerAdaptor.afterClass(
							_clazz, LifecycleMethodExecutor.NO_OP);
					}
					catch (Throwable t) {
						if (throwable != null) {
							t.addSuppressed(throwable);
						}

						throwable = t;
					}

					if (throwable != null) {
						throw throwable;
					}
				}

			};
		}

		return statement;
	}

	private List<MethodRule> _getMethodRules(Object testObject) {
		TestClass testClass = _getTestClass();

		List<MethodRule> methodRules = testClass.getAnnotatedMethodValues(
			testObject, Rule.class, MethodRule.class);

		methodRules.addAll(
			testClass.getAnnotatedFieldValues(
				testObject, Rule.class, MethodRule.class));

		return methodRules;
	}

	private TestClass _getTestClass() {
		if (_testClass == null) {
			_testClass = new TestClass(_clazz) {

				@Override
				public List<FrameworkMethod> getAnnotatedMethods(
					Class<? extends Annotation> annotationClass) {

					List<FrameworkMethod> frameworkMethods = new ArrayList<>(
						super.getAnnotatedMethods(annotationClass));

					frameworkMethods.sort(
						Comparator.comparing(FrameworkMethod::getName));

					return frameworkMethods;
				}

			};
		}

		return _testClass;
	}

	private Statement _methodBlock(FrameworkMethod frameworkMethod) {
		Object testObject = null;

		try {
			TestClass testClass = _getTestClass();

			Constructor<?> constructor = testClass.getOnlyConstructor();

			testObject = constructor.newInstance();
		}
		catch (ReflectiveOperationException roe) {
			if (roe instanceof InvocationTargetException) {
				return new Fail(roe.getCause());
			}

			return new Fail(roe);
		}

		final Object test = testObject;

		Statement statement = _methodInvoker(frameworkMethod, test);

		statement = _possiblyExpectingExceptions(frameworkMethod, statement);

		statement = _withPotentialTimeout(frameworkMethod, statement);

		final Statement oldStatement = statement;

		for (MethodRule methodRule : _getMethodRules(test)) {
			statement = methodRule.apply(statement, frameworkMethod, test);
		}

		final Statement newStatement = statement;

		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				final AtomicBoolean flag = new AtomicBoolean();

				Throwable throwable = null;

				try {
					_testRunnerAdaptor.fireCustomLifecycle(
						new BeforeTestLifecycleEvent(
							test, frameworkMethod.getMethod(),
							() -> {
								flag.set(true);

								newStatement.evaluate();
							}));

					if (flag.get() == false) {
						oldStatement.evaluate();
					}
				}
				catch (Throwable t) {
					throwable = t;
				}
				finally {
					try {
						_testRunnerAdaptor.fireCustomLifecycle(
							new AfterTestLifecycleEvent(
								test, frameworkMethod.getMethod(),
								LifecycleMethodExecutor.NO_OP));
					}
					catch (Throwable t) {
						if (throwable != null) {
							t.addSuppressed(throwable);
						}

						throwable = t;
					}
				}

				if (throwable != null) {
					throw throwable;
				}
			}

		};
	}

	private Statement _methodInvoker(
		FrameworkMethod frameworkMethod, Object testObject) {

		return new Statement() {

			@Override
			public void evaluate() throws Throwable {
				TestResult testResult = _testRunnerAdaptor.test(
					new TestMethodExecutor() {

						public Object getInstance() {
							return testObject;
						}

						@Override
						public Method getMethod() {
							return frameworkMethod.getMethod();
						}

						@Override
						public void invoke(Object... parameters)
							throws Throwable {

							try {
								frameworkMethod.invokeExplosively(
									testObject, parameters);
							}
							catch (Throwable t) {
								State.caughtTestException(t);

								throw t;
							}
						}

					});

				Throwable throwable = testResult.getThrowable();

				if (throwable == null) {
					return;
				}

				if ((testResult.getStatus() == TestResult.Status.SKIPPED) &&
					(throwable instanceof SkippedTestExecutionException)) {

					testResult.setThrowable(
						new AssumptionViolatedException(
							throwable.getMessage()));
				}

				throw throwable;
			}

		};
	}

	private Statement _possiblyExpectingExceptions(
		FrameworkMethod frameworkMethod, Statement statement) {

		Test test = frameworkMethod.getAnnotation(Test.class);

		if ((test == null) || (test.expected() == Test.None.class)) {
			return statement;
		}

		return new ExpectException(statement, test.expected());
	}

	private Statement _withPotentialTimeout(
		FrameworkMethod frameworkMethod, Statement statement) {

		Test test = frameworkMethod.getAnnotation(Test.class);

		if ((test == null) || (test.timeout() <= 0)) {
			return statement;
		}

		FailOnTimeout.Builder builder = FailOnTimeout.builder();

		builder.withTimeout(test.timeout(), TimeUnit.MILLISECONDS);

		return builder.build(statement);
	}

	private static final ThreadLocal<TestRunnerAdaptor>
		_testRunnerAdaptorThreadLocal = new ThreadLocal<>();

	private final Class<?> _clazz;
	private final Map<FrameworkMethod, Description> _methodDescriptions =
		new ConcurrentHashMap<>();
	private TestClass _testClass;
	private TestRunnerAdaptor _testRunnerAdaptor;

}