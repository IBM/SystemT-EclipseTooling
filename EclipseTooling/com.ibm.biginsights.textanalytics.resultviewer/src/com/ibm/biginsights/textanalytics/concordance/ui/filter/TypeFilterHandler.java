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
package com.ibm.biginsights.textanalytics.concordance.ui.filter;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.ITypes;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.concordance.ui.FilterCondition;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * Handler for the type filter.
 */
public class TypeFilterHandler {



  public void execute(FilterCondition fc) {
	  IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
	
	    if (part instanceof ConcordanceView) {
	        ConcordanceView concView = (ConcordanceView) part;
	        IConcordanceModel model = concView.getModel();
	        ITypes types = model.getTypes();
	        Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	        
	        FilterSelectionDialog dialog = new FilterSelectionDialog(parentShell, new LabelProvider());
	        dialog.setElements(types.getTypes().toArray(new String[]{}));
	        dialog.setMultipleSelection(true);

	        // Create the dialog with label and content provider.  Note: the content provider must contain
	        // the model at this point, otherwise the dialog comes up with the types grayed out and the
	        // message that no selection is available.  This seems like a bug in the dialog I'm using, or
	        // I didn't understand how this is supposed to work.
//	        CheckedTreeSelectionDialog dialog = new CheckedTreeSelectionDialog(parentShell,
//	            new FilterLabelProvider(), new TypeFilterContentProvider(types));
	        // Set the input.  This is needed in addition to the content provider.  See comment above.
//	        dialog.setInput(types);
	        // Set the types that are currently active.
	        if(fc.getActiveTypes() == null) {
	        	dialog.setInitialSelections(new String[] {});
	        	 dialog.setResult(new String[] {});
	        }else {
	        	dialog.setInitialSelections(fc.getActiveTypes());
	        	 dialog.setResult(fc.getActiveTypes());
	        }
	        dialog.setTitle(Messages.spanAttributeTitle);
	        dialog.setMessage(Messages.spanAttributeMessage);
	        dialog.setBlockOnOpen(true);
	        dialog.open();
	        // Once the user has closed the dialog, check the results.  If the user has cancelled the
	        // dialog, the result is null.
	        Object[] selectedTypes = dialog.getResult();
	        // Bug in CheckedTreeSelectionDialog: when the user cancels by pressing the escape key 
	        // (instead of clicking the cancel button), the result is not null, but an empty array.  So
	        // in addition to checking the result, we also need to check the dialog return code.
	        if (selectedTypes != null && (dialog.getReturnCode() == Window.OK)) {
	          String[] selected = new String[selectedTypes.length];
	          StringBuilder builder = new StringBuilder();
	          for (int i = 0; i < selected.length-1; i++) {
	            selected[i] = (String) selectedTypes[i];
	            if(i<4) {
	            	builder.append((String)selectedTypes[i]+", ");
	            }
	          }
	          
	          if(selected.length>0){
	        	  selected[selected.length-1] = (String) selectedTypes[selected.length-1];
	        	  if(selected.length > 5) {
	        		  builder.append((String)selectedTypes[4]+" ...");
	        	  }else {
	        		  builder.append((String)selectedTypes[selected.length-1]+"");
	        	  }	        	  
	        	  fc.setFilterValue(builder.toString());
		          fc.setActiveTypes(selected);
		          types.setActiveTypes(selected);
	          }else {
	        	  fc.setFilterValue(Messages.FilterView_notconfigured);
	        	  fc.setActiveTypes(selected);
	        	  fc.setConfigured(false);
	          }
	          
	          
	          fc.setFilterApplied(true);
	          concView.refresh();
	          concView.getFilterViewer().refresh();	  
	          concView.getFilterViewer().save();
	        }

	      }
	  
  }
  
}
