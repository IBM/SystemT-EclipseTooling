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
package com.ibm.biginsights.textanalytics.goldstandard.handler.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.PreferenceStore;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.model.AnnotationType;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldType;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class reaps Annotation Types from an .lc file and adds them to the lc.prefs file
 * 
 *  Krishnamurthy
 *
 */
public class AnnotationTypesImporter {



	protected IFolder gsFolder;
	private IProgressMonitor progressMonitor;
	protected PreferenceStore prefStore;
	protected StringBuilder annotationReapedFiles = new StringBuilder();
	protected StringBuilder reapedAnnTypes = new StringBuilder();

	/**
	 * @param gsFolder
	 */
	public AnnotationTypesImporter(IFolder gsFolder, IProgressMonitor progressMonitor) {
		super();
		this.gsFolder = gsFolder;
		this.progressMonitor = progressMonitor;
	}
	
	public void run() throws CoreException{
		File gsDir = gsFolder.getLocation().toFile();
		File[] gsFiles = gsDir.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(Constants.GS_FILE_EXTENSION_WITH_DOT);
			}
		});
		
		prefStore = GoldStandardUtil.getGSPrefStore(gsFolder);
		if(gsFiles != null){
			progressMonitor.beginTask(Messages.AnnotationImporter_IMPORTING_ANNOTATION_TYPES, gsFiles.length);
			for (File gsFile : gsFiles) {
				IFile file = gsFolder.getFile(gsFile.getName());
				Serializer ser = new Serializer();
				SystemTComputationResult model = ser.getModelForInputStream(file.getContents());
				bootstrapAnnotationTypesFromModel(file, model);
			}
		}
		
		try {
			if(reapedAnnTypes.toString().trim().length() != 0){
				//markFileAsAnnotationReaped(file);
				if(prefStore.needsSaving()){
					prefStore.save();
					
					LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(
							MessageUtil.formatMessage(Messages.AnnotationImporter_ANNOTATION_TYPES_AUTO_IMPORTED,
									gsFolder.getName(),
									reapedAnnTypes.toString()							
							));
				}
			}
			else
			{
				if(prefStore.needsSaving()){
					prefStore.save();
				}
					LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(Messages.AnnotationImporter_ANNOTATION_TYPES_NO_SPAN_DOCUMENT_TYPES);
				
			}
		} catch (IOException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}
		
		try {
			gsFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
		}	
		
	}
	
//	private boolean shouldBootstrapAnnotationTypes(IFile file) {
//		String value = annotationReapedFiles.toString();
//		if(!StringUtils.isEmpty(value)){
//			if(value.contains(file.getFullPath().toString())){
//				return false;
//			}
//		}
//		return true;
//	}
	
	private void bootstrapAnnotationTypesFromModel(IFile file, SystemTComputationResult model) {
		progressMonitor.subTask(MessageUtil.formatMessage(Messages.AnnotationImporter_IMPORTING_ANNOTATION_TYPES_FROM, file.getName()));
		IFolder gsFolder = (IFolder) file.getParent();
		
		String prefStoreKey = Constants.GS_ANNOTATION_TYPES; //$NON-NLS-1$
		AnnotationType[] existingTypes = GoldStandardUtil.getAnnotationTypes(prefStore, gsFolder.getName());
		int count = 0;
		if(existingTypes != null){
			count = existingTypes.length;
		}
		
		OutputView views[] = model.getOutputViews();
		if(views != null && views.length >=0 ){
			for (int i = 0; i < views.length; i++) {
				OutputView view = views[i];
				String viewName = view.getName();
				
				String[] fieldNames = view.getFieldNames();
				FieldType[] fieldTypes = view.getFieldTypes();
				String value = ""; //$NON-NLS-1$
				for (int j = 0; j < fieldTypes.length; j++) {
					FieldType fieldType = fieldTypes[j];
					//Pick only SPAN typed fields
					if(fieldType.equals(FieldType.SPAN)){
						String fieldName = fieldNames[j];
						value = prefStore.getString(prefStoreKey);
						if(value.contains(viewName+","+fieldName)){ //$NON-NLS-1$
							//an annotation type for this view name & field name already exists. So, skip the current one
							continue;
						}else{
							int shortcutKey = count ;//just a dummy key
							String strKey = (shortcutKey>=0 && shortcutKey<=9)? String.valueOf(shortcutKey):"";
							
							//RGB rgb = new RGB(0,0,0);//dummy color
							AnnotationType annType = new AnnotationType(viewName, fieldName, true, strKey);
							if(StringUtils.isEmpty(value)){
								prefStore.putValue(prefStoreKey, annType.toString()+";"); //$NON-NLS-1$
							}else{
								prefStore.putValue(prefStoreKey, value +annType.toString() + ";"); //$NON-NLS-1$
							}
							if (count < 5)
							{
								// This is only for display and we want to display only 5 so that we dont clutter the screen
								reapedAnnTypes.append(viewName+"."+fieldName+"  "); //$NON-NLS-1$ //$NON-NLS-2$
							}
							count++;
						}
					}
					
				}//end: for each fieldType
			}//end: for each view
		}//end: if views[] is not empty
		progressMonitor.worked(1);
	}

//	private void markFileAsAnnotationReaped(IFile file){
//		annotationReapedFiles.append(",");
//		annotationReapedFiles.append(file.getFullPath().toString());
//	}
}
