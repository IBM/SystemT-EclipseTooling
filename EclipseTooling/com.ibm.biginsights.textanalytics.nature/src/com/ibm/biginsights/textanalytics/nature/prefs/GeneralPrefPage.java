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
/**
 * 
 */
package com.ibm.biginsights.textanalytics.nature.prefs;

import java.beans.PropertyChangeListener;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.utils.FileDirectoryPickerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.preferences.PreferencesPlugin;
import com.ibm.biginsights.textanalytics.preferences.TextAnalyticsWorkspacePreferences;
import com.ibm.biginsights.textanalytics.util.FileDirectoryPicker;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * GeneralPrefPage provides the UI for 'General' tab of SystemT project preferences
 * 
 * 
 *
 */
public class GeneralPrefPage extends PrefPageAdapter{

	@SuppressWarnings("unused")


	protected Composite topLevel;
	
	protected Button cbProvenance;
	protected FileDirectoryPicker mainAQLPicker;
	protected SearchPathPrefPage searchPathPrefPage;
	protected PaginationPrefPanel paginationPrefPanel;
	protected Button cbPagination;
	protected String errorMessage = null;
	
	public GeneralPrefPage(Composite parent, SystemTProjectPreferences projectPreferences){
		super(projectPreferences);
		
		topLevel = new Composite(parent, SWT.NONE);
		GridLayout gLayout = new GridLayout(1, true);
		gLayout.marginLeft = 0;
		topLevel.setLayout(gLayout);
		GridData gridData = new GridData(GridData.BEGINNING, GridData.CENTER, false, false);
		topLevel.setLayoutData(gridData);

		IProject proj = ProjectUtils.getSelectedProject ();
		if (proj != null && ProjectPreferencesUtil.isMigrationRequiredForProjectPropertiesToModular (proj)) {
		  createMigratePropertiesToModularAQLProjectButton ();
		}
		createEnableProvenancePanel();
		createMainAQLPanel();
		createSearchPathPanel();
		createCustomPanel();
		
		createPaginationPrefPanel();

		restoreDefaults();
	}
	
	public Control getControl(){
		return topLevel;
	}
	
	protected void createPaginationPrefPanel() {
		paginationPrefPanel = new PaginationPrefPanel(topLevel,projectPreferences);
		
	}
	
