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
package com.ibm.biginsights.textanalytics.treeview.control;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.ibm.biginsights.textanalytics.treeview.model.ITreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.AbstractTreeObject;
import com.ibm.biginsights.textanalytics.treeview.model.impl.TreeParent;

/**
 * This class is used to set the render the content of the TreeView
 *  Madiraju
 *
 */
public class TreeViewContentProvider implements IStructuredContentProvider, ITreeContentProvider 
{


	
		public void inputChanged(Viewer v, Object oldInput, Object newInput) 
		{
		}
		
		public void dispose() 
		{
		}
		
		public Object[] getElements(Object parent) 
		{
			return getChildren(parent);
		}

		public Object getParent(Object child) 
		{
			if (child instanceof AbstractTreeObject) 
			{
				return ((AbstractTreeObject)child).getParent();
			}
			return null;
		}
		
		public ITreeObject[] getChildren(Object parent) {
			if (parent instanceof TreeParent) 
			{
				return ((TreeParent)parent).getChildren();
			}
			return new ITreeObject[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
			return ((TreeParent)parent).hasChildren();
			return false;
		}
}

