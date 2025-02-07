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
package com.ibm.biginsights.project.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.project.Messages;
import com.ibm.biginsights.project.locations.IBigInsightsLocation;
import com.ibm.biginsights.project.sampleProjects.SampleProjectsProvider;
import com.ibm.biginsights.project.util.BIConstants;
import com.ibm.json.java.JSONObject;

public class ImportProjectCommand extends AbstractHandler  {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {		
		String commandId = event.getCommand().getId();
		String projectSuffix = BIConstants.SAMPLE_PROJECT_SUFFIX;
		String projectType = null;
		if (commandId.equals("com.ibm.biginsights.project.importMDAProjectCommand")) { //$NON-NLS-1$
			projectSuffix = BIConstants.MDA_PROJECT_SUFFIX;
			projectType = Messages.IMPORTPROJECTCOMMAND_MACHINE_DATA_TYPE;
		}
		else if (commandId.equals("com.ibm.biginsights.project.importSDAProjectCommand")) { //$NON-NLS-1$
			projectSuffix = BIConstants.SDA_PROJECT_SUFFIX;
			projectType = Messages.IMPORTPROJECTCOMMAND_SOCIAL_DATA_TYPE;
		}
		
		SampleProjectSelectionDialog dlg = new SampleProjectSelectionDialog(
													Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell(), 
													null, null, projectSuffix, projectType);
		if (dlg.open()==Window.OK && dlg.getResult().length>0) {
			
			SampleProjectsProvider _projectsProvider = new SampleProjectsProvider();			
			IBigInsightsLocation loc = (IBigInsightsLocation)dlg.getResult()[0];
			JSONObject selectedProject = (JSONObject)dlg.getResult()[1];			
			
			_projectsProvider.importSampleProject(loc, (String)selectedProject.get("name"),  //$NON-NLS-1$
													projectSuffix, (String)selectedProject.get("path")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}

}
