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

package com.ibm.biginsights.textanalytics.nature.prefs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class PaginationPrefPanel extends PrefPageAdapter {



	protected Button cbPagination;
	protected Text numField;
	protected Label lblPageNos;
	protected String errorMessage;
	
	public PaginationPrefPanel(Composite parent, SystemTProjectPreferences projectPreferences) {
		super(projectPreferences);
		Group paginationPanel = new Group(parent, SWT.NONE);
		paginationPanel.setText(Messages.getString("PaginationPrefPanel.HEADING")); //$NON-NLS-1$
		GridLayout layout = new GridLayout(3,false);
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		paginationPanel.setLayout(layout);
		paginationPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label lblEnablePagination = new Label(paginationPanel,SWT.NONE);
    lblEnablePagination.setText(Messages.getString("PaginationPrefPanel.CHECKBOX_LABEL")); //$NON-NLS-1$
    
		cbPagination = new Button(paginationPanel, SWT.CHECK);
		cbPagination.setText(""); //$NON-NLS-1$
		cbPagination.setEnabled(true);
		cbPagination.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (cbPagination.getSelection()) {
					lblPageNos.setEnabled(true);
					numField.setEnabled(true);
				} else {
					lblPageNos.setEnabled(false);
					numField.setEnabled(false);
				}
			}
		});
		
		cbPagination.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
	    public void getName (AccessibleEvent e) {
	      e.result = Messages.getString("PaginationPrefPanel.CHECKBOX_LABEL");
	    }
	  });
		
		lblPageNos = new Label(paginationPanel,SWT.None);
		lblPageNos.setText(Messages.getString("PaginationPrefPanel.TEXTFIELD_LABEL")); //$NON-NLS-1$
		lblPageNos.setToolTipText(Messages.getString("PaginationPrefPanel.TEXTFIELD_TOOLTIP")); //$NON-NLS-1$
		lblPageNos.setEnabled(false);
		GridData gd1 = new GridData(SWT.FILL,SWT.CENTER,false,false,2,1);
		lblPageNos.setLayoutData(gd1);
		
		numField = new Text(paginationPanel,SWT.BORDER|SWT.SINGLE);
		numField.setTextLimit(4);
		numField.setLayoutData(new GridData(25,17));
		numField.setEnabled(false);
		
		restoreDefaults();
	}
	@Override
	public void restoreDefaults() {
		boolean paginationEnabled = preferenceStore.getDefaultBoolean(Constants.PAGINATION_ENABLED);
		int numFilesPerPage = preferenceStore.getDefaultInt(Constants.PAGINATION_FILES_PER_PAGE);
		
		if (numFilesPerPage <= 0) {
			numFilesPerPage = Constants.PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE;
		}
		numField.setText(String.valueOf(numFilesPerPage));
		
		cbPagination.setSelection(paginationEnabled);
		lblPageNos.setEnabled(paginationEnabled);
		numField.setEnabled(paginationEnabled);

	}

	@Override
	public void apply() {
		setValue(Constants.PAGINATION_ENABLED,String.valueOf(cbPagination.getSelection()));
		projectPreferences.getProjectProperties().setPaginationEnabled(preferenceStore.getBoolean(Constants.PAGINATION_ENABLED));
		
		setValue(Constants.PAGINATION_FILES_PER_PAGE,numField.getText());
		projectPreferences.getProjectProperties().setNumFilesPerPage(preferenceStore.getInt(Constants.PAGINATION_FILES_PER_PAGE));
	}
	
	public boolean isValid(){
		try {
			int value = Integer.parseInt(numField.getText());
			if (value > 0) {
				setErrorMessage(null);
				return true;
			} else {
				setErrorMessage(Messages.getString("PaginationPrefPanel.ERR_PAGINATION_VALUE_MUST_BE_NUMBER_GREATER_THAN_ZERO")); //$NON-NLS-1$
				return false;
			}
		} catch (NumberFormatException e) {
			setErrorMessage(Messages.getString("PaginationPrefPanel.ERR_PAGINATION_VALUE_MUST_BE_NUMBER_GREATER_THAN_ZERO")); //$NON-NLS-1$
			return false;
		}
	}

	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		boolean paginationEnabled = properties.isPaginationEnabled();
		int numFilesPerPage = properties.getNumFilesPerPage();
		
		if (numFilesPerPage <= 0) {
			numFilesPerPage = Constants.PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE;
		}
		numField.setText(String.valueOf(numFilesPerPage));
		
		cbPagination.setSelection(paginationEnabled);
		lblPageNos.setEnabled(paginationEnabled);
		numField.setEnabled(paginationEnabled);
	}
	
	protected void setErrorMessage(String msg) {
		this.errorMessage = msg;
	}
	
	public String getErrorMessage() {
		return this.errorMessage;
	}

}
