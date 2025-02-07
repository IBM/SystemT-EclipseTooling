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
package com.ibm.biginsights.project;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

public class BigInsightsLibraryContainer implements IClasspathContainer {
    private final IClasspathEntry[] _entries;
	private final IPath _path;
	private String _description;
	
    public BigInsightsLibraryContainer(IPath path, IClasspathEntry[] entries, String description) {		
		this._path= path;
		this._entries= entries;
		this._description = description;
	}

	public IClasspathEntry[] getClasspathEntries() {
		return _entries;
	}

	public String getDescription() {
		return this._description;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return _path;
	}
	
}
