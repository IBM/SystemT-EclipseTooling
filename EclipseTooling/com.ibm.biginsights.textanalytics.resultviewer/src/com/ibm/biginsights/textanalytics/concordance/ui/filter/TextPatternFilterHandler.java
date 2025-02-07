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

import static org.eclipse.jface.window.Window.OK;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel;
import com.ibm.biginsights.textanalytics.concordance.model.IConcordanceModel.StringFilterType;
import com.ibm.biginsights.textanalytics.concordance.model.IStringFilter;
import com.ibm.biginsights.textanalytics.concordance.model.impl.StringFilter;
import com.ibm.biginsights.textanalytics.concordance.ui.ConcordanceView;
import com.ibm.biginsights.textanalytics.concordance.ui.FilterCondition;
import com.ibm.biginsights.textanalytics.resultviewer.Messages;

public class TextPatternFilterHandler {



  private final StringFilterType filterType;

  private final String title;

  private final String message;

  public TextPatternFilterHandler(StringFilterType filterType, String dialogTitle, String message) {
    super();
    this.filterType = filterType;
    this.title = dialogTitle;
    this.message = message;
  }

  public void execute(FilterCondition fc) {
	  IWorkbenchPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();	  
	    if (part instanceof ConcordanceView) {
	        ConcordanceView concView = (ConcordanceView) part;
	        IConcordanceModel model = concView.getModel();
	        //IStringFilter current = model.getStringFilter(this.filterType);
	        IStringFilter current = fc.getStringFilter();
	        if(fc.getStringFilter() == null) {
	        	current = model.getStringFilter(this.filterType);
	        }else {
	        	current = fc.getStringFilter();
	        }
	        Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	        TextPatternInputDialog dialog = new TextPatternInputDialog(parentShell, this.title,
	            this.message, current.getString(), new RegexPatternInputValidator(current.isRegex()),
	            current.isRegex());
	        dialog.open();
	        if (dialog.getReturnCode() == OK) {
	          String value = dialog.getValue();
	          if (value.equals("") || value.equals("*")) { //$NON-NLS-1$ //$NON-NLS-2$
	            value = null;
	            fc.setFilterValue(Messages.FilterView_notconfigured);
	          }

	          model.setStringFilter(this.filterType, new StringFilter(dialog.isRegex(), value));
	          fc.setStringFilter(model.getStringFilter(this.filterType));
	          
	          fc.setFilterValue(value);
	          concView.updateFilters();
	          
	          fc.setFilterApplied(true);
	          concView.refresh();
		      concView.getFilterViewer().refresh();	      
		      concView.getFilterViewer().save();
	        }

	      }
  }
 
}
