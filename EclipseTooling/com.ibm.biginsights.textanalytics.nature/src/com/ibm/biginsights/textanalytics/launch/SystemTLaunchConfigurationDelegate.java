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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import com.ibm.biginsights.textanalytics.nature.Activator;
import com.ibm.biginsights.textanalytics.nature.Messages;
import com.ibm.biginsights.textanalytics.nature.run.SystemTRunConfig;
import com.ibm.biginsights.textanalytics.nature.run.SystemtRunJob;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.IRunConfigConstants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;

public class SystemTLaunchConfigurationDelegate extends JavaLaunchDelegate
{
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

	private String[] combinedClasspaths = null;

	@Override
  public void launch (ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
  {
    String projectName = configuration.getAttribute (IRunConfigConstants.PROJECT_NAME, ""); //$NON-NLS-1$
    IProject project = ProjectPreferencesUtil.getProject (projectName);

    SystemTRunConfig runConfig = ProjectPreferencesUtil.createRunConfiguration (project, configuration);

    combinedClasspaths = getCombinedClasspaths(configuration, project);

    Job systemtJob = new SystemtRunJob (
      Messages.getString ("SystemTLaunchConfigurationDelegate.RUNNING_SYSTEMT") + project.getName (), project, runConfig); //$NON-NLS-1$
    systemtJob.setUser (true);
    systemtJob.schedule ();
  }
	
  /**
   * We want to run AQL with not only the classpath we have now but also classpath set in project build path. 
   * @return The combined classpath, plugin classpath and project build path classpath.
   */
  private String[] getCombinedClasspaths (ILaunchConfiguration configuration, IProject project)
  {
    String wsAbsPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString ();

    try {
      String[] pluginClasspaths = this.getClasspath (configuration);

      if (pluginClasspaths != null) {

        List<String> allClasspaths = new ArrayList<String>(Arrays.asList (pluginClasspaths));

        IJavaProject javaProject = JavaCore.create (project);
        IClasspathEntry[] cpEntries = javaProject.getResolvedClasspath (true);

        for (IClasspathEntry cpe : cpEntries) {
          String p = cpe.getPath ().toOSString ();
          if (allClasspaths.contains (p) == false) {
            File f = new File (p);
            if (f.exists ())
              allClasspaths.add (f.getCanonicalPath ());
            else {
              String absPath = wsAbsPath + p;
              f = new File (absPath);
              if ( f.exists () &&
                   allClasspaths.contains (absPath) == false )
                allClasspaths.add (absPath);
            }
          }
        }

        return allClasspaths.toArray (new String[0]);
      }
    }
   catch (Exception e) {
      Activator.getDefault ().getLog ().
        log (new Status (IStatus.ERROR, Activator.PLUGIN_ID, "Getting combined classpath encountered an error:\n" + e.getMessage ())); //$NON-NLS-1$
    }

    return null;
  }

  @Override
  public String[] getClasspath(ILaunchConfiguration configuration) throws CoreException
  {
    if (combinedClasspaths != null)
      return combinedClasspaths;
    else
      return super.getClasspath (configuration);
  }

  @Override
  protected IProject[] getBuildOrder (ILaunchConfiguration configuration, String mode) throws CoreException
  {
    //Ignoring parameter 'mode'. Build order will always be same.
    String projectName = configuration.getAttribute (IRunConfigConstants.PROJECT_NAME, "");
    if (!projectName.trim().isEmpty ()) {
      IProject proj = ProjectUtils.getProject (projectName);
      if (proj != null) {
        IProject[] baseProjects = {proj};
        IProject[] order = computeReferencedBuildOrder (baseProjects); //Looks into transitive relations too.
        return order; 
      }
    }
    return null; 
    //^ In case, the launchconfiguration does not have a valid project name 
    //(shouldn't happen - expect 'run' button to be disabled), 
    //this would result in an incremental workspace build.
  }
	
	
}
