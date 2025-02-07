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
package com.ibm.biginsights.project.wizard;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jdt.ui.wizards.NewElementWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.BigInsightsLibraryContainerInitializer;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.util.BIConstants;

/**
 * BigInsightsLibraryContainerPage used in two places:
 * 1) From New BigInsights project wizard
 * 2) From project properties: Java build path, Edit on BigInsights library entry
 *
 */
public class BigInsightsLibraryContainerPage extends NewElementWizardPage implements
		IClasspathContainerPage, IClasspathContainerPageExtension {
	
	private Combo _cbLibrary;	
	private IJavaProject _javaProject;
	private String _initVersion = null;
	private String _containerName;
	
	public BigInsightsLibraryContainerPage() {
		super("BigInsightsLibraries");	 //$NON-NLS-1$
	    this.setTitle(Messages.BIGINSIGHTSLIBRARYCONTAINERPAGE_TITLE);	    
	    this.setDescription(Messages.BIGINSIGHTSLIBRARYCONTAINERPAGE_DESC);
	    this.setImageDescriptor(Activator.getImageDescriptor("/icons/wiz_bigInsight.gif")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
     * @see org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension#initialize(org.eclipse.jdt.core.IJavaProject, org.eclipse.jdt.core.IClasspathEntry[])
     */
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
    	_javaProject = project;
    }
    
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.FILL);
		composite.setFont(parent.getFont());
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, "com.ibm.biginsights.project.help.create_ bigi_proj"); //$NON-NLS-1$
		// 2 column layout, columns have different width
		composite.setLayout(new GridLayout(2,false));
		
		Label lblVersion = new Label(composite, SWT.NONE);
		lblVersion.setFont(composite.getFont());
		lblVersion.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblVersion.setText(Messages.BIGINSIGHTSLIBRARYCONTAINERPAGE_VERSION_LABEL);
		
		_cbLibrary = new Combo(composite, SWT.READ_ONLY);
		_cbLibrary.setFont(composite.getFont());		
		_cbLibrary.setItems(BigInsightsLibraryContainerInitializer.getInstance().getSupportedContainers());	
		
		GridData gdCombo = new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1);
		gdCombo.widthHint = SWT.DEFAULT;
		gdCombo.widthHint = 300;
		_cbLibrary.setLayoutData(gdCombo);
				
		_cbLibrary.addModifyListener(new ModifyListener() {			
			
			@Override
			public void modifyText(ModifyEvent e) {
				changeLibrarySelection();				
			}			
		});
		
		
		setControl(composite);

		// select the right library initially
		if (_initVersion!=null) { // when launched from Edit in project libraries, select current library
			String versionToCompare = _initVersion;
			if (BIConstants.CONTAINER_ID_DEFAULT.equals(versionToCompare)) {
				versionToCompare = "V"+BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerVersion(); //$NON-NLS-1$
			}
        	// pre-populate the drop-down for the library container
    		int selected = 0;
    		for (int i=0; i<_cbLibrary.getItemCount(); i++) {
    			String libName = _cbLibrary.getItem(i);
    			if (libName.contains(versionToCompare))	{
    				selected = i;
    				break;
    			}
    		}
    		_cbLibrary.select(selected);        	
		}
		else { // when launched from New BI project wizard select default container
			String defaultContainer = BigInsightsLibraryContainerInitializer.getInstance().getDefaultBigInsightsContainerEntryName();
			if (defaultContainer!=null) {
				int defaultIndex = Math.max(0,_cbLibrary.indexOf(defaultContainer)); // avoid setting it to -1
				_cbLibrary.select(defaultIndex); // select the default container as first entry
			}
		}

	}
	
	protected void changeLibrarySelection() {				
		if (_cbLibrary!=null) { 		
			_containerName = _cbLibrary.getItem(_cbLibrary.getSelectionIndex());			
		}						
	}	
	
	@Override
	public boolean finish() {
		return true;		
	}

	@Override
	public IClasspathEntry getSelection() {	
		return BigInsightsLibraryContainerInitializer.getInstance().getClasspathEntryByName(_containerName);
	}
	
	public String getContainerName()
	{
		return this._containerName;
	}

	@Override
	public void setSelection(IClasspathEntry containerEntry) {
		// method only called from Edit in project properties to change the BI properties from there
        if(containerEntry != null) {
        	_initVersion = containerEntry.getPath().segment(1);
        }        
	}

	 public boolean isPageComplete() {
	        return _containerName!=null && !_containerName.isEmpty();
	    }
}
