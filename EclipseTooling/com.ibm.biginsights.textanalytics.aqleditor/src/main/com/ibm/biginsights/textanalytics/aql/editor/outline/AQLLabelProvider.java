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
package com.ibm.biginsights.textanalytics.aql.editor.outline;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;

/**
 *  
 *  Babbar
 * 
 */
public class AQLLabelProvider extends LabelProvider {



	@Override public Image getImage(Object element) {
		if(element instanceof AQLNode){
			return Activator.getDefault().getImageRegistry().get(((AQLNode)element).getImage());
		}
		return null;
	}

}
