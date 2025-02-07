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
 
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
 
public class AlphabeticalSorter extends ViewerSorter {


 
    
	private String prjName;


	public AlphabeticalSorter(String projectName)
	{
		this.prjName = projectName;
	}
	
    public int category(Object element) {
        return 1;
        
    }
 
    
   public int compare(Viewer viewer, Object e1, Object e2) {
	   
	   
        int cat1 = category(e1);
        int cat2 = category(e2);
        if (cat1 != cat2)
            return cat1 - cat2;
        String name1, name2;
        if (viewer == null || !(viewer instanceof ContentViewer)) {
            name1 = e1.toString();
            name2 = e2.toString();
        } else {
            IBaseLabelProvider prov = ((TreeViewer)viewer).getLabelProvider();
            if (prov instanceof ILabelProvider) {
                ILabelProvider lprov = (ILabelProvider)prov;
                name1 = lprov.getText(e1);
                name2 = lprov.getText(e2);
            } else {
                name1 = e1.toString();
                name2 = e2.toString();
            }
        }
       
        if (name1 == null || name1.equals("import declarations") || name1.equals(prjName))
            name1 = ""; //$NON-NLS-1$
        if (name2 == null || name2.equals("import declarations") || name2.equals(prjName))
            name2 = ""; //$NON-NLS-1$
        
        return getComparator().compare(name1, name2);
    }
}
