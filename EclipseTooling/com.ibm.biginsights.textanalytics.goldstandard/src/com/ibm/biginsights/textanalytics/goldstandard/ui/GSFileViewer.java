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
package com.ibm.biginsights.textanalytics.goldstandard.ui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

import com.ibm.biginsights.textanalytics.concordance.ui.ResultEditor;
import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.model.GoldStandardModel;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.EditorInput;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * AQL annotation editor class.
 *  Krishnamurthy
 * 
 */
public class GSFileViewer extends ResultEditor {



	private static final String GS_EDITOR_SCOPE = "com.ibm.biginsights.textanalytics.GoldstandardEditorScope";
	
	public GSFileViewer() {
		super();
		this.setHelpContextId("com.ibm.biginsights.textanalytics.tooling.help.labeled_document_collection");
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {

		setDocumentProvider(new FileDocumentProvider());
		FileEditorInput fileEditorInput = (FileEditorInput)input;
		IFile file = fileEditorInput.getFile();
		
		IFolder tempFolder = ((IFolder)file.getParent()).getFolder(Constants.TEMP_TEXT_DIR_NAME);//GoldStandardUtil.getGSParentFolder(file.getProject(), true).getFolder(Constants.TEMP_TEXT_DIR_NAME);
		if(!tempFolder.exists()){
			try {
				tempFolder.create(true, true, null);
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
			}
		}
		
		try {
			tempFolder.setHidden(true);
		} catch (CoreException e1) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e1.getLocalizedMessage());
		}
		
		Serializer ser = new Serializer();
		try {
			SystemTComputationResult model = ser.getModelForInputStream(file.getContents(true));
			String text = model.getInputText();
			InputStream source = new ByteArrayInputStream(text.getBytes(Constants.ENCODING));
			IFile editorFile = tempFolder.getFile(file.getName()+".temp");
			if(editorFile.exists()){
				editorFile.delete(true, null);
			}
			editorFile.create(source, true, null);
			tempFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
			EditorInput editorInput = new EditorInput(editorFile, model.getInputTextID(), file.getFullPath().toString(), new GoldStandardModel(model),null);
			editorInput.setUserData(file);
			super.init(site, editorInput);
			
			installEncodingSupport();
			fEncodingSupport.setEncoding(Constants.ENCODING);
		} catch (CoreException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
		} catch (UnsupportedEncodingException e) {
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
		}
		
	}
	
	@Override
	public boolean isEditable() {
	    return false;
	}

	@Override
	public boolean isEditorInputModifiable() {
	    return false;
	}

	@Override
	public boolean isEditorInputReadOnly() {
	    return true;
	}

	@Override
	public boolean isDirty() {
	    return false;
	}
	
	@Override
	protected void initializeKeyBindingScopes() {
		String[] scopes = new String[] { "org.eclipse.ui.textEditorScope",
				GS_EDITOR_SCOPE };
		setKeyBindingScopes(scopes);
	}
	
	@Override
	public void dispose ()
	{
	  // Disposing GoldStandardModel object contained in EditorInput object.
	  super.dispose ();
	  EditorInput eInput =  (EditorInput) getEditorInput ();
	  eInput.dispose ();
	}

}
