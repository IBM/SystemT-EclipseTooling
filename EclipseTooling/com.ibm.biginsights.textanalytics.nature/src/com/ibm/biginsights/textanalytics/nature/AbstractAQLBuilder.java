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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.aql.ParseException;
import com.ibm.avatar.aql.compiler.CompilerWarning;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.ILog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

public abstract class AbstractAQLBuilder {

	@SuppressWarnings("unused")
  private static final String _COPYRIGHT = "Licensed Materials - Property of IBM\n(C) Copyright IBM Corp\n"+//$NON-NLS-1$
		"US Government Users Restricted Rights - Use, duplication disclosure restricted by GSA ADP Schedule Contract with IBM Corp.";//$NON-NLS-1$

	private static final ILog logger = LogUtil
	.getLogForPlugin(Activator.PLUGIN_ID);

	protected static final String TEXT_ANALYTICS_FOLDER_DECORATOR_ID = "com.ibm.biginsights.project.decorator.TextAnalyticsFolderDecorator";
	
	protected AQLBuilder aqlBuilder;
	//Used to set the project details from AQLBuilder
	protected IProject project;
	//Used to check whether the referenced project need to be build.
	protected boolean isReferencedProjectToBeBuild;

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}
	
	public boolean isReferencedProjectToBeBuild() {
		return isReferencedProjectToBeBuild;
	}

	public void setReferencedProjectToBeBuild(boolean isReferencedProjectToBeBuild) {
		this.isReferencedProjectToBeBuild = isReferencedProjectToBeBuild;
	}

	class AQLErrorHandler {

		/*
		 * This method is used to determine the problem file against which the
		 * problems have to be marked. If the problem file is a file in the current
		 * workspace file then we have to show the markers against
		 * that file itself, else the problems need to be marked against the main AQL file only.
		 */
		private IFile getProblemFileFromParseException(ParseException pe, IFile mainAQLFile) {
		  //Getting path to erroneous file from parse exception
		  String problemFileName = pe.getFileName ();
  		if (problemFileName != null) { //sometimes ParseException does not contain file name, even in non-modular aql code
  			IFile problemFile = FileBuffers
  			.getWorkspaceFileAtLocation(new Path(problemFileName));
  			// It could be null if the aql file is an included external file (this can happen in non-modular projects)
  			if (problemFile != null) {				
  				return problemFile;
  			}
		  }
			return mainAQLFile;
		}

		/*
		 * This method handles the ParseException by extracting 
		 * line number, error description and filename from the exception, and uses that 
		 * info to populate the marker info. Used by NonModularAQLBuilder
		 * 
		 * @param pe
		 * 
		 * @param severity
		 */
		protected void handleError(ParseException pe, int severity,
		  IFile mainAQLFile) {
		  IFile file = getProblemFileFromParseException(pe, mainAQLFile);
		  this.addMarker(file, pe.getErrorDescription (), pe.getLine (), severity);
		}

		/**
		 * This method handles the ParseException by extracting
		 * line number, error description and filename from the exception and uses them
		 * to populate the the marker information.
		 * If a ParseException does not provide a file name, it marks the error against
		 * the provided project's directory. Used by ModularAQLBuilder
		 * @param pe
		 * @param severity
		 * @param buildProject
		 */
		protected void handleError(ParseException pe, int severity, IProject buildProject) {
		  String fileName = pe.getFileName ();
		  if (fileName == null) { //If ParseException does not have a file name associated with it, mark error against project directory
		    this.addMarker (buildProject, pe.getErrorDescription (), severity);
		  }
		  else { //When file name is associated with parse exception, mark error against that file.
		    IFile problemFile = FileBuffers.getWorkspaceFileAtLocation(new Path(fileName));
		    this.addMarker(problemFile, pe.getErrorDescription (), pe.getLine (), severity);
		  }

		}
		
		/**
     * This method handles the Warnings reported by compiler, it extracts 
     * line number, error description and filename from the CompilerWarning and uses them
     * to populate the marker information.
     * If a CompilerWarning does not provide a file name, it marks the error against
     * the provided project's directory. Used by ModularAQLBuilder
     * @param cWarning the compiler warning object returned by the AQL compiler
     * @param originatingProject project of the aql script where this warning is originated 
     */
    protected void handleWarning(CompilerWarning cWarning, IProject originatingProject) {
      String fileName = cWarning.getFileName ();
      IFile iFile = ProjectUtils.getFileWithAbsPath (originatingProject, fileName);
      if (iFile == null) { 
        this.addMarker (originatingProject, cWarning.toString (), IMarker.SEVERITY_WARNING);
      } else { 
        this.addMarker(iFile, cWarning.toString (), cWarning.getBeginLine (), IMarker.SEVERITY_WARNING);
      }
    }

		/*
		 * This is the method that actually creates the marker and adds them to the file.
		 * 
		 * @param file
		 * @param message
		 * @param lineNumber
		 * @param severity
		 */
		private void addMarker(IFile file, String message, int lineNumber, int severity)
		{
		  // Defect 78913 -- when file info does not exist, set marker to the project.
		  if (file == null)
		    addMarker (getProject(), message, severity);

      try {
        // putting a check to see if the same marker exist from the parser,
        // if yes then return otherwise create the marker
        boolean exist = false;
        IMarker[] problems = null;
        int depth = IResource.DEPTH_INFINITE;
        try {
          problems = file.findMarkers (AQLBuilder.PARSE_MARKER_TYPE, true, depth);
        }
        catch (CoreException e) {
          logger.logError (e.getMessage ());
          problems = new IMarker[0];
        }

        for (int i = 0; i < problems.length; i++) {
          if (problems[i].getAttribute (IMarker.MESSAGE).equals (message)) {
            exist = true;
          }
        }

        if (!exist) {
          IMarker marker = file.createMarker (AQLBuilder.COMPILE_MARKER_TYPE);
          marker.setAttribute (IMarker.MESSAGE, message);
          marker.setAttribute (IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
          marker.setAttribute (IMarker.SEVERITY, severity);
          if (lineNumber <= 0) {
            lineNumber = 1;
          }
          marker.setAttribute (IMarker.LINE_NUMBER, lineNumber);
        }
      }
      catch (CoreException e) {
        logger.logError (e.getMessage ());
      }
		}
	
		/**
		 * This method creates a marker with the given message and severity
		 * and adds it against the given project's directory.
		 * @param project
		 * @param errorMessage
		 * @param severity
		 */
		protected void addMarker(IProject project, String errorMessage, int severity) {
		  try {
		    // putting a check to see if the same marker exist from the
		    // parser,
		    // if yes then return otherwise create the marker
		    boolean exist = false;
		    IMarker[] problems = null;
		    int depth = IResource.DEPTH_INFINITE;
		    try {
		      problems = project.findMarkers(AQLBuilder.PARSE_MARKER_TYPE, true, depth);
		    } catch (CoreException e) {
		      logger.logError(e.getMessage());
		      problems = new IMarker[0];
		    }
		    for (int i = 0; i < problems.length; i++) {

		      if (problems[i].getAttribute(IMarker.MESSAGE).equals(
		        errorMessage)) {
		        exist = true;
		      }
		    }
		    if (!exist) {

		      IMarker marker = project.createMarker(AQLBuilder.COMPILE_MARKER_TYPE);
		      marker.setAttribute(IMarker.MESSAGE, errorMessage);
		      marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
		      marker.setAttribute(IMarker.SEVERITY, severity);
		    }
		  } catch (CoreException e) {
		    logger.logError(e.getMessage());
		  }
		}
	}

	protected ArrayList<String> getSearchPathList(String searchPath) {
		String[] tokens = searchPath.split(";"); //$NON-NLS-1$
		// separator.
		return new ArrayList<String>(Arrays.asList(tokens));
	}

	protected void refreshBinFolder(IFolder binPath) {
		if (project != null) {
			try {
				if(ProjectUtils.isModularProject(binPath.getProject())){
					  Display.getDefault().asyncExec(new Runnable() {
				      @Override
				      public void run() {
				        PlatformUI.getWorkbench().getDecoratorManager().
				        update(TEXT_ANALYTICS_FOLDER_DECORATOR_ID);
				      }
				    });
				}
				
	    		binPath.refreshLocal (IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				logger.logError(e.getMessage());
			}
		}

	}
	
	
	protected void delete(File file) {
	    if (file.exists()) {
	        File[] files = file.listFiles();
	        for (int i = 0; i < files.length; i++) {
	            if (files[i].isDirectory()) {
	                delete(files[i]);
	                files[i].delete();
	            } else {
	                files[i].delete();
	            }
	        }
	    }
	}


}
