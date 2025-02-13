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
package com.ibm.biginsights.textanalytics.launch;

import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProjectPreferences;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class RunPreferences extends SystemTProjectPreferences {

	@SuppressWarnings("unused")
private static final String _COPYRIGHT = "Copyright IBM\n"+
 "Licensed under the Apache License, Version 2.0 (the \"License\");\n"+
 "you may not use this file except in compliance with the License.\n"+
 "You may obtain a copy of the License at\n\n"+
 "    http://www.apache.org/licenses/LICENSE-2.0\n\n"+
 "Unless required by applicable law or agreed to in writing, software\n"+
 "distributed under the License is distributed on an \"AS IS\" BASIS,\n"+
 "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"+
 "See the License for the specific language governing permissions and\n"+
 "limitations under the License.";

	SystemTRunConfig runConfig;
	
	public RunPreferences(String projectName) {
		super(Constants.CONSUMER_RUN_CONFIG, projectName);
		runConfig = new SystemTRunConfig(null, null, null, getProjectProperties());
	}

	public SystemTRunConfig getRunConfig(){
		return runConfig;
//		SystemTProperties projectProperties = getProjectProperties();
//		GeneralPrefPage genPrefPage = getGeneralPrefPage();
//		String language = null;
//		String inputCollection = null;
//		
//		if(genPrefPage != null && genPrefPage instanceof RunConfigGeneralPage){
//			RunConfigGeneralPage runConfigGenPage = (RunConfigGeneralPage)genPrefPage;
//			language = runConfigGenPage.getLanguage();
//			inputCollection = runConfigGenPage.getInputCollection();
//		}
//		String resultDir = ProjectPreferencesUtil.getDefaultResultDir(ProjectPreferencesUtil.getProject(projectName));
//		SystemTRunConfig runConfig = new SystemTRunConfig(language, inputCollection, resultDir, projectProperties);
//		return runConfig;
	}

	@Override
	protected void createTabGeneral(TabFolder tabFolder, TabItem tabGeneral) {
		generalPrefPage = new RunConfigGeneralPage(tabFolder, this);
	}
	
	
}
