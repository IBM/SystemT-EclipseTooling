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
package com.ibm.biginsights.textanalytics.goldstandard.handler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import com.ibm.biginsights.textanalytics.goldstandard.GoldStandardPlugin;
import com.ibm.biginsights.textanalytics.goldstandard.Messages;
import com.ibm.biginsights.textanalytics.goldstandard.handler.util.AnnotationTypesImporter;
import com.ibm.biginsights.textanalytics.goldstandard.util.GoldStandardUtil;
import com.ibm.biginsights.textanalytics.resultviewer.model.FieldValue;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputView;
import com.ibm.biginsights.textanalytics.resultviewer.model.OutputViewRow;
import com.ibm.biginsights.textanalytics.resultviewer.model.Serializer;
import com.ibm.biginsights.textanalytics.resultviewer.model.SpanVal;
import com.ibm.biginsights.textanalytics.resultviewer.model.SystemTComputationResult;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.IFolderFilter;
import com.ibm.biginsights.textanalytics.util.common.MessageUtil;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.common.StringUtils;
import com.ibm.biginsights.textanalytics.util.common.ui.FilteredFileDirectoryDialog;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 * This class handles the click event on popup menu item 'Labeled Document Collection -> Import from extraction result'
 * on a gsParentFolder or Project from the project explorer or package explorer.
 * 
 * This class imports the specified result directory into labeledCollections directory.
 * 
 *  Krishnamurthy
 *
 */
public class ExplorerImportResultHandler extends GSActionHandler implements IHandler {



	private Serializer ser = new Serializer();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (super.execute(event) == null) {
			return null;
		}
		
		IFolder resultRootDir = ProjectUtils.getRootResultFolder(selectedProject);
		boolean resultExists = true;
		if(resultRootDir == null || !resultRootDir.exists()){
			resultExists = false;
		}else{
			try {
				//find atleast one subfolder of 'result' folder that begins with 'result'
				IResource[] res = resultRootDir.members();
				resultExists = false;
				for (IResource iResource : res) {
					if(iResource instanceof IFolder){
						IFolder folder = (IFolder) iResource;
						if(ProjectUtils.isResultFolder(folder)){
							resultExists = true;
							break;
						}
					}
				}
			} catch (CoreException e) {
				resultExists = false;
			}
		}
		
