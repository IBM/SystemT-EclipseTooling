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
package com.ibm.biginsights.project.locations;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

import com.ibm.biginsights.project.locations.actions.OpenConfFileAction;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppCategoryFolder;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppFolder;
import com.ibm.biginsights.project.locations.apps.BigInsightsAppTypeFolder;
import com.ibm.biginsights.project.util.BIConstants;


public class BILocationContentProvider implements ITreeContentProvider {

	public BigInsightsLocationRoot treeRoot = new BigInsightsLocationRoot(this);
	
	// The viewer that displays this Tree content	
	private Viewer viewer;
	private StructuredViewer sviewer;
	  
	public BILocationContentProvider()
	{
		super();
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return new Object[] { treeRoot };		
	}

	@Override
	public void dispose() {

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	   this.viewer = viewer;	   
	   if ((viewer != null) && (viewer instanceof StructuredViewer)) {
		   this.sviewer = (StructuredViewer) viewer;
		   this.sviewer.addDoubleClickListener(new IDoubleClickListener() {
				public void doubleClick(DoubleClickEvent event) {
					if (event.getSelection() instanceof TreeSelection) {
						TreeSelection sel = (TreeSelection)event.getSelection();						
						if (sel.getFirstElement() instanceof File && ((File)sel.getFirstElement()).isFile()) {
							// double-click on the conf file opens the xml file in the editor
							OpenConfFileAction.openFile(null, (File)sel.getFirstElement());
						}
					}
				}
			});		  
	   }
	   else
		   this.sviewer = null;
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof BigInsightsLocationRoot ||
			parentElement instanceof BigInsightsLocation ||
			parentElement instanceof BigInsightsAppFolder ||
			parentElement instanceof BigInsightsAppCategoryFolder ||
			parentElement instanceof BigInsightsAppTypeFolder ||
			parentElement instanceof BigInsightsConfFolder)
		{
			IBigInsightsLocationNode locationContent = (IBigInsightsLocationNode)parentElement;
			locationContent.setContentProvider(this);
			return locationContent.getChildren();
		}
		else if (parentElement instanceof File) {
			return filterFiles(((File)parentElement).listFiles());
		}
	    return null;
	}
	
	private Object[] filterFiles(Object[] files) {
		Object[] result = null;
		if (files!=null) {
			ArrayList<Object>tempArray = new ArrayList<Object>();		
			for (Object o:files) {
				File file = (File)o;
				if (file.isFile() && file.getName().endsWith(BIConstants.LOCATION_XML)) {
					// don't add location.xml 
				}
				else 
					tempArray.add(o);
			}
			result = tempArray.toArray();
		}
		return result;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof IBigInsightsLocation)
			return treeRoot;
		else if (element instanceof File) {
			return ((File)element).getParentFile();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof IBigInsightsLocationNode)
		{
			IBigInsightsLocationNode locationContent = (IBigInsightsLocationNode)element;
			return locationContent.hasChildren();
		}
		else if (element instanceof File) {
			// check if there are more files under the folder
			Object[] files = getChildren(element);
			return files!=null ? files.length>0 : false;
		}
		return false;
	}

	public void refresh(final IBigInsightsLocationNode content) {
		if (this.sviewer != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					BILocationContentProvider.this.sviewer.refresh(content);
				}
			});

		} else {
			refresh();
		}
	}
		  
	public void refresh() {
		if (this.viewer == null)
			return;

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				BILocationContentProvider.this.viewer.refresh();
			}
		});
	}
}
