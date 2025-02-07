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

package com.ibm.biginsights.textanalytics.resultdifferences.colldiff;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;

/**
 * 
 * singleton that contains the folders that are compared inside the analysis
 * differences view.
 * 
 *  Rueck
 * 
 */
public class CollDiffModel {



	private static CollDiffModel instance;


	private IFolder rightFolder;
	
	public boolean isComparisonAgainstGS() {
		return isComparisonAgainstGS;
	}

	public void setComparisonAgainstGS(boolean isComparisonAgainstGS) {
		this.isComparisonAgainstGS = isComparisonAgainstGS;
	}

	private boolean isComparisonAgainstGS;

	/**
	 * the folder which is set on on the top left of the view. it can not be
	 * changed. this information is necessary for the view to show the correct
	 * folder on the left side because this folder can be the old or the new
	 * folder.
	 */
	private IFolder leftFolder;

	private IProject project;

	private CollDiffModel(IFolder rightFolder,
			IFolder leftFolder) {
		this.rightFolder = rightFolder;
		
		// set the project
		this.leftFolder = leftFolder;
		this.project = leftFolder.getProject();
	}

	public static CollDiffModel getInstance(IFolder rightFolder,
			IFolder leftFolder) {
		if (instance == null) {
			instance = new CollDiffModel(rightFolder, leftFolder);
		} else {
			CollDiffModel tempModel = new CollDiffModel(rightFolder, leftFolder);
			if (!tempModel.equals(instance)) {
				instance = tempModel;
			}
		}
		return instance;
	}

	public static CollDiffModel getInstance() {
		return instance;
	}

	/**
	 * @return the folder which is set in the combobox on the left
	 */
	public IFolder getLeftFolder() {
		return leftFolder;
	}

	public String getLeftFolderName() {
		return leftFolder.getName();
	}

	/** get the folder which is set in the combobox on the right */
	public IFolder getRightFolder() {
		return rightFolder;
	}

	public String getRightFolderName() {
		return getRightFolder().getName();
	}

	public IProject getProject() {
		return project;
	}

	@Override
	public boolean equals(Object object) {

		if (object instanceof CollDiffModel) {

			CollDiffModel model = (CollDiffModel) object;
			return this.rightFolder.equals(model.rightFolder)
					&& this.leftFolder.equals(model.leftFolder);

		}

		return false;
	}
	
	public static boolean isLeftFile(IFile file)
	{
		return getInstance().getLeftFolderName().equals(file.getParent().getName());
	}
}