	protected void createEnableProvenancePanel(){
		if(projectPreferences.consumer == Constants.CONSUMER_PROPERTY_SHEET
				|| projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG){
			//Row 1: Language choice
			Composite enableProvenancePanel = new Composite(topLevel, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			enableProvenancePanel.setLayout(layout);
			enableProvenancePanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			
			if(isEnableProvenanceVisible()){
				//Row 1.b: Provenance
				Label lbProvenance = new Label(enableProvenancePanel, SWT.NONE);
				lbProvenance.setText(Messages.getString("GeneralPrefPage.ENABLE_PROVENANCE")); //$NON-NLS-1$
				cbProvenance = new Button(enableProvenancePanel, SWT.CHECK);
				cbProvenance.setText(""); //$NON-NLS-1$
				cbProvenance.setEnabled(true);
				if(projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG){
          cbProvenance.setEnabled(false);
        }   
				
				cbProvenance.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
				  @Override
				  public void getName (AccessibleEvent e)
				  {
            e.result = Messages.getString("GeneralPrefPage.ENABLE_PROVENANCE");
				  }
		    });
				
			}
		}
	}
	
	protected void createMigratePropertiesToModularAQLProjectButton() {
	  Group migratePropertiesPanel = new Group(topLevel, SWT.NONE);
	  GridLayout layout = new GridLayout(1, false);
	  layout.marginHeight = 10;
	  layout.marginWidth = 10;
	  migratePropertiesPanel.setLayout (layout);
	  migratePropertiesPanel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	  
	  Label migrateInfo = new Label(migratePropertiesPanel, SWT.WRAP);

	  migrateInfo.setText (Messages.getString("GeneralPrefPage.MigrationInfo")); //$NON-NLS-1$
	  GridData labelGridData = new GridData(SWT.FILL,SWT.CENTER,true,false);
	  labelGridData.widthHint = 200;
	  migrateInfo.setLayoutData (labelGridData);
	  migrateInfo.pack ();
	  Button migrateButton = new Button (migratePropertiesPanel, SWT.PUSH);
	  migrateButton.addSelectionListener (new SelectionAdapter() {
	    @Override
	    public void widgetSelected (SelectionEvent e)
	    {
	      super.widgetSelected (e);
	      IProject project = projectPreferences.getProject ();
	      boolean success = ProjectPreferencesUtil.migrateTAPropertiesToModularFormat(project);
	      if (success) {
	        LogUtil.getLogForPlugin (Activator.PLUGIN_ID).logAndShowInfo (Messages.getString("GeneralPrefPage.MigrationSuccessMessage")); //$NON-NLS-1$
	        projectPreferences.getControl ().getShell ().close ();
	      } 
	      /*
	       * Not doing anything here if migration failed, because
	       * migrateTAPropertiesToModularFormat(project) would
	       * display the failure message along with reason.
	       */
	    }
	  });
	  migrateButton.setText (Messages.getString("GeneralPrefPage.MigrationButtonText")); //$NON-NLS-1$
	  
	  migrateButton.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
      public void getName (AccessibleEvent e) {
        e.result = Messages.getString("GeneralPrefPage.MigrationInfo") +"  " +Messages.getString("GeneralPrefPage.MigrationButtonText");
      }
    });
	}
	
	protected void createMainAQLPanel(){
		mainAQLPicker = new FileDirectoryPicker(topLevel, Constants.FILE_ONLY);
		mainAQLPicker.setDescriptionLabelText(Messages.getString("GeneralPrefPage.LOCATION_MAIN_AQL")); //$NON-NLS-1$
		mainAQLPicker.setAllowedFileExtensions(Constants.AQL_FILE_EXTENSION_STRING);
		mainAQLPicker.setAllowMultipleSelection(false);
		if(projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG){
			mainAQLPicker.enableUI(false);
		}
	}
	

	/**
	 * Empty implementation. To be implemented by subclasses
	 */
	protected void createCustomPanel() {
	}

	protected void createSearchPathPanel() {
		searchPathPrefPage = new SearchPathPrefPage(topLevel, projectPreferences, 
				Messages.getString("SearchPathPrefPage.DATAPATH"), false);//$NON-NLS-1$
	}


	/**
	 * Stores UI data into preferenceStore. Invoked when 'Apply' button of property sheet is clicked.
	 */
	@Override
	public void apply() {
		if(isEnableProvenanceVisible()){
			boolean enableProvenance = cbProvenance.getSelection();
			super.setValue(Constants.GENERAL_PROVENANCE, Boolean.toString(enableProvenance));
		}
		try{
			projectPreferences.getProjectProperties().setProvenance(Boolean.parseBoolean(preferenceStore.getString(Constants.GENERAL_PROVENANCE)));
		}catch(Exception e){
			//do nothing. This exception block is to ensure that unexpected exceptions are caught when enableProvenance property is read from PreferenceStore
			//Unlike other properties, enableProvenance property may not be available in all cases
		}
	
		setValue(Constants.GENERAL_MAINAQLFILE, mainAQLPicker.getFileDirValue());
		
		projectPreferences.getProjectProperties().setMainAQLFile(preferenceStore.getString(Constants.GENERAL_MAINAQLFILE));
		
		searchPathPrefPage.apply();
		paginationPrefPanel.apply();
	}

	/**
	 * Rstores the values from preferenceStore. This method is invoked when 'Restore defaults' button of property sheet is clicked
	 */
	@Override
	public void restoreDefaults() {
		if(preferenceStore == null){
			return;
		}

		if(isEnableProvenanceVisible()){
			Boolean enableProvenance = preferenceStore.getDefaultBoolean(Constants.GENERAL_PROVENANCE);
			cbProvenance.setSelection(enableProvenance);
		}
	
		String mainAQL = preferenceStore.getDefaultString(Constants.GENERAL_MAINAQLFILE);
		mainAQLPicker.setFileDirValue(ProjectPreferencesUtil.getPath(mainAQL), ProjectPreferencesUtil.isWorkspaceResource(mainAQL));
		
		searchPathPrefPage.restoreDefaults();
		paginationPrefPanel.restoreDefaults();
	}
	

	
	public void addModifyListeners(ModifyListener listener){
		mainAQLPicker.addModifyListenerForFileDirTextField(listener);
	}
	
	public void removeModifyListeners(ModifyListener listener){
		mainAQLPicker.removeModifyListenerForFileDirTextField(listener);
	}
	
	public void addDataPathChangeListener(PropertyChangeListener listener){
		searchPathPrefPage.addDataPathChangeListener(listener);
	}
	
	public void removeDataPathChangeListener(PropertyChangeListener listener){
		searchPathPrefPage.addDataPathChangeListener(listener);
	}	

	/**
	 * Restores the UI form fields to values picked from properties argument
	 */
	@Override
	public void restoreToProjectProperties(SystemTProperties properties) {
		if(isEnableProvenanceVisible()){
			cbProvenance.setSelection(properties.getEnableProvenance());
		}
		String mainAQL = properties.getMainAQLFile();
		mainAQLPicker.setFileDirValue(ProjectPreferencesUtil.getPath(mainAQL), ProjectPreferencesUtil.isWorkspaceResource(mainAQL));

		searchPathPrefPage.restoreToProjectProperties(properties);
		paginationPrefPanel.restoreToProjectProperties(properties);
	}
	
	/**
	 * validates values entered in the UI
	 * 
	 * @return true if the entries in the UI are valid, false otherwise
	 */
	public boolean isValid(){
		String mainAQL = mainAQLPicker.getFileDirValue();
		if(!StringUtils.isEmpty(mainAQL) && !FileDirectoryPickerUtil.isPathValid(mainAQL)){
			setErrorMessage(Messages.getString("General.ERR_MAIN_AQL_NOT_FOUND")); //$NON-NLS-1$
			return false;
		}
		
		if(!searchPathPrefPage.isValid()){
			setErrorMessage(searchPathPrefPage.getErrorMessage());
			return false;
		}
		
		if(!paginationPrefPanel.isValid()) {
			setErrorMessage(paginationPrefPanel.getErrorMessage());
			return false;
		}
		setErrorMessage(null);
		return true;
	}
	
	public String getErrorMessage(){
		return errorMessage;
	}
	
	protected void setErrorMessage(String errorMsg){
		this.errorMessage = errorMsg;
	}
	
	public void addSelectionListeners(
			SelectionListener listener) {
		if(cbProvenance != null){
			this.cbProvenance.addSelectionListener(listener);
		}
	}
	
	private boolean isEnableProvenanceVisible(){
		if(projectPreferences.consumer == Constants.CONSUMER_PROPERTY_SHEET
				|| projectPreferences.consumer == Constants.CONSUMER_RUN_CONFIG){
			TextAnalyticsWorkspacePreferences wprefs = PreferencesPlugin.getTextAnalyticsWorkspacePreferences ();
			return wprefs.getPrefShowEnableProvenanceOption ();
		}else{
			return false;
		}
	}
}