		if(!resultExists){
			LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
					Messages.ImportResultActionDelegate_NoExtractorResults);
			return null;
		}
		
		FilteredFileDirectoryDialog dirDialog = new FilteredFileDirectoryDialog(
				ProjectUtils.getActiveWorkbenchWindow().getShell(),
				new WorkbenchLabelProvider(), new WorkbenchContentProvider(), Constants.DIRECTORY_ONLY);
		dirDialog.setTitle(Messages.BootstrapAQLResultHandler_BOOTSTRAP_FROM_RESULT);
		dirDialog.setMessage(Messages.BootstrapAQLResultHandler_SELECT_RESULT_DIR);
		dirDialog.setInput(resultRootDir); //$NON-NLS-1$
		dirDialog.setFolderFilter(new IFolderFilter() {
			
			@Override
			public boolean allowFolder(IFolder folder) {
				return ProjectUtils.isResultFolder(folder);
			}
		});
		
		dirDialog.setContextHelpId("com.ibm.biginsights.textanalytics.tooling.help.import_extraction_result");
		
		IFolder resultDir = (IFolder) dirDialog.getSelectedResource();
		if(resultDir != null ){
			Job systemtJob = new ImportResultJob(Messages.BootstrapAQLResultHandler_INFO_IMPORTING_FROM_RESULT, gsParentFolder,resultDir);
			systemtJob.setUser(true);
			systemtJob.schedule();
		}
		
		return null;

	}
	
	/**
	 * The ImportResultJob class executes the import task in a separate job thread
	 * so that the main UI is not blocked.
	 * 
	 * 
	 *
	 */
	private class ImportResultJob extends Job {

		IFolder gsParentFolder;
		IFolder resultDir;
		IProgressMonitor progressMonitor;
		int totalWork;

		public ImportResultJob(String name, IFolder gsParentFolder, IFolder resultDir) {
			super(name);
			this.gsParentFolder = gsParentFolder;
			this.resultDir = resultDir;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			this.progressMonitor = monitor;
			
			try {
				IFolder gsFolder = GoldStandardUtil.createDefaultGSFolder(gsParentFolder, progressMonitor);
				boolean success = importFromResultFolder(gsFolder);
				if(!success){
					gsFolder.delete(true, progressMonitor);
					return Status.CANCEL_STATUS;
				}
				gsParentFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(
						MessageUtil.formatMessage(
								Messages.ImportResultActionDelegate_ImportResultSuccessful,
								gsFolder.getFullPath().toString()));
				
				new AnnotationTypesImporter(gsFolder, progressMonitor).run();
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
			}
			return Status.OK_STATUS;
		}

		private boolean importFromResultFolder(IFolder gsFolder) throws CoreException{
			StringBuilder alreadyExistingFiles = new StringBuilder();
		
			totalWork = resultDir.getLocation().toFile().list().length;
			progressMonitor.beginTask(MessageUtil.formatMessage(
					Messages.BootstrapAQLResultHandler_IMPORTING_FROM_RESULT_DIR, 
					resultDir.getName()), 
					totalWork);
			IResource contents[] = null;
			try {
				contents = resultDir.members();
			} catch (CoreException e) {
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logError(e.getMessage());
			}
			for (IResource resource : contents) {
				if(!progressMonitor.isCanceled()){
					if(resource instanceof IFile){
						if(Constants.STRF_FILE_EXTENSION.equals(resource.getFileExtension())){
							if(isValidResultFile((IFile)resource)){
								String ext = Constants.STRF_FILE_EXTENSION_WITH_DOT; //$NON-NLS-1$
								String name = resource.getName();
								String newName = name.replace(ext, Constants.GS_FILE_EXTENSION_WITH_DOT);
								progressMonitor.subTask(MessageUtil.formatMessage(Messages.BootstrapAQLResultHandler_IMPORTING_FILE, newName));
								Path path = new Path(gsFolder.getFullPath()+ "/"+newName); //$NON-NLS-1$
								try {
									replaceNonSpanTagsAndCopy(resource,gsFolder.getFile(newName));
									gsFolder.getFile(newName).setCharset(Constants.ENCODING, null);
								}catch (CoreException e) {
									if(e.getMessage().contains("already exists")){ //$NON-NLS-1$
										alreadyExistingFiles.append(newName).append(" "); //$NON-NLS-1$
									}else{
										LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(e.getLocalizedMessage());
									}
								}//end: catch CoreException
							}//end: if validResultFile
							else{
								LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowError(
										Messages.ExplorerImportResultHandler_RESULT_NOT_IMPORTED_SINCE_IT_CONTAINS_NON_SPAN_FIELDS);
								return false;
							}
						}//end if STRF file
					}//end: if resource instanceof IFile
				}//end: if progressMonitor is not cancelled
				progressMonitor.worked(1);
			}//end: for each resource
			if(!StringUtils.isEmpty(alreadyExistingFiles.toString())){
				LogUtil.getLogForPlugin(GoldStandardPlugin.PLUGIN_ID).logAndShowInfo(
						MessageUtil.formatMessage(Messages.BootstrapAQLResultHandler_LIST_OF_FILES_NOT_IMPORTED, alreadyExistingFiles.toString() ));
			}

			return true;
		}
		
		/**
		 * When importing result files which have non-span tags as a labeled collection, we will remove all non-span tags so as to not cause incomplete annotation error.
		 * This has been done for defect 17933. This method uses an utility method to remove te nonspan tags and then serializes the model as a .lc file
		 * @param resultFile
		 * @param gsFile
		 * @throws CoreException
		 */
		
		private void replaceNonSpanTagsAndCopy(IResource resultFile,IFile gsFile) throws CoreException
		{
			IFile file = (IFile)resultFile;
			SystemTComputationResult model = ser.getModelForInputStream(file.getContents());
			GoldStandardUtil.removeNonSpanTags(model);			
			ser.writeModelToFile(gsFile, model);
		}

		/**
		 * Marks a result file as invalid, if it contains any row of type SPAN that is not over Document.text. However, result files with non-SPAN rows are treated as valid, 
		 * because the result importer takes care of skipping non-SPAN rows while importing a result file into labeled collection. However, this method requires a rewrite in future, 
		 * when we start to support import of SPANs that are not over Document.text. (eg. detagged documents). See defect 17933 for more.
		 * @param resource
		 * @return
		 * @throws CoreException
		 */
    private boolean isValidResultFile (IFile resource) throws CoreException
    {
      SystemTComputationResult model = ser.getModelForInputStream (resource.getContents ());
      OutputView[] views = model.getOutputViews ();
      if (views != null) {
        for (OutputView view : views) {
          // check : If any row has a SPAN over non Document.text parentScope, then treat the collection as invalid
          OutputViewRow[] rows = view.getRows ();
          if (rows != null) {
            for (OutputViewRow row : rows) {
              FieldValue[] fVals = row.fieldValues;
              if (fVals != null) {
                for (FieldValue fVal : fVals) {
                  if (fVal instanceof SpanVal) {
                    SpanVal spanVal = (SpanVal) fVal;
                    if (!"Document.text".equals (spanVal.parentSpanName)) { //$NON-NLS-1$
                      return false;
                    }
                  }
                }// end: for each fVal
              }// end: if fVals != null
            }// end: foreach row
          }// end: if rows != null
        }// end: for each view
      }// end: if views != null

      return true;
    }
	}//end of job class
	

}
