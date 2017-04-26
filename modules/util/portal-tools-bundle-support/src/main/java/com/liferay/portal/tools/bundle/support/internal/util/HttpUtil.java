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

package com.liferay.portal.tools.bundle.support.internal.util;

import com.beust.jcommander.internal.Lists;

import com.liferay.portal.tools.bundle.support.util.StreamLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * @author David Truong
 */
public class HttpUtil {

	public static final String LIFERAY_TOKEN_URL =
		"https://web.liferay.com/token-auth-portlet/api/secure/jsonws" +
			"/tokenauthentry/add-token-auth-entry";

	public static void createToken(
			String userName, String password, Path cacheDirPath)
		throws Exception {

		URI uri = new URI(LIFERAY_TOKEN_URL);

		try (CloseableHttpClient closeableHttpClient = _getHttpClient(
				uri, userName, password)) {

			HttpPost httpPost = new HttpPost(uri);

			List<NameValuePair> params = new ArrayList<>();

			InetAddress localHost = InetAddress.getLocalHost();

			params.add(
				new BasicNameValuePair(
					"device",
					"portal-tools-bundle-support-" + localHost.getHostName()));

			httpPost.setEntity(new UrlEncodedFormEntity(params));

			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
				userName, password);

			BasicScheme basicScheme = new BasicScheme();

			httpPost.addHeader(basicScheme.authenticate(creds, httpPost, null));

			HttpResponse response = closeableHttpClient.execute(httpPost);

			StatusLine statusLine = response.getStatusLine();

			int statusCode = statusLine.getStatusCode();

			if (statusCode != 200) {
				throw new RuntimeException(
					"Failed : HTTP error code : " + statusCode);
			}

			HttpEntity httpEntity = response.getEntity();

			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(httpEntity.getContent()))) {

				File tokenFile = new File(cacheDirPath.toFile(), ".token");

				Files.createDirectories(cacheDirPath);

				StringBuilder sb = new StringBuilder();

				String line;

				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

				String output = sb.toString();

				int start = output.indexOf("token\":\"") + 8;

				int end = output.indexOf("\"", start);

				String token = output.substring(start, end);

				Files.write(tokenFile.toPath(), token.getBytes());
			}
		}
	}

	public static Path downloadFile(
			URI uri, String token, Path cacheDirPath, StreamLogger streamLogger)
		throws Exception {

		Path path;

		try (CloseableHttpClient closeableHttpClient = _getHttpClient(
				uri, token)) {

			path = _downloadFile(
				closeableHttpClient, uri, cacheDirPath, streamLogger);
		}

		return path;
	}

	public static Path downloadFile(
			URI uri, String userName, String password, Path cacheDirPath,
			StreamLogger streamLogger)
		throws Exception {

		Path path;

		try (CloseableHttpClient closeableHttpClient = _getHttpClient(
				uri, userName, password)) {

			path = _downloadFile(
				closeableHttpClient, uri, cacheDirPath, streamLogger);
		}

		return path;
	}

	private static void _checkResponseStatus(HttpResponse httpResponse)
		throws IOException {

		StatusLine statusLine = httpResponse.getStatusLine();

		if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
			throw new IOException(statusLine.getReasonPhrase());
		}
	}

	private static Path _downloadFile(
			CloseableHttpClient closeableHttpClient, URI uri, Path cacheDirPath,
			StreamLogger streamLogger)
		throws Exception {

		Path path;

		HttpHead httpHead = new HttpHead(uri);

		HttpContext httpContext = new BasicHttpContext();

		String fileName = null;

		Date lastModifiedDate;

		try (CloseableHttpResponse closeableHttpResponse =
				closeableHttpClient.execute(httpHead, httpContext)) {

			_checkResponseStatus(closeableHttpResponse);

			Header dispositionHeader = closeableHttpResponse.getFirstHeader(
				"Content-Disposition");

			if (dispositionHeader != null) {
				String dispositionValue = dispositionHeader.getValue();

				int index = dispositionValue.indexOf("filename=");

				if (index > 0) {
					fileName = dispositionValue.substring(
						index + 10, dispositionValue.length() - 1);
				}
			}
			else {
				RedirectLocations redirectLocations = (RedirectLocations)
					httpContext.getAttribute(
						HttpClientContext.REDIRECT_LOCATIONS);

				if (redirectLocations != null) {
					uri = redirectLocations.get(redirectLocations.size() - 1);
				}
			}

			Header lastModifiedHeader = closeableHttpResponse.getFirstHeader(
				HttpHeaders.LAST_MODIFIED);

			if (lastModifiedHeader != null) {
				lastModifiedDate = DateUtils.parseDate(
					lastModifiedHeader.getValue());
			}
			else {
				lastModifiedDate = new Date();
			}
		}

		if (fileName == null) {
			String uriPath = uri.getPath();

			fileName = uriPath.substring(uriPath.lastIndexOf('/') + 1);
		}

		if (cacheDirPath == null) {
			cacheDirPath = Files.createTempDirectory(null);
		}

		path = cacheDirPath.resolve(fileName);

		if (Files.exists(path)) {
			FileTime fileTime = Files.getLastModifiedTime(path);

			if (fileTime.toMillis() == lastModifiedDate.getTime()) {
				return path;
			}

			Files.delete(path);
		}

		Files.createDirectories(cacheDirPath);

		HttpGet httpGet = new HttpGet(uri);

		try (CloseableHttpResponse closeableHttpResponse =
				closeableHttpClient.execute(httpGet)) {

			_checkResponseStatus(closeableHttpResponse);

			HttpEntity httpEntity = closeableHttpResponse.getEntity();

			long length = httpEntity.getContentLength();

			streamLogger.onStarted();

			try (InputStream inputStream = httpEntity.getContent();
				OutputStream outputStream = Files.newOutputStream(path)) {

				byte[] buffer = new byte[10 * 1024];
				int completed = 0;
				int read = -1;

				while ((read = inputStream.read(buffer)) >= 0) {
					outputStream.write(buffer, 0, read);

					completed += read;

					streamLogger.onProgress(completed, length);
				}
			}
			finally {
				streamLogger.onCompleted();
			}
		}

		Files.setLastModifiedTime(
			path, FileTime.fromMillis(lastModifiedDate.getTime()));

		return path;
	}

	private static CloseableHttpClient _getHttpClient(URI uri, String token) {
		HttpClientBuilder httpClientBuilder = _getHttpClientBuilder(
			uri, null, null);

		Header header = new BasicHeader(
			HttpHeaders.AUTHORIZATION, "Bearer " + token);

		List<Header> headers = Lists.newArrayList(header);

		httpClientBuilder.setDefaultHeaders(headers);

		return httpClientBuilder.build();
	}

	private static CloseableHttpClient _getHttpClient(
		URI uri, String userName, String password) {

		HttpClientBuilder httpClientBuilder = _getHttpClientBuilder(
			uri, userName, password);

		return httpClientBuilder.build();
	}

	private static HttpClientBuilder _getHttpClientBuilder(
		URI uri, String userName, String password) {

		HttpClientBuilder httpClientBuilder = HttpClients.custom();

		CredentialsProvider credentialsProvider =
			new BasicCredentialsProvider();

		httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

		requestConfigBuilder.setCookieSpec(CookieSpecs.STANDARD);
		requestConfigBuilder.setRedirectsEnabled(true);

		httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());

		if ((userName != null) && (password != null)) {
			credentialsProvider.setCredentials(
				new AuthScope(uri.getHost(), uri.getPort()),
				new UsernamePasswordCredentials(userName, password));
		}

		String scheme = uri.getScheme();

		String proxyHost = System.getProperty(scheme + ".proxyHost");
		String proxyPort = System.getProperty(scheme + ".proxyPort");
		String proxyUser = System.getProperty(scheme + ".proxyUser");
		String proxyPassword = System.getProperty(scheme + ".proxyPassword");

		if ((proxyHost != null) && (proxyPort != null) && (proxyUser != null) &&
			(proxyPassword != null)) {

			credentialsProvider.setCredentials(
				new AuthScope(proxyHost, Integer.parseInt(proxyPort)),
				new UsernamePasswordCredentials(proxyUser, proxyPassword));
		}

		httpClientBuilder.useSystemProperties();

		return httpClientBuilder;
	}

}