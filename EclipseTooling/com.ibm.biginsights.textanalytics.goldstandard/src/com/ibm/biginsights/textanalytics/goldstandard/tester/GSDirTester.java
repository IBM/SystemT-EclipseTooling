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
package com.ibm.biginsights.textanalytics.goldstandard.tester;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFolder;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;

/**
 * Tests whether a given folder is a GoldStandard folder or not
 * 
 *  Krishnamurthy
 *
 */
public class GSDirTester extends PropertyTester {



	public static final String IS_GOLDSTANDARD_DIR = "isGoldStandardDirectory";
	public static final String IS_GOLDSTANDARD_PARENT_DIR = "isGoldStandardParentDirectory";
	
	public GSDirTester() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if(receiver instanceof IFolder){
			if(IS_GOLDSTANDARD_DIR.equals(property)){
				return GoldStandardUtil.isGoldStandardFolder((IFolder)receiver);
			}else if(IS_GOLDSTANDARD_PARENT_DIR.equals(property)){
				return GoldStandardUtil.isGoldStandardParentDir((IFolder)receiver);
			}
		}
		return false;
	}

}
