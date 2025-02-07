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
package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import org.eclipse.core.resources.IFolder;


public class FileDifferencesModel {


	
	private String[] rightFiles;
	public String[] getRightFilePaths() {
		return rightFiles;
	}

	public String[] getLeftFilePaths() {
		return leftFilePaths;
	}

	private String[] leftFilePaths;
	
	public FileDifferencesModel(String[] pRightFiles, String[] pleftFilePaths, IFolder pLeftFolder, IFolder pRightFolder)
	{
		rightFiles = pRightFiles;
		leftFilePaths = pleftFilePaths;
		
		rightFolder=pRightFolder;
		leftFolder = pLeftFolder;
	}
	
	
	private IFolder rightFolder;
	public IFolder getRightFolder() {
		return rightFolder;
	}

	public void setRightFolder(IFolder rightFolder) {
		this.rightFolder = rightFolder;
	}

	public IFolder getLeftFolder() {
		return leftFolder;
	}

	public void setLeftFolder(IFolder leftFolder) {
		this.leftFolder = leftFolder;
	}

	private IFolder leftFolder;
  private String selectedType;
  public void setType (String selectedType)
  {
   this.selectedType = selectedType;
    
  }
	
  public String getType ()
  {
   return this.selectedType;
    
  }
}
