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

import java.util.Arrays;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.FilteredList;

import com.ibm.biginsights.textanalytics.resultviewer.Messages;

/**
 * 
 * TextSelectionDialog is for filtered selection of values
 *
 */
public class FilterSelectionDialog extends ElementListSelectionDialog {



	List selList;

	String[] result;
	
	Button addButton, removeButton;
	
	public FilterSelectionDialog(Shell shell, ILabelProvider label) {
		super(shell, label);
	}
	
	@Override
	public void create() {
		super.create();

		//populate list box with values
		for (String str : result) {
			selList.add(str);
		}
		//disable add button when items is empty
		if(selList == null || this.fFilteredList != null && this.fFilteredList.isEmpty()) {
			addButton.setEnabled(false);
		}

		//disable remove button when result is empty
		if(result == null || result.length == 0) {
			removeButton.setEnabled(false);
		}
		
	}

	@Override
	public Object[] getResult() {
		return result;
	}
	
	/**
	 * Handles default selection (double click). 
	 * Do nothing on double click.
	 */
	@Override
	protected void handleDefaultSelected ()
	{
	  //Do nothing. By default it would call okPressed()
	  //Avoiding that, as we don't want double clicking on 
	  //selection list item to activate the ok button.
	}
	
	@Override
	protected void okPressed() {
		result = selList.getItems();
		super.okPressed();
	}
	
	public void setResult(String[] values) {
		result = values;
	}
	
	/**
	 * Override creatButtonBar to add list box and add, remove button
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		
		final FilteredList f = this.fFilteredList;
		
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		GridLayout gl = new GridLayout(1, false);
		gl.horizontalSpacing = 5;
		gl.marginLeft = 5;
		gl.marginRight = 5;
		
		c.setLayout(gl);
		
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		gd.widthHint = 75;
		gd.heightHint = 25;
		addButton = new Button(c, SWT.NONE);
		addButton.setLayoutData(gd);
		addButton.setText(Messages.addButtonLabel);
		
		addButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] obj = f.getSelection();
				
				java.util.List<String> lst = Arrays.asList(selList.getItems());
				
				for (Object o : obj) {
					String s = (String) o;
					if(lst != null && !lst.contains(s)) {
						selList.add(s);
					}
				}
				if(!removeButton.isEnabled()) {
					removeButton.setEnabled(true);
				}
				
			}
		});

		Label selLabel = new Label(c, SWT.NONE);
		selLabel.setText(Messages.filterSelectionLabel);
		
		selList = new List(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint=100;
		gd.widthHint=100;
		selList.setLayoutData(gd);
		
		gd = new GridData();
		gd.horizontalAlignment = GridData.END;
		gd.widthHint = 75;
		gd.heightHint = 25;
		
		removeButton = new Button(c, SWT.NONE);
		
		removeButton.setLayoutData(gd);
		removeButton.setText(Messages.removeButtonLabel);
		
		removeButton.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				//remove the selected items for the list box
				selList.remove(selList.getSelectionIndices());
				//set the first item as selected
				if(selList.getItemCount()>0) {
					selList.select(0);
				}
			}
		});
		
		return super.createButtonBar(parent);
	}
}
