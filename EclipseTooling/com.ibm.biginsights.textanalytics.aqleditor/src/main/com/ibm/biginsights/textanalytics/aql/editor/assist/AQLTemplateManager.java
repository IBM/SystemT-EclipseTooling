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
package com.ibm.biginsights.textanalytics.aql.editor.assist;


import java.io.IOException;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;

import com.ibm.biginsights.textanalytics.aql.editor.Activator;


/**
 *
 *  Babbar
 */
public class AQLTemplateManager {




	private static AQLTemplateManager instance;
	private TemplateStore fStore;
	private ContributionContextTypeRegistry fRegistry;

	private AQLTemplateManager(){
	}

	public static AQLTemplateManager getInstance(){
		if(instance==null){
			instance = new AQLTemplateManager();
		}
		return instance;
	}

	public TemplateStore getTemplateStore(String context){
		if (fStore == null){
			fStore = new ContributionTemplateStore(getContextTypeRegistry(context), Activator.getDefault().getPreferenceStore(), context);
			try {
				fStore.load();
			} catch (IOException e){
			}
		}
		
		return fStore;
	}

	public ContextTypeRegistry getContextTypeRegistry(String context){
		if (fRegistry == null){
			fRegistry = new ContributionContextTypeRegistry();
			
			try {
				fRegistry.addContextType(context);
			} catch (AssertionFailedException e) {
				System.out.println("SOMETHING WRONG");
			}
			
		}
		return fRegistry;
	}

	public IPreferenceStore getPreferenceStore(){
		return Activator.getDefault().getPreferenceStore();
	}

//	public void savePluginPreferences(){
//		HTMLPlugin.getDefault().savePluginPreferences();
//	}

}
