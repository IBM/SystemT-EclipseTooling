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
package com.ibm.biginsights.textanalytics.resultdifferences.ui;

import org.eclipse.core.resources.IFile;

/**
 * Wrapper around a type name
 * 
 * 
 *
 */

public class TypeContainer {


	
	/** type name */
	private final String fName;
	
	private IFile[] leftFileList;
	public IFile[] getLeftFileList() {
		return leftFileList;
	}

	public void setLeftFileList(IFile[] leftFileList) {
		this.leftFileList = leftFileList;
	}

	private IFile[] rightFleList;
	
	public IFile[] getRightFleList() {
		return rightFleList;
	}

	public void setRightFleList(IFile[] rightFleList) {
		this.rightFleList = rightFleList;
	}

	/**
	 * Constructor
	 * @param name The type name to store
	 */
	public TypeContainer(final String name) {
		fName = name;
	}
	
	/**
	 * Getter for the type name
	 * @return The type name
	 */
	public String getName() {
		return fName;
	}
	
	public String toString(){
		return fName;
	}
	

}
