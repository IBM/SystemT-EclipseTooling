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
package com.ibm.biginsights.textanalytics.resultviewer.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * Utility class created to cater to common methods used across all the three result views - AnnotationExplorer, TableView and TreeView
 * 
 *  Madiraju
 *
 */
public class ResultViewerUtil {



	/**
	 * This utility method returns the result folder given the temp directory path (usually stored in the concordancemodel) 
	 * @param tempDirPath
	 * @return
	 */
	public static IFolder getResultFolder(String tempDirPath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFolder tempFolder = workspace.getRoot().getFolder(
				new Path(tempDirPath));
		IFolder resultFolder = (IFolder) tempFolder.getParent();
		return resultFolder;
	}

	/**
	 * This utility method returns the temp folder given the temp directory path (usually stored in the concordancemodel) 
	 * @param tempDirPath
	 * @return
	 */
	public static IFolder getTempFolder(String tempDirPath) {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IFolder tempFolder = workspace.getRoot().getFolder(
				new Path(tempDirPath));
		return tempFolder;
	}

	/**
	 * Each input document file has a corresponding serialized .strf file. This
	 * method is a utility method to get the STR model from the input filename
	 * 
	 * @param inputFileName
	 * @param extn 
	 * @return
	 */
	public static SystemTComputationResult getResultFromFileName(IFolder resultFolder,
			String inputFileName, String extn,String docSchema) {
		try {
			// First normalize the file name because thats how the strf file
			// would
			// have been created during the run
			String nInputFileName = StringUtils
					.normalizeSpecialChars(inputFileName);
			Serializer srlzr = new Serializer();
			IFile file = resultFolder.getFile(new Path(nInputFileName + extn));
			SystemTComputationResult returnResult = srlzr.getModelForInputStream(file.getContents());
			returnResult.setInputTextID(returnResult.getInputTextIDForThisSchema(docSchema));
			return returnResult;
		} catch (CoreException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	/**
	 * For display on to the text editor a temp file with bitatext extension is
	 * created so that it can be displayed on the eclipse text editor. This is
	 * utility method to do that.
	 * 
	 * @param text
	 * @param fileMod
	 * @param tmpDir
	 * @return
	 * @throws CoreException
	 * @throws IOException
	 */

	  public static IFile writeTempFile(String text, String fileMod, String tmpDir) throws CoreException,
      IOException {
	    if (text == null) {
	      return null;
	    }
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    IFolder tmpFolder = workspace.getRoot().getFolder(new Path(tmpDir));
	    if (!tmpFolder.exists()) {
	    	if (tmpFolder.getParent().isAccessible())
	    	{
	    		tmpFolder.create(IResource.HIDDEN, true, new NullProgressMonitor());
	    	}
	    	else
	    	{
				String msg = Messages.parentResultFolderDoesNotExist;
				String formattedMsg = MessageUtil.formatMessage(msg, tmpFolder.getParent().getFullPath().toString());
	
				LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(formattedMsg); 
				return null;
	    	}
	    }
	    // use the unique text id from SystemT as doc ID
	    String fileName = fileMod + Messages.resultTextFileExtension;
	    IFile file = tmpFolder.getFile(fileName);
	    if (file.exists()) {
	      return file;
	    }
	    byte[] bytes = text.getBytes(Constants.ENCODING);
	    InputStream is = new ByteArrayInputStream(bytes);
	    file.create(is, true, new NullProgressMonitor());
	    return file;
  }

		/**
		 * This is an utility method used to remove all annotations from a model.
		 * Useful when comparison of files that don't exist from the ResultDifferences module.
		 * Used to simulate the case of 0 annotations - for calculations of all differences - 
		 * missing, spurious,and all other LC measures.
		 * @param model
		 */
		public static SystemTComputationResult stripAllAnnotations(SystemTComputationResult model) {		
			SystemTComputationResult returnedModel = new SystemTComputationResult();
		      returnedModel.setDocumentID(model.getDocumentID());
		      returnedModel.setInputTextID(model.getInputTextID());
		      returnedModel.setTextMap(model.getTextMap());

		      OutputView[] opViews = model.getOutputViews();
		      if (opViews != null)
		      {
			      OutputView[] retViews = new OutputView[opViews.length];
			      OutputViewRow[] emptyRow = new OutputViewRow[0];
			      for (int j=0; j< opViews.length;j++)
			      {
			    	  OutputView view = opViews[j];
			    	  OutputView retView = new OutputView();
			    	  retView.setFieldNames(view.getFieldNames());
			    	  retView.setFieldTypes(view.getFieldTypes());
			    	  retView.setName(view.getName());
			    	  retView.setRows(emptyRow);
			    	  retViews[j]=retView;
			      }
			      returnedModel.setOutputViews(retViews);
		      }
   			  return returnedModel;
		}

		public static boolean isValidLCorSTRFFile(IFile file)
		{
			if (file != null)
			{
				if (file.getName().startsWith(Constants.ALL_DOCS))
				{
					return false;
				}
				String fileExtension = file.getFileExtension();
				if ((fileExtension.equals(Constants.STRF_FILE_EXTENSION) || fileExtension.equals(Constants.GS_FILE_EXTENSION)))
				{
					return true;
				}
			}
			return false;
		}

}
