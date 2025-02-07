/*******************************************************************************
* Copyright IBM
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/

package com.ibm.biginsights.textanalytics.nature.utils;

import java.io.File;

import com.ibm.avatar.algebra.util.file.FileUtils;

public class FileDirectoryPickerUtil
{
	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+          //$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp."; //$NON-NLS-1$

	/**
	 * Gets the Absolute path. This is used to get the absolute path from
	 * FileDirectoryPicker
	 * 
	 * @param inputPath
	 * @return absolute Path
	 */
	public static String getPath(String inputPath) {

		if (inputPath != null && inputPath.indexOf("[W]") != -1) {//$NON-NLS-1$
			// inputCollectionValue starts with [W] followed by path.
			// [W]refers to the current workspace.
			// We take only the substring and check whether the file exist.
			String subStr = inputPath.substring(3);
			File file = FileUtils.createValidatedFile(subStr);
			// If file exist then its a valid path
			if (file.exists()) {
				return subStr;
			} else {
				// If file not exist, then the path may be relative to
				// workspace.
				// We create absolute path for inputCollectionValue
				String workspacePath = ProjectPreferencesUtil
						.getAbsolutePath(inputPath);
				if (workspacePath == null) {
					return inputPath;
				} else {
					// Check the path exist with the
					File workspaceFile = FileUtils.createValidatedFile(workspacePath);
					if (!workspaceFile.exists()) {
						return workspacePath;

					}
				}

			}
		} else if (inputPath != null && !inputPath.isEmpty()) {
			// if the path is external file system, then [W] will not be there.
			if(inputPath.startsWith(":") || inputPath.startsWith("."))
				return inputPath;
			File file;
			//Adding a file separator to check Absolute files
			if(!inputPath.endsWith(File.separator)) {
				file = FileUtils.createValidatedFile(inputPath.concat (File.separator));
			} else {
			  file = FileUtils.createValidatedFile(inputPath);
			}
			
			// Checking if it is an Absolute path and checking if they are valid.
			if(file.isAbsolute() && !file.exists()) {
				return inputPath;
			}
			// If file exist then its a valid path
			if (file.exists()) {
				return inputPath;
			} else {
				// If file not exist, then the path may be relative to
				// workspace.
				// We create absolute path for inputCollectionValue
				String workspacePath = ProjectPreferencesUtil
						.getAbsolutePath("[W]" + inputPath);
				if (workspacePath != null) {
					File workspaceFile = FileUtils.createValidatedFile(workspacePath);
					if (workspaceFile.exists()) {
						return workspacePath;

					}
				}
			}

		}
		return inputPath;
	}

	/**
	 * Check whether the path is valid. This checks whether the path provided by
	 * FileDirectoryPicker is valid.
	 * 
	 * @return
	 */
	public static boolean isPathValid(String inputCollectionValue) {
		if (inputCollectionValue != null
				&& inputCollectionValue.indexOf("[W]") != -1) {//$NON-NLS-1$
			// inputCollectionValue starts with [W] followed by path.
			// [W]refers to the current workspace.
			// We take only the substring and check whether the file exist.
			String subStr = inputCollectionValue.substring(3);
			File file = FileUtils.createValidatedFile(subStr);
			// If file exist then its a valid path
			if (!file.exists()) {
				// If file not exist, then the path may be relative to
				// workspace.
				// We create absolute path for inputCollectionValue
				String workspacePath = ProjectPreferencesUtil
						.getAbsolutePath(inputCollectionValue);
				if (workspacePath == null) {
					return false;
				} else {
					// Check the path exist with the
					File workspaceFile = FileUtils.createValidatedFile(workspacePath);
					if (!workspaceFile.exists()) {
						return false;

					}
				}

			}
		} else if (inputCollectionValue != null) {
			// if the path is external file system, then [W] will not be there.
			if(inputCollectionValue.startsWith(".") || inputCollectionValue.startsWith(":")) {
				return false;
			}
			if(!inputCollectionValue.endsWith(File.separator))
				inputCollectionValue = inputCollectionValue.concat(File.separator);
			File file = FileUtils.createValidatedFile(inputCollectionValue);
			// Checking if it is an Absolute path and checking if they are valid.
			if(file.isAbsolute() && !file.exists()) {
				return false;
			}
			// If file exist then its a valid path
			if (file.exists()) {
				return true;
			}

			// If file not exist, then the path may be relative to workspace.
			// We create absolute path for inputCollectionValue
			String workspacePath = ProjectPreferencesUtil.getAbsolutePath("[W]"
					+ inputCollectionValue);
			if (workspacePath == null) {
				return false;
			} else {
				// Check the path exist with the
				File workspaceFile = FileUtils.createValidatedFile(workspacePath);
				if (!workspaceFile.exists()) {
					return false;

				}
			}

		}
		return true;
	}

}
