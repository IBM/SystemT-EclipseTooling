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
package com.ibm.biginsights.textanalytics.workflow.plan.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;

import com.ibm.biginsights.textanalytics.workflow.plan.ActionPlanView;
import com.ibm.biginsights.textanalytics.workflow.plan.models.TreeObject;
import com.ibm.biginsights.textanalytics.workflow.util.AqlProjectUtils;

public class DragListener implements DragSourceListener {



	TreeViewer viewer;

	public DragListener(TreeViewer viewer) {
		this.viewer = viewer;
	}

	@Override
	public void dragStart(DragSourceEvent event) {

	}

	@Override
	public void dragSetData(DragSourceEvent event) {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		ActionPlanView plan = AqlProjectUtils.getActionPlanView ();
		plan.setDraggedObjects (null);

		// Get an array of selected objects
		TreeObject[] selectedObjects = new TreeObject[selection.size ()];
		for (int i = 0; i < selection.size (); i++) {
		  selectedObjects[i] = (TreeObject)selection.toArray ()[i];
		}
		plan.setDraggedObjects (selectedObjects);

		if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = selectedObjects;
		}
		if (ActionPLanTransfer.getInstance().isSupportedType(event.dataType)) {
			event.data = selectedObjects;
		}
	}

	@Override
	public void dragFinished(DragSourceEvent event) {

	}

}
