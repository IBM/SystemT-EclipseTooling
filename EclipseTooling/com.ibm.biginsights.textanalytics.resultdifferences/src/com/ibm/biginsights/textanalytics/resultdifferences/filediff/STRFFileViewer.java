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
package com.ibm.biginsights.textanalytics.resultdifferences.filediff;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;

public class STRFFileViewer   {



	private String getBaseTextFromSTRFFile(IFile file)
	{
		try {
			InputStream fileContents =  file.getContents();
			Serializer serialzer = new Serializer();
			SystemTComputationResult result = serialzer.getModelForInputStream(fileContents);
			return result.getInputText();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ""; //$NON-NLS-1$
	}
  static public STRFFileViewer callSTRFFileViewer(String strfFile,
       String title, boolean sharedView) {
	  
    IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
    STRFFileViewer viewer = null;
	
    return viewer;
  }

}
