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
package com.ibm.biginsights.textanalytics.aql.library;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.avatar.aql.ParseException;

/**
*
*  Babbar
* 
*/
public class AQLParseErrorHandler {



	public static final String AQL_FILE_EXTENSION = ".aql";
	public static final String PARSE_MARKER_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.parseerror";
	public static final String COMPILE_MARKER_TYPE = "com.ibm.biginsights.textanalytics.aqleditor.compileerror";

	public AQLParseErrorHandler()
	{
	
	}
	
	/*
     * This method handles the ParseException by searching for patterns with line number and column
     * number and uses that info to populate the marker info
     * 
     * @param pe
     * 
     * @param severity
     */
    public void handleError(ParseException pe, int severity, IFile aqlFile) {
      //Assuming that the ParseException instance provided will always have the correct error location details
      addMarker(aqlFile, pe.getErrorDescription (), pe.getLine (), severity);
    }
    
   /*
     * This is the method that actually creates the marker and adds them to the file
     * 
     * @param file
     * 
     * @param message
     * 
     * @param lineNumber
     * 
     * @param severity
     */
    private void addMarker(IFile file, String message, int lineNumber, int severity) {
       try {
    	    	//putting a check to see if the same marker exist from the compiler,
    	    	//if yes then return otherwise create the marker
    	    	boolean exist = false;
    	    	IMarker[] problems = null;
    	  	  	int depth = IResource.DEPTH_INFINITE;
    	  	  	try {
    	  	  		problems = file.findMarkers(COMPILE_MARKER_TYPE, true, depth);
    	  	  	} catch (CoreException e) {
    	  	     // something went wrong
    	  	  	}
    	  	  
    	  	  	if(problems != null)
    	  	  	{
    	  	  		for(int i = 0;i<problems.length;i++)
    	  	  		{
    	  	  			if(problems[i].getAttribute(IMarker.MESSAGE).equals(message))
    	  	  			{
    	  	  				exist = true;
    	  	  			}				
    	  	  		}
    	  	  	}
    	  	  	if(!exist)
    	  	  	{
    	    	
    	  	  	IMarker marker = file.createMarker(PARSE_MARKER_TYPE);
    	        marker.setAttribute(IMarker.MESSAGE, message);
    	        marker.setAttribute(IMarker.SEVERITY, severity);
    	        if (lineNumber == -1) {
    	          lineNumber = 1;
    	        }        
    	        marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
    	    	}
    	    	} catch (CoreException e) {
              Messages.LogErrorMessage (Messages.ERROR_CANT_ADD_MARKERS, e);
    	    	}
    	   }
    
    public void deleteMarkers(IFile file) {
        try {
          if (file != null) {
            // Choosing the DEPTH_ONE because we want to delete only the
            // problem markers in the
            // current file before parsing again
        	  //file.getProject().findMarkers(null, false, 0);
        	  
        	  IMarker[] problems = null;
        	  int depth = IResource.DEPTH_INFINITE;
        	  try {
        	     problems = file.findMarkers(PARSE_MARKER_TYPE, true, depth);
        	  } catch (CoreException e) {
        	     // something went wrong
        	  }
        	  
        	  if(problems != null)
        	  {
        		  for(int i = 0;i<problems.length;i++)
        		  {
        			  problems[i].delete();
        		  }
        	  }
        	  
        	  //file.deleteMarkers(PARSE_MARKER_TYPE, false, IResource.DEPTH_ONE);
          }
        } catch (CoreException ce) {
          Messages.LogErrorMessage (Messages.ERROR_CANT_DELETE_MARKERS, ce);
        }
      }
    
    public void deleteAqlMarkers (IFile file)
    {
      try {
        if (file != null) {
          int depth = IResource.DEPTH_INFINITE;
          file.deleteMarkers (PARSE_MARKER_TYPE, true, depth);
          file.deleteMarkers (COMPILE_MARKER_TYPE, true, depth);
        }
      }
      catch (CoreException ce) {
        Messages.LogErrorMessage (Messages.ERROR_CANT_DELETE_MARKERS, ce);
      }
    }

    public void setIgnoreAqlWarning (IFile file)
    {
      List<IMarker> mrkrs = getIgnoreAqlWarningMarkers (file);
      if (mrkrs.isEmpty ())
        addMarker(file, Messages.WRN_AQL_FILE_NOT_COMPILED, 0, IMarker.SEVERITY_WARNING);
    }

    public void removeIgnoreAqlWarning (IFile file)
    {
      try {
        List<IMarker> mrkrs = getIgnoreAqlWarningMarkers (file);
        for (IMarker m : mrkrs) {
          m.delete ();
        }
      }
      catch (CoreException ce) {
      }
    }

    private List<IMarker> getIgnoreAqlWarningMarkers (IFile file)
    {
      List<IMarker> mrkrs = new ArrayList<IMarker> ();
      try {
        IMarker[] markers = file.findMarkers(PARSE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        for (IMarker m : markers) {
          if (m.getAttribute (IMarker.MESSAGE).equals (Messages.WRN_AQL_FILE_NOT_COMPILED)) 
            mrkrs.add (m);
        }

        markers = file.findMarkers(COMPILE_MARKER_TYPE, true, IResource.DEPTH_INFINITE);
        for (IMarker m : markers) {
          if (m.getAttribute (IMarker.MESSAGE).equals (Messages.WRN_AQL_FILE_NOT_COMPILED)) 
            mrkrs.add (m);
        }
      }
      catch (CoreException ce) {
      }

      return mrkrs;
    }
}
