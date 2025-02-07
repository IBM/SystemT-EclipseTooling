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
package com.ibm.biginsights.textanalytics.aql.editor.hover;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;

import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.aql.tam.ModuleUtils;
import com.ibm.biginsights.textanalytics.aql.editor.Activator;
import com.ibm.biginsights.textanalytics.aql.editor.assist.CurrentWordFinder;
import com.ibm.biginsights.textanalytics.aql.editor.common.FileTraversal;
import com.ibm.biginsights.textanalytics.aql.editor.ui.AQLPartitionScanner;
import com.ibm.biginsights.textanalytics.aql.library.AQLElement;
import com.ibm.biginsights.textanalytics.aql.library.AQLModule;
import com.ibm.biginsights.textanalytics.aql.library.AQLModuleProject;
import com.ibm.biginsights.textanalytics.aql.library.IAQLLibrary;
import com.ibm.biginsights.textanalytics.indexer.types.ElementType;
import com.ibm.biginsights.textanalytics.indexer.util.IndexerUtil;
import com.ibm.biginsights.textanalytics.nature.utils.ProjectPreferencesUtil;
import com.ibm.biginsights.textanalytics.util.common.Constants;
import com.ibm.biginsights.textanalytics.util.common.ProjectUtils;
import com.ibm.biginsights.textanalytics.util.log.LogUtil;

/**
 *  Displays element comment when hovered over the element.
 * 
 * 
 */
public class AQLDocHover implements ITextHover, ITextHoverExtension2 {


	
	IFileEditorInput input;
	String currentFileLocation;
	IFile currentFile;
	IProject currPorject;
	IAQLLibrary aqlLibrary;
	IRegion prevRegion;
	IDocument doc;
	IRegion prev2Region;
	Region region;

	final String EMPTY_STRING = ""; //$NON-NLS-1$ 
	final String SEMICOLON_STRING = ";"; //$NON-NLS-1$
	final String FROM_STRING = "from"; //$NON-NLS-1$
	final String REGEX_STRING = "regex"; //$NON-NLS-1$
	String lastWord = EMPTY_STRING;
	String currWord = EMPTY_STRING;
	String contentType;

	ArrayList<String> searchPathList;
	Set<String> createdViews;
	List<String> tamViews = new ArrayList<String>();
	Set<String> fileSet = new HashSet<String>();
	ArrayList<String> fileList = new ArrayList<String>();
	Set<String> moduleFileSet = new HashSet<String>();
	ArrayList<String> moduleFileList = new ArrayList<String>();

	Set<String> tamFileSet;
	boolean isModularProject;
	

