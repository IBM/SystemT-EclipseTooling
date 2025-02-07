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

package com.ibm.biginsights.textanalytics.resourcechange.action;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

/**
 *  Krishnamurthy
 *
 */
public class ResourceChangeActionDelegate extends AbstractResourceChangeActionDelegate {



	public ResourceChangeActionDelegate(IResource removedResource, IResource addedResource) {
		super(removedResource, addedResource);
	}
	
	@Override
	public void run(){
		
		if(!validateResourceChangeEvent()){
			return;
		}
		
		if(isGoldStandardResourceChange()){
			new GoldStandardResourceChangeActionDelegate(removedResource, addedResource).run();
		}else if(isResultResourceChange()){
			new ResultResourceChangeActionDelegate(removedResource, addedResource).run();
		}else{
			new AQLResourceChangeActionDelegate(removedResource, addedResource).run();
		}
	}

	private boolean isGoldStandardResourceChange() {
		/**Presently, we are interested only in the following events:
		 * a) add (or) move of GS folder
		 * b) rename of GSParent folder
		 * Presently, we are NOT interested in rename & delete of other GS resources like .lc file or lc.prefs file
		 */
		if(addedResource instanceof IFolder || removedResource instanceof IFolder){
			return GoldStandardUtil.isGoldStandardFolder((IFolder)addedResource) 
					|| GoldStandardUtil.isGoldStandardParentDir((IFolder)removedResource);
		}
		return false;
	}
	
	private boolean isResultResourceChange(){
		/** Presently, we are interested only in the following events:
		 * a) add (or) move of Result folder
		 * b) rename of result root directory
		 */
		if(addedResource instanceof IFolder || removedResource instanceof IFolder){
			return ProjectUtils.isResultFolder((IFolder)addedResource)
				|| ProjectUtils.isResultRootDir((IFolder)removedResource);
		}
		return false;
	}

	@Override
	protected void handleRenameAction() {
		// dummy implementation
		
	}

	@Override
	protected void handleDeleteAction() {
		// dummy implementation
		
	}

	@Override
	protected void handleAddAction() {
		// dummy implementation
		
	}

}
