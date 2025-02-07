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
package com.ibm.biginsights.textanalytics.goldstandard.ui.prefpages;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.ibm.avatar.algebra.util.lang.LangCode;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationTypesModelProvider;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;

/**
 * Preference page that allows a user to specify some general preferences for gold standard.
 * Preferences such as default annotation type and auto-detection of word boundaries
 *  Krishnamurthy
 *
 */
public class GSGeneralPage extends GenericPrefPage implements IWorkbenchPreferencePage{



	protected Label lbDetectWordBoundaries;
	protected Label lbLanguage;
	protected Label lbAnnotationTypes;
	protected Button checkBoxDetectWordBoundaries;
	protected Combo cbLanguage;
	protected Combo cbAnnotationTypes;
	protected AnnotationType[] annotationTypes;
	//protected String[] annotationTypeNames;
	
	public GSGeneralPage(IProject project, IFolder gsFolder){
		super(project, gsFolder);
	}

	private String[] getAnnotationTypeNames() {
		if(annotationTypes != null){
			String[] annTypeNames = new String[annotationTypes.length];
			for (int i = 0; i < annotationTypes.length; i++) {
				if(annotationTypes[i] != null && annotationTypes[i].isEnabled()){
					annTypeNames[i] = annotationTypes[i].getViewName() + "." +annotationTypes[i].getFieldName(); //$NON-NLS-1$
				}
			}
			return annTypeNames;
		}else return new String[0];
	}

	@Override
	protected Control createContents(Composite parent) {
		//noDefaultAndApplyButton();
		Composite contents = new Composite(parent, SWT.NONE);
		contents.setLayout(new GridLayout(1, true));
		
		Composite panelDetectWB = new Composite(contents, SWT.NONE);
		panelDetectWB.setLayout(new GridLayout(2, false));
		checkBoxDetectWordBoundaries = new Button(panelDetectWB, SWT.CHECK);
		checkBoxDetectWordBoundaries.setText(Messages.GSGeneralPage_DETECT_WORD_BOUNDARIES);
		
		Composite panel = new Composite(contents, SWT.NONE);
		panel.setLayout(new GridLayout(2, false));
		lbLanguage = new Label(panel, SWT.NONE);
		lbLanguage.setText(Messages.GSGeneralPage_LANGUAGE);
		cbLanguage = new Combo(panel, SWT.READ_ONLY);
		// Languages are in synch with the list of languages supported by the SystemT Runtime
		for (LangCode language : LangCode.values()) {
			cbLanguage.add(language.name());
		}
		cbLanguage.select(cbLanguage.indexOf("en")); //$NON-NLS-1$
		
		lbAnnotationTypes = new Label(panel, SWT.NONE);
		lbAnnotationTypes.setText(Messages.GSGeneralPage_DEFAULT_ANNOTATION_TYPE);
		
		cbAnnotationTypes = new Combo(panel, SWT.READ_ONLY);
		populateAnnotationTypes(getAnnotationTypeNames());
		
		Composite tipPanel = new Composite(contents, SWT.NONE);
		GridLayout tipPanelLayout = new GridLayout(2, false);
		tipPanelLayout.marginLeft = 0;
		tipPanel.setLayout(tipPanelLayout);
		Label lbImage = new Label(tipPanel, SWT.NONE);
		lbImage.setImage(GoldStandardUtil.getImage("icons/tip.gif")); //$NON-NLS-1$
		Text tfDefaultAnnotationTip = new Text(tipPanel, SWT.WRAP | SWT.MULTI);
		tfDefaultAnnotationTip.setText(Messages.GSGeneralPage_TIP_DEFAULT_ANNOTATION);
		GridDataFactory
		.fillDefaults()
		.align(SWT.FILL, SWT.CENTER)
		.grab(true, false)
		.hint(
				convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH),
				SWT.DEFAULT).applyTo(tfDefaultAnnotationTip);
		tfDefaultAnnotationTip.setBackground(contents.getBackground());
		tfDefaultAnnotationTip.setEditable (false);
		restoreFromPreferences();
		return contents;
	}

	
	private void populateAnnotationTypes(String[] annTypes) {
		cbAnnotationTypes.removeAll();
		for (String annType : annTypes) {
			if(annType != null){
				cbAnnotationTypes.add(annType);
			}
		}
	}

	private void restoreFromPreferences() {
		String defaultAnnotationType = getPreferenceStore().getString(PARAM_DEFAULT_ANNOTATION_TYPE);
		boolean detectWordBoundaries = false;
		
		try {
			String strDetectWordBoundaries = getPreferenceStore().getString(PARAM_DETECT_WORD_BOUNDARIES);
			if(StringUtils.isEmpty(strDetectWordBoundaries)){
				//it is not set yet. So, default it to true
				detectWordBoundaries = true;
			}else{
				detectWordBoundaries = Boolean.parseBoolean(strDetectWordBoundaries);
			}
		} catch (Exception e) {
			detectWordBoundaries = false;
		}
	
		checkBoxDetectWordBoundaries.setSelection(detectWordBoundaries);
		cbAnnotationTypes.select(cbAnnotationTypes.indexOf(defaultAnnotationType));
		
	}

	@Override
	public void init(IWorkbench workbench) {
		this.workbench = workbench;
	}
		
	@Override
	protected void performApply() {
		IPreferenceStore gsPreferences = getPreferenceStore();
		
		if(checkBoxDetectWordBoundaries != null && cbAnnotationTypes != null){
			gsPreferences.setValue(PARAM_DETECT_WORD_BOUNDARIES, String.valueOf(checkBoxDetectWordBoundaries.getSelection()));
			
			int langIndex = cbLanguage.getSelectionIndex();
			if(langIndex >= 0){
				gsPreferences.setValue(PARAM_LANGUAGE, cbLanguage.getItem(langIndex));
			}
			int selectionIndex = cbAnnotationTypes.getSelectionIndex();
			if(selectionIndex >=0 ){
				gsPreferences.setValue(PARAM_DEFAULT_ANNOTATION_TYPE, cbAnnotationTypes.getItem(selectionIndex));
			}
		}
		super.performApply();
	}

	@Override
	protected void refreshData() {
		loadAnnotationTypes();
		populateAnnotationTypes(getAnnotationTypeNames());
		restoreFromPreferences();
	}
	
	protected void loadAnnotationTypes() {
		String prefAnnotationTypes =  GoldStandardUtil.getGSPrefStore(gsFolder).getString(PARAM_ANNOTATION_TYPES);
		if(StringUtils.isEmpty(prefAnnotationTypes)){
			return;
		}
		annotationTypes = GoldStandardUtil.splitAnnotationTypes(prefAnnotationTypes);
	}

	public AnnotationType[] getAnnotationTypes(){
		return annotationTypes;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		checkBoxDetectWordBoundaries.setSelection(
				getPreferenceStore().getBoolean(PARAM_DETECT_WORD_BOUNDARIES));
		cbLanguage.select(cbLanguage.indexOf(getPreferenceStore().getString(PARAM_LANGUAGE)));
		cbAnnotationTypes.select(cbAnnotationTypes.indexOf(getPreferenceStore().getString(PARAM_DEFAULT_ANNOTATION_TYPE)));
	}

	@Override
	protected void setProject(IProject project) {
		super.setProject(project);
		loadAnnotationTypes();
		AnnotationTypesModelProvider.getInstance(project.getName(), gsFolder.getName()).refreshAnnotationTypes();
	}
	
	
}
