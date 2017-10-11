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

package com.liferay.jenkins.results.parser;

import java.io.File;

/**
 * @author Michael Hashimoto
 */
public class DataArchiveBranch {

	public DataArchiveBranch(
		File generatedDataArchiveDirectory,
		GitWorkingDirectory portalLegacyGitWorkingDirectory) {

		_generatedDataArchiveDirectory = generatedDataArchiveDirectory;
		_portalLegacyGitWorkingDirectory = portalLegacyGitWorkingDirectory;

		GitWorkingDirectory.Branch upstreamBranch =
			_portalLegacyGitWorkingDirectory.getUpstreamBranch();

		_portalLegacyGitWorkingDirectory.checkoutBranch(upstreamBranch);

		_portalLegacyGitWorkingDirectory.reset("--hard");

		_portalLegacyGitWorkingDirectory.clean();
	}

	public File getGeneratedDataArchiveDirectory() {
		return _generatedDataArchiveDirectory;
	}

	public File getPortalLegacyWorkingDirectory() {
		return _portalLegacyGitWorkingDirectory.getWorkingDirectory();
	}

	private final File _generatedDataArchiveDirectory;
	private final GitWorkingDirectory _portalLegacyGitWorkingDirectory;

}