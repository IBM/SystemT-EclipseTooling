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
package com.ibm.biginsights.textanalytics.refinement.run;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.MessageConsoleStream;

import com.ibm.avatar.algebra.function.scalar.AutoID;
import com.ibm.avatar.api.DocReader;
import com.ibm.avatar.provenance.AQLProvenanceRewriter;
import com.ibm.avatar.provenance.AQLRefine;
import com.ibm.avatar.provenance.RefineEvaluator;
import com.ibm.biginsights.textanalytics.nature.prefs.SystemTProperties;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.refinement.Activator;
import com.ibm.biginsights.textanalytics.refinement.command.RefinerContainer;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.FileUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;
import com.ibm.biginsights.textanalytics.util.provenance.ProvenanceUtils;

public class RefinerJob extends Job {


	
	private RefinerContainer params;
	private MessageConsoleStream messageStream;

	public RefinerJob(String name, RefinerContainer params, MessageConsoleStream out) {
		super(name);
		this.params = params;
		this.messageStream = out;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		// Log INFO
		String message = String.format(
				"Running Refinement Job !!!");
		LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logInfo(message);
		
		try{
			
			String projectName = params.getProperty(Constants.REFINER_PROJECT_NAME_PROP);
			SystemTProperties projectProps = ProjectPreferencesUtil.getSystemTProperties(projectName);

			String viewName = params.getProperty(Constants.REFINER_VIEW_NAME_PROP);
			
			String absAQLFile = ProjectPreferencesUtil.getAbsolutePath(projectProps.getMainAQLFile());
			String absSearchPath = ProjectPreferencesUtil.getAbsolutePath(projectProps.getSearchPath());
			String absAOGPath = ProjectPreferencesUtil.getAbsolutePath(projectProps.getAogPath());			
			String absResultDir = ProjectPreferencesUtil.getAbsolutePath(projectProps.getResultDir());
			String absConfigPath = ProjectPreferencesUtil.getAbsolutePath(params.getProperty(Constants.REFINER_CONFIG_PATH_PROP));
			String absDatasetPath = ProjectPreferencesUtil.getAbsolutePath(params.getProperty(Constants.REFINER_DATA_PATH_PROP));
			String absLabelPath = ProjectPreferencesUtil.getAbsolutePath(params.getProperty(Constants.REFINER_LABEL_PATH_PROP));
			
			System.err.printf("AQL absolute path: '%s'\n", absAQLFile);			
			System.err.printf("Search absolute path: '%s'\n", absSearchPath);
			System.err.printf("AOG absolute path: '%s'\n", absAOGPath);
			System.err.printf("Result absolute path: '%s'\n", absResultDir);
			System.err.printf("Configuration absolute path: '%s'\n", absConfigPath);
			System.err.printf("Dataset absolute path: '%s'\n", absDatasetPath);
			System.err.printf("Labels absolute path: '%s'\n", absLabelPath);

			// Rewrite the input AQL
			AutoID.resetIDCounter();
			
			AQLRefine refiner = new AQLRefine();
			AQLRefine.setDEBUG(false);
			refiner.setProperties(FileUtils.loadProperties(absConfigPath));
					
			DocReader docs = new DocReader(new File(absDatasetPath));
			
			// Get the content of the main AQL file that we rewrite    	
		    String aql = FileUtils.fileToStr(new File(absAQLFile), Constants.ENCODING);
		    	
		    // Perform the rewrite
		    messageStream.println("Initializing ...");
		    AQLProvenanceRewriter rewriter = new AQLProvenanceRewriter();
		    String rewrittenAQL = rewriter.rewriteAQLString(aql, absSearchPath, refiner.getBaseViews());
		    refiner.addBaseViews(rewriter.getBaseViews());
			
			// Read the provenance AOG from disk
			File aogFile = ProvenanceUtils.getRewrittenAOGFile(absAOGPath);
			String operatorGraphStr = FileUtils.fileToStr(aogFile, Constants.ENCODING);
			
			messageStream.print("Reading training data ...");
			RefineEvaluator eval = new RefineEvaluator(operatorGraphStr, viewName, docs);
			eval.readGoldenStandard(absLabelPath);

			refiner.autoRefine(rewrittenAQL, rewriter, viewName, absSearchPath, absSearchPath, absSearchPath, absDatasetPath, eval, false, messageStream);
			
			refiner = null;
			messageStream.println("\nAQL Refinement DONE.\n\n");
			messageStream.flush();
			messageStream.close();			
		}
		catch(Exception e){
			message = String.format(
					"Problem encountered during AQL refinement.");

			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message, e);
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowStatus(status);
			
			return status;
		}
		
		return Status.OK_STATUS;
	}

}
