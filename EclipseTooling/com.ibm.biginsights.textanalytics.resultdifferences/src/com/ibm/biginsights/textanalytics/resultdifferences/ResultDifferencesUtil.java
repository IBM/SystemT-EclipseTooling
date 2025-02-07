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
package com.ibm.biginsights.textanalytics.resultdifferences;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultdifferences.colldiff.CollectionDifferencesMainView;
import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileDifferencesView;
import com.ibm.biginsights.textanalytics.resultdifferences.filediff.FileSideBySideDifferencesView;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.resultviewer.util.ResultViewerUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;

public class ResultDifferencesUtil {


  
  private static Map<String,SystemTComputationResult> modelMap = new HashMap<String,SystemTComputationResult>();
	
	public static boolean isValidFile(IFile file)
	{
		if (file != null)
		{
			String fileExtension = file.getFileExtension();
			if (file.getName().startsWith(Constants.ALL_DOCS))
			{
				return false;
			}
			return (fileExtension.equals("strf") || fileExtension.equals(Constants.GS_FILE_EXTENSION)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return false;
	}

	public static boolean checkBothFilesAreAccessible(IFile[] strfFiles1, IFile[] strfFiles2)
	  {
		  for (int m=0;m<strfFiles1.length;m++)
		  {
			  if (strfFiles1[m].isAccessible() == false)
			  {
				  return false;
			  }
		  }
		  for (int m=0;m<strfFiles2.length;m++)
		  {
			  		  if (strfFiles2[m].isAccessible() == false)
			  {
				  return false;
			  }
		  }
		  return true;
	  }

	public static boolean checkFilesInBothFoldersAreAccessible(IFolder leftFolder, IFolder rightFolder) 
	  {
		return true;
/*		try
		{
		IResource[] resources = leftFolder.members();
	    for (IResource resource : resources) {
		      if ((resource.isAccessible() == false)){
				  return false;
		      }
		      if (resource instanceof IFile)
		      {
		    	  	IFile file2 = getRightFolderFile((IFile)resource,leftFolder,rightFolder);
				    if ((file2.isAccessible() == false)){
						  return false;
				    }
		      }
	    }
		}
		catch(CoreException e)
		{
			e.printStackTrace();
		}
	    return true;
*/	  }

	
	
	public static IFile getRightFolderFile(IFile leftFile, IFolder leftFolder, IFolder rightFolder) {

		IPath newFilePath = new Path(""); //$NON-NLS-1$
		// This replacing is done below because it is not just strf vs strf comparison that can happen
	
		String rightFile = leftFile.getName();
		if(GoldStandardUtil.isGoldStandardFolder(rightFolder))
		{
			// This is done because if it is gold standard folder, then all the file names would end in "lc"
			rightFile=  rightFile.replaceAll("strf", Constants.GS_FILE_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
		}
		newFilePath = rightFolder.getProjectRelativePath().append(newFilePath).append(rightFile);
		return rightFolder.getProject().getFile(newFilePath);
	}

	public static SystemTComputationResult getModelFromSTRFFile(IFile file)
	{
		if (file == null) return null;
	  String key = file.getFullPath().toString();
	  SystemTComputationResult result = null;
	  if( modelMap.containsKey (key) )
	  {
	    result= modelMap.get (key);
	  }
	  else
	  {
  
  	  try {
  	    InputStream fileContents =  file.getContents();
  	    Serializer serialzer = new Serializer();
  	    result = serialzer.getModelForInputStream(fileContents);
  	  } catch (CoreException e) {
  	    // TODO Auto-generated catch block
  	    e.printStackTrace();
  	  }
  	  modelMap.put(key,result);
	  }
	  return result;
	}
	
	public static void clearCache(){
		modelMap = new HashMap<String,SystemTComputationResult>();
	}
	
	
	public static ArrayList<String> getSuperSetNamesInBothFolders(IFolder leftFolder, IFolder rightFolder) 
	  {
		ArrayList<String> al = new ArrayList<String>();
		try
		{
			if (leftFolder != null)
			{
				IResource[] resources = leftFolder.members();
				IFile file = null;
				String name = null;
			    for (IResource resource : resources) {
				      if (resource instanceof IFile){
						  file = (IFile) resource;
						  if (ResultViewerUtil.isValidLCorSTRFFile(file))
						  {
							  name = removeExtension(file);
							  if (al.contains(name) == false)
							  {
								  al.add(name);
							  }
						  }
				      }
			    }
			}
			if (rightFolder != null)
			{
				IResource[] resources = rightFolder.members();
				IFile file =null;
				String name = null;
			    for (IResource resource : resources) {
				      if (resource instanceof IFile){
						  file = (IFile) resource;
						  if (ResultViewerUtil.isValidLCorSTRFFile(file))
						  {
							  name = removeExtension(file);
							  if (al.contains(name) == false)
							  {
								  al.add(name);
							  }
						  }
				      }
			    }
			}
		}
		catch(CoreException e)
		{
			e.printStackTrace();
		}
	    return al;
	  }
	
	
	public static String removeExtension(IFile file)
	{
		return file.getName().replaceAll("."+file.getFileExtension(),"");
	}

	
	public static void hidePrevCollDiffViewInSeparateThread() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				hidePrevCollDiffView();
			}
		});
	}

	public static void hidePrevCollDiffView() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		final IWorkbenchPage wbPage = PlatformUI.getWorkbench()
		.getActiveWorkbenchWindow().getActivePage();

		final CollectionDifferencesMainView view = (CollectionDifferencesMainView) window
				.getActivePage()
				.findView(CollectionDifferencesMainView.ID);
		wbPage.hideView(view);
		
		IViewReference[] viewRefs = window.getActivePage().getViewReferences();
	    for (IViewReference ref : viewRefs) {
	      if (ref.getId().equals(FileDifferencesView.ID)) {
	    	  wbPage.hideView(ref);
	      }
	      }
		
		
	    viewRefs = window.getActivePage().getViewReferences();
	    for (IViewReference ref : viewRefs) {
	      if (ref.getId().equals(FileSideBySideDifferencesView.ID)) {
	    	  wbPage.hideView(ref);
	      }
	      }
				
}

	public static IFile[] getSuperSetFilesInBothFolders(IFolder leftFolder, IFolder rightFolder) 
	  {
		ArrayList<IFile> al = new ArrayList<IFile>();
		try
		{
			if (leftFolder != null)
			{
				IResource[] resources = leftFolder.members();
				IFile file = null;
			    for (IResource resource : resources) {
				      if (resource instanceof IFile){
						  file = (IFile) resource;
						  if ((ResultViewerUtil.isValidLCorSTRFFile(file)) && (isFilePresentInArrayListContained(file, al) == false))
						  {
								  al.add(file);
						  }
				      }
			    }
			}
			if (rightFolder != null)
			{
				IResource[] resources = rightFolder.members();
				IFile file =null;
			    for (IResource resource : resources) {
				      if (resource instanceof IFile){
						  file = (IFile) resource;
						  if ((ResultViewerUtil.isValidLCorSTRFFile(file)) && (isFilePresentInArrayListContained(file, al) == false))
						  {
							  al.add(file);
						  }
				      }
			    }
			}
		}
		catch(CoreException e)
		{
			e.printStackTrace();
		}
		IFile[] filArray = new IFile[al.size()];
	    return al.toArray(filArray);
	  }
	
private  static boolean isFilePresentInArrayListContained(IFile file , ArrayList<IFile> al)
{
	Iterator<IFile> iter = al.iterator();
	IFile alFile = null;
	String alFileName = null;
	String fileName = null;
	while (iter.hasNext())
	{
		alFile = iter.next();
		alFileName = removeExtension(alFile);
		fileName = removeExtension(file);
		if (alFileName.equals (fileName))
		{
			return true;
		}
	}
	return false;
}


}