	@Override
	public String getHoverInfo2(ITextViewer viewer, IRegion region) {
		fileSet.clear();
		moduleFileSet.clear();
		fileList.clear();
		moduleFileList.clear();

		// As this would not be part of the active UI thread, making an asyncExec call to get the Active page.
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
			  IEditorInput genericInput = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage()
            .getActiveEditor().getEditorInput();
			  if (!(genericInput instanceof IFileEditorInput)) {
			    return;
			  }
				input = (IFileEditorInput) genericInput;
				currentFile = input.getFile();
				currentFileLocation = currentFile.getLocation().toOSString();
				currPorject = input.getFile().getProject();
			}
		});
		while(currPorject == null) {
		  // Waiting till currPorject gets populated in the above thread
		  try {
        Thread.sleep (5);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
		}
    
		// DocHover is supported from 2.0, if it is non modular project then return an empty string.
		isModularProject = ProjectUtils.isModularProject(currPorject);
		if (!isModularProject) {
			return "";
		} else {
		  //Loading the AQL Library
			aqlLibrary = Activator.getModularLibrary();
		}

		searchPathList = new ArrayList<String>();

		searchPathList = getModularProjectSearchPathList(currPorject);
		// get the module for AQL file..
		IFolder moduleFolder = ProjectUtils.getModule4AqlFile(currentFile);

		if(moduleFolder == null || !moduleFolder.exists ())
		  return "";
		
		String modulePath = moduleFolder.getLocation().toOSString();
		ArrayList<String> moduleFilePathList = getModuleSearchPathList(
				currPorject, modulePath);
		moduleFileSet = getAllCurrModuleFiles(moduleFilePathList);
		moduleFileList.addAll(moduleFileSet);

		fileSet = getAllAQLFiles(searchPathList);
		fileList.addAll(fileSet);
		Collections.swap(fileList, 0, fileList.indexOf(currentFileLocation));
		
		// Calculating the current word based on the region offset.
		doc = viewer.getDocument();
		int offSet = region.getOffset();
		try {
			ITypedRegion tRegion = doc.getPartition(region.getOffset());
			//System.out.println("Character at this offset is ***************************** The offset value is  "+region.getOffset()+"  The character at the given offset is  "+doc.getChar(region.getOffset()));
			if (region != null) {
				if ((tRegion.getType() == AQLPartitionScanner.AQL_COMMENT)) {
					currWord = EMPTY_STRING;
					return "";
				} else {
					currWord = doc.get(offSet,
							region.getLength());
				}
			}
      char doubleQuotes;
      if (offSet > 0) {
        doubleQuotes = doc.getChar (offSet-1);
        // If the hovered string begins with double quotes, then for the indexer to identify the element its begin offset needs to be modified.
        if (doubleQuotes == '\"') {
          offSet = offSet-1;
        }
      }  
			}catch (BadLocationException exp) {
					LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
						Constants.UNABLE_TO_PROCESS_AQL_DOC_COMMENT,
						exp);
					exp.printStackTrace();
			}
		// Getting the ElementType for the token at the given offset. 
		ElementType eType = IndexerUtil.detectElementType(currentFile, offSet);
		if (eType == null || eType.equals(ElementType.UNKNOWN)) {
			return "";
		}
		
		//Based on the ElementType, we get the associated doc comment for those elements.
		if (eType.equals(ElementType.VIEW) || eType.equals(ElementType.EXTERNAL_VIEW)) {
			return getComment(ElementType.VIEW);
		} else if (eType.equals(ElementType.DICTIONARY)){
			return getComment(ElementType.DICTIONARY);
		} else if (eType.equals(ElementType.TABLE)){
			return getComment(ElementType.TABLE);
		} else if (eType.equals(ElementType.FUNCTION)){
			return getComment(ElementType.FUNCTION);
		} else if (eType.equals(ElementType.MODULE)){
			return getModuleComments();
		} 
		
		return "";
	}

	private String getComment(ElementType view) {
		List<AQLElement> elements = null;
		
		//Based on the Element Type fetch the corresponding Element lists from the AQLLibrary
		if (view.equals(ElementType.VIEW)) {
			elements = aqlLibrary.getViews(fileList);
			elements.addAll (aqlLibrary.getSelects (fileList));
			elements.addAll(aqlLibrary.getExternalViews(fileList));
		} else if (view.equals(ElementType.DICTIONARY)) {
			elements = aqlLibrary.getDictionaries(fileList);
			elements.addAll(aqlLibrary.getExternalDictionaries(fileList));
		} else if (view.equals(ElementType.TABLE)) {
			elements = aqlLibrary.getTables(fileList);
			elements.addAll(aqlLibrary.getExternalTables(fileList));
		} else if (view.equals(ElementType.FUNCTION)) {
			elements = aqlLibrary.getFunctions(fileList);
		} 
		
		// Iterate through the AQLElement lists, to get the comment for the selected Element
		if (elements != null) {
			Iterator<AQLElement> eleItr = elements.iterator();
			while (eleItr.hasNext()) {
				AQLElement elmt = eleItr.next();
				String typeName = elmt.getName();
				String aliasName = elmt.getAliasName();
				// To handle the cases where we have moduleName.viewName
					String modulePrefix = elmt.getModuleName().concat(".");
				if (typeName.startsWith(modulePrefix))
					typeName = typeName.replace(modulePrefix, "");
				if (typeName.equals(currWord)) {
					return elmt.getComment();
				}
				if (aliasName != null && aliasName.equals(currWord)) {
					return elmt.getComment();
				}
			}

		}
		
		// If the selected AQLElement is not present in the source files, then we scan through the TAM files.
			ModuleMetadata[] tamMetaData = ProjectPreferencesUtil.getModuleMetadata(currPorject);
				for (int i = 0; i < tamMetaData.length; i++) {
					tamViews.clear();
					try {
					  
						if (view.equals(ElementType.VIEW)) {
							return tamMetaData[i].getComment(com.ibm.avatar.api.tam.ModuleMetadata.ElementType.VIEW, ModuleUtils.prepareQualifiedName (tamMetaData[i].getModuleName (),currWord));
						} else if (view.equals(ElementType.DICTIONARY)) {
							return tamMetaData[i].getComment(com.ibm.avatar.api.tam.ModuleMetadata.ElementType.DICTIONARY, ModuleUtils.prepareQualifiedName (tamMetaData[i].getModuleName (),currWord));
						} else if (view.equals(ElementType.TABLE)) {
							return tamMetaData[i].getComment(com.ibm.avatar.api.tam.ModuleMetadata.ElementType.TABLE, ModuleUtils.prepareQualifiedName (tamMetaData[i].getModuleName (),currWord));
						} else if (view.equals(ElementType.FUNCTION)) {
							return tamMetaData[i].getComment(com.ibm.avatar.api.tam.ModuleMetadata.ElementType.FUNCTION, ModuleUtils.prepareQualifiedName (tamMetaData[i].getModuleName (),currWord));
						}
					} catch (Exception e) {
						// element of the given type with the given name does not exist.
						return "";
					}
				}
		return "";
	}

	private String getModuleComments() {
		AQLModule module;
		//Checking if the module src is available and retrieving the comment.
		module = getAQLModuleFromCurrAndRefProjects(currPorject, currWord);
		if (null != module)
			return module.getComment();
		//If the module src is not available then scanning through the TAM files.
		ModuleMetadata[] tamMetaData = ProjectPreferencesUtil.getModuleMetadata(currPorject);
		for (int i = 0; i < tamMetaData.length; i++) {
			String mName = tamMetaData[i].getModuleName();
			//tamMetaData[i].getComment(type, name)
			if (mName.equals(currWord)) {
				return tamMetaData[i].getComment();
			}
		}
		return "";
	}

	@Override
	public IRegion getHoverRegion(ITextViewer tv, int off) {
		return getCurrentRegion(tv.getDocument(), off);
	}

	public IRegion getCurrentRegion(IDocument doc, int offset) {
		return CurrentWordFinder.findCurrWord (doc, offset);
	}

	public IRegion getLastRegion(IDocument doc, int offset) {
		return CurrentWordFinder.findLastWord(doc, offset);
	}

	// This method returns the search path for modular type projects..
	private String getAllSearchPaths(IProject project, String moduleSrcPath) {
		if (moduleSrcPath == null) {
			return null;
		}
		String finalSearchPath = "";
		String currPath = "";
		String[] moduleNames = ProjectUtils.getModules(project);
		if (moduleNames.length > 0) {
			for (String moduleName : moduleNames) {
				//currPath = moduleSrcPath + File.separator + moduleName + ";"; //$NON-NLS-1$
				currPath = String.format("%s/%s;", moduleSrcPath, moduleName);
				finalSearchPath = finalSearchPath + currPath;
			}
		}
		return finalSearchPath;
	}

	// This method returns the search path list for the given module.
	private ArrayList<String> getModuleSearchPathList(IProject project, String modName)
	{
    ArrayList<String> pathList = new ArrayList<String> ();

    if (project != null && project.getLocation () != null) {
      String projectName = project.getLocation ().toOSString ();
      Set<String> aqlFilePaths = new HashSet<String> ();

      // Calculating file paths for all aql files with in a module
      AQLModuleProject modularProject = aqlLibrary.getModuleLibraryMap ().get (projectName);
      if (modularProject != null) {
        HashMap<String, AQLModule> modules = modularProject.getAQLModules ();

        AQLModule aqlModule = modules.get (modName);
        List<String> paths = aqlModule.getAqlFilePaths ();
        for (String p : paths) {
          aqlFilePaths.add (p);
        }

        pathList = new ArrayList<String> (aqlFilePaths);
      }
    }

    return pathList;
	}
	
	private AQLModule getAQLModuleFromCurrAndRefProjects(IProject project, String mName) {
		IProject refProjects[] = null;
		List<String> projectNames = new ArrayList<String>();
		String projectName = project.getLocation().toOSString();
		projectNames.add(projectName);
		if (projectName == null) {
			return null;
		}
		// Calculating all the dependent projects of the current project..
		try {
			refProjects = project.getReferencedProjects();
		} catch (CoreException exp) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Constants.UNABLE_TO_PROCESS_AQL_DOC_COMMENT,
					exp);
			exp.printStackTrace();
		}
		
		if ((refProjects != null) && (refProjects.length > 0)) {
			for (IProject proj : refProjects) {
				// collecting all the project names and later use them as keys
				// to search for the source..
				projectNames.add(proj.getLocation().toOSString());
			}
		}
		// Calculating file paths for all the referenced module projects
		for (String prjName : projectNames) {
			AQLModuleProject modularProject = aqlLibrary.getModuleLibraryMap().get(prjName);
			if (modularProject == null) {
				return null;
			}
			HashMap<String, AQLModule> modules = modularProject.getAQLModules();
			String moduleName = prjName + File.separator + "textAnalytics" + File.separator + "src" + File.separator + mName;
			if(modules.containsKey(moduleName))
				return modules.get(moduleName);
			
		}
		return null;
		
	}

	private ArrayList<String> getSearchPathList(String searchPath) {
		String[] tokens = searchPath.split(SEMICOLON_STRING); // Single blank is the separator.
		return new ArrayList<String>(Arrays.asList(tokens));
	}

	// This method returns all the search path for modular type projects..
	private ArrayList<String> getModularProjectSearchPathList(IProject project) {
		List<String> projectNames = new ArrayList<String>();
		String projectName = project.getLocation().toOSString();
		projectNames.add(projectName);
		Set<String> aqlFilePaths = new HashSet<String>();
		
		IProject refProjects[] = null;
		// Calculating all the dependent projects of the current project..
		try {
			refProjects = project.getReferencedProjects();
		} catch (CoreException exp) {
			LogUtil.getLogForPlugin(Activator.PLUGIN_ID).logAndShowError(
					Constants.UNABLE_TO_PROCESS_AQL_DOC_COMMENT,
					exp);
			exp.printStackTrace();
		}
		if ((refProjects != null) && (refProjects.length > 0)) {
			for (IProject proj : refProjects) {
				// collecting all the project names and later use them as keys
				// to search for the source..
				if(proj.isOpen())
					projectNames.add(proj.getLocation().toOSString());
			}
		}
		// Calculating file paths for all the referenced module projects as
		// well...
		for (String prjName : projectNames) {
			AQLModuleProject modularProject = aqlLibrary.getModuleLibraryMap()
					.get(prjName);
			// AQLLibrary will contain the lists of projects that contains at least a single AQL file
			// If the modular project does not contain AQL's, then that project
			// will not be updated in the AQL library
			if (modularProject != null) {
			  HashMap<String, AQLModule> modules = modularProject.getAQLModules();
	      Iterator<String> moduleItr = modules.keySet().iterator();
	      String moduleName = ""; //$NON-NLS-1$
	      while (moduleItr.hasNext()) {
	        moduleName = moduleItr.next();
	        AQLModule aqlModule = modules.get(moduleName);
	        List<String> paths = aqlModule.getAqlFilePaths();
	        for (String p : paths) {
	          aqlFilePaths.add(p);
	        }
	      }
			}
		}// end of for loop..
		return new ArrayList<String>(aqlFilePaths);
	}

	// This method returns all the files with in a module path list
	private Set<String> getAllCurrModuleFiles(ArrayList<String> modulePathList) {
		Iterator<String> iterator1 = modulePathList.iterator();
		// System.out.println("AQLDocHover.getAllCurrModuleFiles()"+currentFileLocation);
		fileSet.add(currentFileLocation);
		// All files that are there in the search path
		while (iterator1.hasNext()) {
			String temp = iterator1.next();
			IPath path = new Path(temp).makeAbsolute();
			try {
				new FileTraversal() {
					public void onFile(final File f) {
						if (f.getName().endsWith(".aql")) {
							moduleFileSet.add(f.getAbsolutePath().toString());
						}
					}
				}.traverse(new File(path.toOSString()));
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		return moduleFileSet;
	}

	// This method returns all the files within the search path list
	private Set<String> getAllAQLFiles(ArrayList<String> searchpathList) {
		Iterator<String> pathItr = searchpathList.iterator();
		//System.out.println("Current File Location is " + currentFileLocation);
		fileSet.add(currentFileLocation);
		// All files that are there in the search path
		while (pathItr.hasNext()) {
			String temp = pathItr.next();
			IPath path = new Path(temp).makeAbsolute();
			try {
				new FileTraversal() {
					public void onFile(final File f) {
						if (f.getName().endsWith(".aql")) //$NON-NLS-1$
						{
							fileSet.add(f.getAbsolutePath().toString());
						}
					}
				}.traverse(new File(path.toOSString()));
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
		return fileSet;
	}

	@Override
	public String getHoverInfo(ITextViewer viewer, IRegion region) {
		return getHoverInfo2 (viewer, region);
	}
	


}
