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
package com.ibm.biginsights.textanalytics.nature;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.ibm.biginsights.project.Activator;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class AQLNature implements IProjectNature {



  /**
   * ID of this project nature
   */
  public static final String NATURE_ID = "com.ibm.biginsights.textanalytics.nature";

  private IProject project;
  
  public static final boolean MODULAR_AQL_PROJECT_DEFAULT_VALUE = true; 

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#configure()
   */
  public void configure() throws CoreException {
    IProjectDescription desc = this.project.getDescription();
    ICommand[] commands = desc.getBuildSpec();



    for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(AQLBuilder.BUILDER_ID)) {
        return;
      }
    }

    ICommand[] newCommands = new ICommand[commands.length + 1];
    System.arraycopy(commands, 0, newCommands, 0, commands.length);
    ICommand command = desc.newCommand();
    command.setBuilderName(AQLBuilder.BUILDER_ID); 
    newCommands[newCommands.length - 1] = command;
    desc.setBuildSpec(newCommands);
    this.project.setDescription(desc, null);
    
    ProjectPreferencesUtil.createDefaultDirs(project);
    createDefaultSystemTPreferences(project);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#deconfigure()
   */
  public void deconfigure() throws CoreException {
    IProjectDescription description = getProject().getDescription();
    ICommand[] commands = description.getBuildSpec();
    // I don't know if this code is really needed
   /* for (int i = 0; i < commands.length; ++i) {
      if (commands[i].getBuilderName().equals(AQLBuilder.BUILDER_ID)) {
        ICommand[] newCommands = new ICommand[commands.length - 1];
        System.arraycopy(commands, 0, newCommands, 0, i);
        System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
        description.setBuildSpec(newCommands);
        this.project.setDescription(description, null);
        return;
      }
    }
    */
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#getProject()
   */
  public IProject getProject() {
    return this.project;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
   */
  public void setProject(IProject project) {
    this.project = project;
  }
  
  private void createDefaultSystemTPreferences(IProject project) {
	  
	  String systemTPreferencesFile = project.getLocation() + File.separator + Constants.TEXT_ANALYTICS_PREF_FILE;

	  // GHE #31 Override a prior .textanalytics state when "Add the BigInsights Nature"
	  //   In the case that the .textanalytics file exists,
	  //   confirm that the user wants to overwrite it.
	  File file = new File (systemTPreferencesFile);
	  if (file.exists ()) {
	      Shell shell = ProjectUtils.getActiveWorkbenchWindow ().getShell ();
	      boolean ok = MessageDialog.openQuestion (shell,
	              Messages.getString ("AQLNature.OVERWRITE_TEXT_ANALYTICS"),
	              Messages.getString ("AQLNature.CONFIRM_IF_OVERWRITING_TEXT_ANALYTICS"));
	      if (!ok) return;
	  }

	  PreferenceStore prefStore = new PreferenceStore(systemTPreferencesFile);
	  
	  prefStore.setValue(Constants.GENERAL_PROVENANCE, "true");
	  prefStore.setValue(Constants.MODULAR_AQL_PROJECT, MODULAR_AQL_PROJECT_DEFAULT_VALUE);
	  prefStore.setValue(Constants.SEARCHPATH_DATAPATH,  ProjectPreferencesUtil.getDefaultDataPath(project));
	  prefStore.setValue(Constants.MODULE_SRC_PATH, ProjectPreferencesUtil.createDefaultModuleSrcPath(project));
	  prefStore.setValue(Constants.MODULE_BIN_PATH, ProjectPreferencesUtil.createDefaultModuleBinPath(project));
	  prefStore.setValue(Constants.PAGINATION_ENABLED, Constants.PAGINATON_ENABLED_DEFAULT_VALUE);
	  prefStore.setValue(Constants.PAGINATION_FILES_PER_PAGE, Constants.PAGINATION_FILES_PER_PAGE_DEFAULT_VALUE);
	 
	  try {
		prefStore.save();
	} catch (IOException e) {
		Activator
		.getDefault()
		.getLog()
		.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
				.getMessage()));

	}
}


}
